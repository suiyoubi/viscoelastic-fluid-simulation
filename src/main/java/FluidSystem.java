import org.apache.commons.math3.util.Pair;
import processing.core.PVector;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class FluidSystem {
    private static final float rho_0 = 10F; // Rest density
    private static final float k = (float) 0.01; // Pressure-density linear coefficient
    private static final float k_near = (float) 0.01; // NearPressure-density linear coefficient
    private static final float k_spring = (float) 0.3; // Spring elasticity coefficient
    private static final float yieldRatio = 0.15F;
    private static final float miu = 0.5F; // Friction parameter
    private static final float miu_rigid = 0F; // Friction parameter for movable rigid bodies
    public static float h = 20; // Interaction range
    private final float dt = (float) 2; // Timestamp
    private final List<Particle> particles; // Fluid particles
    private final List<RigidSphere> rigidSpheres; // All rigid spheres
    private final Map<Pair<Particle, Particle>, Float> springs; // Springs between close particle pairs
    // Variadic Parameters
    public PVector gravity = new PVector(0, (float) 0.03); // Gravity acceleration
    public float alpha = 0.3F; // Plasticity constant
    public float delta = 0.0F; // Viscosity's quadratic dependence
    public float beta = 0.01F;  // Viscosity's linear dependence
    private int particleNum; // Number of the fluid particles in the system

    public FluidSystem() {
        this.particles = new LinkedList<>();
        this.springs = new HashMap();
        this.rigidSpheres = new LinkedList<>();
        this.particleNum = 0;
    }

    public void simulationStep() {
        applyGravity();
        applyViscosity();
        positionUpdate();
        adjustSprings();
        applySpringDisplacements();
        doubleDensityRelaxation();
        resolveCollision();
        velocityUpdate();
        this.particles.forEach(Particle::cleanNeighbor); // Cleanup the neighbor mappings after each iteration
    }

    /**
     * Add particle in the given position to the system
     */
    public void addParticle(PVector position) {
        this.particles.add(new Particle(position, particleNum));
        particleNum += 1;
    }

    public void addParticleWithVelocity(PVector position, PVector velocity) {
        Particle particle = new Particle(position, particleNum);
        particle.velocity = velocity;
        this.particles.add(particle);
        particleNum += 1;
    }

    /**
     * Add fixed rigid body to the system with given position and radius
     */
    public void addFixedRigidBody(PVector position, float radius) {
        this.rigidSpheres.add(new RigidSphere(position, radius));
    }

    /**
     * Add movable rigid body to the system with given postiion, radius and mass
     */
    public void addMovableRigidBody(PVector position, float radius, float mass) {
        this.rigidSpheres.add(new RigidSphere(position, radius, mass));
    }

    /**
     * Retrieve the particle list
     */
    public List<Particle> getParticles() {
        return particles;
    }

    /**
     * Retrieve the rigid sphere list
     */
    public List<RigidSphere> getRigidSpheres() {
        return rigidSpheres;
    }

    /**
     * Apply gravity to each particles
     */
    private void applyGravity() {
        this.particles.forEach(p -> {
            // v = v + dt * g
            p.velocity = p.velocity.add(PVector.mult(gravity, dt));
        });
    }

    /**
     * Update the position of each particle using its velocity
     */
    private void positionUpdate() {
        this.particles.forEach(p -> {
            // x_pre = x
            p.previousPosition = p.position.copy();
            // x = x + dt * v
            p.position = p.position.add(PVector.mult(p.velocity, dt));
        });
    }

    /**
     * Apply the viscosity as the impulses to each particle pairs
     */
    private void applyViscosity() {
        this.particles.forEach(i -> {
            // Find particle i's neighbors whose id is greater than i.id
            List<Particle> neighbors = i.findNeighbors(this.particles);
            neighbors = neighbors.stream().filter(j -> i.id < j.id).collect(Collectors.toList());
            neighbors.forEach(j -> {
                PVector r_ij = PVector.sub(i.position, j.position);
                float r_ij_norm = r_ij.mag();
                float q = r_ij_norm / h;
                PVector r_hat_ij = r_ij.normalize();
                if (q < 1) {
                    // Inward radial velocity
                    float u = PVector.sub(i.velocity, j.velocity).dot(r_hat_ij);
                    if (u > 0) {
                        // Linear an quadratic impulses
                        PVector I = PVector.mult(r_hat_ij, dt * (1 - q) * (delta * u + beta * u * u));
                        PVector I_half = PVector.div(I, 2);
                        i.velocity = PVector.sub(i.velocity, I_half);
                        j.velocity = PVector.add(j.velocity, I_half);
                    }
                }
            });
        });
    }

    /**
     * Adjust the rest length of the spring to simulate the plastic flow when deformation is sufficiently large.
     */
    private void adjustSprings() {
        this.particles.forEach(i -> {
            List<Particle> neighbors = i.findNeighbors(this.particles);
            // Only care i < j pair
            neighbors = neighbors.stream().filter(j -> i.id < j.id).collect(Collectors.toList());
            neighbors.forEach(j -> {
                PVector r_ij = PVector.sub(i.position, j.position);
                float r_ij_norm = r_ij.mag();
                float q = r_ij_norm / h;
                if (q < 1) {
                    Pair<Particle, Particle> pair = new Pair<>(i, j);
                    // Add spring <i,j> with rest length h if there is no spring i j
                    if (!this.springs.containsKey(pair)) {
                        this.springs.put(pair, h);
                    }
                    // Calculate tolerable deformation
                    float L_ij = this.springs.get(pair);
                    float d = yieldRatio * L_ij;
                    if (r_ij_norm > L_ij + d) {
                        // Stretch
                        this.springs.put(pair, L_ij - dt * alpha * (r_ij_norm - L_ij - d));
                    } else if (r_ij_norm < L_ij - d) {
                        // Compress
                        this.springs.put(pair, L_ij + dt * alpha * (L_ij - d - r_ij_norm));
                    }
                }
            });
        });
        // Remove spring if its rest length is too large
        this.springs.entrySet().removeIf(e -> e.getValue() > h);
    }

    /**
     * Apply spring displacements to simulate elastic behaviour
     */
    private void applySpringDisplacements() {
        this.springs.forEach((k, Length) -> {
            Particle i = k.getFirst();
            Particle j = k.getSecond();
            PVector r_ij = PVector.sub(i.position, j.position);
            float r_ij_norm = r_ij.mag();
            PVector r_ij_vector = r_ij.normalize();
            PVector D = r_ij_vector.mult(
                    dt * dt * k_spring * (1 - Length / h) * (Length - r_ij_norm)
            );
            PVector D_half = D.mult(0.5F);
            i.position = PVector.sub(i.position, D_half);
            j.position = PVector.add(j.position, D_half);
        });

    }

    /**
     * Simulate the particle-particle interaction due to pressure
     */
    private void doubleDensityRelaxation() {
        for (Particle i : this.particles) {
            List<Particle> neighbors = i.findNeighbors(this.particles);

            float rho = 0;
            float rho_near = 0;

            // Compute density and near-density
            for (Particle j : neighbors) {
                PVector r_ij = PVector.sub(j.position, i.position);
                float r_ij_norm = r_ij.mag();
                float q = r_ij_norm / h;
                if (q < 1) {
                    rho = rho + (1 - q) * (1 - q);
                    rho_near = rho_near + (1 - q) * (1 - q) * (1 - q);
                }
            }
            // Compute pressure and near-pressure
            float pressure = k * (rho - rho_0);
            float pressure_near = k_near * rho_near;
            // Displacement
            PVector dx = new PVector(0, 0);
            for (Particle j : neighbors) {
                if (j.equals(i)) continue;
                PVector r_ij = PVector.sub(j.position, i.position);
                float r_ij_norm = r_ij.mag();
                float q = r_ij_norm / h;
                if (q < 1) {
                    PVector direction = r_ij.normalize();
                    // Apply displacements
                    direction = direction.mult(
                            dt * dt * (pressure * (1 - q) + pressure_near * (1 - q) * (1 - q))
                    );
                    j.position = j.position.add(direction.div(2));
                    dx = dx.sub(direction.div(2));
                }
            }
            i.position = i.position.add(dx);
            i.pressure = pressure;
        }
    }

    /**
     * Resolve Particle-Body and Body-Body interactions
     */
    private void resolveCollision() {
        // Wall Detection
        this.particles.forEach(p -> {
            PVector n_hat = null;
            if (p.position.x < 0) {
                p.position.x = 0;
                n_hat = new PVector(1, 0);
            }
            if (p.position.y < 0) {
                p.position.y = 0;
                n_hat = new PVector(0, 1);
            }
            if (p.position.x > Simulation.boxWidth) {
                p.position.x = Simulation.boxWidth;
                n_hat = new PVector(-1, 0);
            }
            if (p.position.y > Simulation.boxWidth) {
                p.position.y = Simulation.boxWidth;
                n_hat = new PVector(0, -1);
            }

            if (n_hat != null) {
                PVector v_normal = PVector.mult(n_hat, PVector.dot(p.velocity, n_hat));
                PVector v_tangent = PVector.sub(p.velocity, v_normal);
                PVector I = PVector.sub(v_normal, PVector.mult(v_tangent, miu));
                p.velocity = PVector.add(p.velocity, I);
            }
        });

        // Rigid bodies non-movable
        this.rigidSpheres.stream().filter(rs -> !rs.movable).collect(Collectors.toList()).forEach(rigidSphere -> {
            this.particles.forEach(particle -> {
                if (rigidSphere.isInside(particle)) {
                    // Compute collision impulse
                    PVector I = computeImpulse(rigidSphere, particle);
                    particle.velocity = PVector.add(particle.velocity, I);
                    // Extract the particle
                    particle.position = rigidSphere.extract(particle);
                }
            });
        });

        // Rigid bodies movable
        this.rigidSpheres.stream().filter(rs -> rs.movable).collect(Collectors.toList()).forEach(rigidSphere -> {
            PVector I_net = new PVector(0, 0);
            // Save original body position
            rigidSphere.previousCenter = rigidSphere.center.copy();
            // Advance body using V
            rigidSphere.velocity = PVector.add(rigidSphere.velocity, PVector.mult(gravity, dt));
            rigidSphere.center = PVector.add(rigidSphere.center, PVector.mult(rigidSphere.velocity, dt));
            for (Particle particle : this.particles) {
                if (rigidSphere.isInside(particle)) {
                    // Compute collision impulse
                    PVector I = computeImpulse(rigidSphere, particle);

                    I_net = PVector.add(I_net, I);
                }
            }
            // Modify V with I_net
            rigidSphere.velocity = PVector.add(rigidSphere.velocity, PVector.div(I_net, rigidSphere.mass));

            // Resolve collisions and contacts between bodies (Only consider collision with walls)
            PVector n_hat_sphere = null;
            if (rigidSphere.center.x - rigidSphere.radius < 0) {
                rigidSphere.center.x = rigidSphere.radius;
                n_hat_sphere = new PVector(1, 0);
            }
            if (rigidSphere.center.y < 0) {
                rigidSphere.center.y = rigidSphere.radius;
                n_hat_sphere = new PVector(0, 1);
            }
            if (rigidSphere.center.x + rigidSphere.radius > Simulation.boxWidth) {
                rigidSphere.center.x = Simulation.boxWidth - rigidSphere.radius;
                n_hat_sphere = new PVector(-1, 0);
            }
            if (rigidSphere.center.y + rigidSphere.radius > Simulation.boxWidth) {
                rigidSphere.center.y = Simulation.boxWidth - rigidSphere.radius;
                n_hat_sphere = new PVector(0, -1);
            }
            if (n_hat_sphere != null) {
                PVector v_normal = PVector.mult(n_hat_sphere, PVector.dot(rigidSphere.velocity, n_hat_sphere));
                PVector v_tangent = PVector.sub(rigidSphere.velocity, v_normal);
                PVector I = PVector.sub(v_normal, PVector.mult(v_tangent, miu));
                rigidSphere.velocity = PVector.mult(rigidSphere.velocity, -0.2F);//new PVector(0, 0);//PVector.sub(rigidSphere.velocity, I);
            }

            // Apply impulse to particles
            this.particles.forEach(particle -> {
                if (rigidSphere.isInside(particle)) {
                    // Compute collision impulse
                    PVector I = computeImpulse(rigidSphere, particle);
                    particle.velocity = PVector.add(particle.velocity, I);
                    // Extract the particle
                    particle.position = rigidSphere.extract(particle);
                }
            });
        });
    }

    private PVector computeImpulse(RigidSphere rigidSphere, Particle particle) {
        PVector v_bar = PVector.sub(particle.velocity, rigidSphere.velocity);
        PVector n_hat = rigidSphere.calculateNHat(particle);
        PVector v_normal = PVector.mult(n_hat, PVector.dot(v_bar, n_hat));
        PVector v_tangent = PVector.sub(v_bar, v_normal);
        return PVector.sub(v_normal, PVector.mult(v_tangent, miu_rigid));
    }

    /**
     * Update velocity of particles based on positions.
     */
    private void velocityUpdate() {
        this.particles.forEach(p -> {
            // v = (x - x_pre) / dt
            p.velocity = PVector.sub(p.position, p.previousPosition);
            p.velocity = p.velocity.div(dt);
        });
    }
}
