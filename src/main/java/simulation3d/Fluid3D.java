package simulation3d;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.apache.commons.math3.util.Pair;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Fluid3D {
    private static final float rho_0 = 10; // Rest density
    private static final float k = (float) 0.01; // Pressure-density linear coefficient
    private static final float k_near = (float) 0.01; // NearPressure-density linear coefficient
    private static final float k_spring = (float) 0.3; // Spring elasticity coefficient
    private static final float yieldRatio = 0.15F;
    private static final float miu = 0.5F; // Friction parameter
    private static final float miu_rigid = 0F;// Friction parameter for movable rigid bodies
    public static double h = 65; // Interaction range
    private final float dt = (float) 3; // Timestamp
    private final List<Particle3D> particles; // Fluid particles
    private final List<RigidSphere3D> rigidSpheres; // All rigid spheres
    private final Map<Pair<Particle3D, Particle3D>, Double> springs; // Springs between close particle pairs
    // Variadic Parameters
    public Vector3D gravity = new Vector3D(0, (float) 0.01, 0); // Gravity acceleration
    public float alpha = 0.3F; // Plasticity constant
    public float delta = 0.F; // Viscosity's quadratic dependence
    public float beta = 0.1F;  // Viscosity's linear dependence
    private int particleNum; // Number of the fluid particles in the system

    public Fluid3D() {
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
        this.particles.forEach(Particle3D::cleanNeighbor); // Cleanup the neighbor mappings after each iteration
    }

    /**
     * Add particle in the given position to the system
     */
    public void addParticle(Vector3D position) {
        this.particles.add(new Particle3D(position, particleNum));
        particleNum += 1;
    }

    /**
     * Add fixed rigid body to the system with given position and radius
     */
    public void addFixedRigidBody(Vector3D position, float radius) {
        this.rigidSpheres.add(new RigidSphere3D(position, radius));
    }

    /**
     * Add movable rigid body to the system with given postiion, radius and mass
     */
    public void addMovableRigidBody(Vector3D position, int radius, float mass) {
        this.rigidSpheres.add(new RigidSphere3D(position, radius, mass));
    }

    /**
     * Retrieve the particle list
     */
    public List<Particle3D> getParticles() {
        return particles;
    }

    /**
     * Retrieve the rigid sphere list
     */
    public List<RigidSphere3D> getRigidSpheres() {
        return rigidSpheres;
    }

    /**
     * Apply gravity to each particles
     */
    private void applyGravity() {
        this.particles.forEach(p -> {
            // v = v + dt * g
            p.velocity = p.velocity.add(gravity.scalarMultiply(dt));
        });
    }

    /**
     * Update the position of each particle using its velocity
     */
    private void positionUpdate() {
        this.particles.forEach(p -> {
            // x_pre = x
            p.previousPosition = new Vector3D(p.position.getX(), p.position.getY(), p.position.getZ());
            // x = x + dt * v
            p.position = p.position.add(p.velocity.scalarMultiply(dt));
        });
    }

    /**
     * Apply the viscosity as the impulses to each particle pairs
     */
    private void applyViscosity() {
        this.particles.forEach(i -> {
            // Find particle i's neighbors whose id is greater than i.id
            List<Particle3D> neighbors = i.findNeighbors(this.particles);
            neighbors = neighbors.stream().filter(j -> i.id < j.id).collect(Collectors.toList());
            neighbors.forEach(j -> {
                Vector3D r_ij = i.position.subtract(j.position);
                double r_ij_norm = r_ij.getNorm();
                double q = r_ij_norm / h;
                Vector3D r_hat_ij = r_ij_norm != 0 ? r_ij.normalize() : new Vector3D(0, 0, 0);
                if (q < 1) {
                    // Inward radial velocity
                    double u = i.velocity.subtract(j.velocity).dotProduct(r_hat_ij);
                    if (u > 0) {
                        // Linear an quadratic impulses
                        Vector3D I = r_hat_ij.scalarMultiply(dt * (1 - q) * (delta * u + beta * u * u));
                        Vector3D I_half = I.scalarMultiply(0.5);
                        i.velocity = i.velocity.subtract(I_half);
                        j.velocity = j.velocity.add(I_half);
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
            List<Particle3D> neighbors = i.findNeighbors(this.particles);
            // Only care i < j pair
            neighbors = neighbors.stream().filter(j -> i.id < j.id).collect(Collectors.toList());
            neighbors.forEach(j -> {
                Vector3D r_ij = i.position.subtract(j.position);
                double r_ij_norm = r_ij.getNorm();
                double q = r_ij_norm / h;
                if (q < 1) {
                    Pair<Particle3D, Particle3D> pair = new Pair<>(i, j);
                    // Add spring <i,j> with rest length h if there is no spring i j
                    if (!this.springs.containsKey(pair)) {
                        this.springs.put(pair, h);
                    }
                    // Calculate tolerable deformation
                    double L_ij = this.springs.get(pair);
                    double d = yieldRatio * L_ij;
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
            Particle3D i = k.getFirst();
            Particle3D j = k.getSecond();
            Vector3D r_ij = i.position.subtract(j.position);
            double r_ij_norm = r_ij.getNorm();
            Vector3D r_ij_vector = r_ij.normalize();
            Vector3D D = r_ij_vector.scalarMultiply(
                    dt * dt * k_spring * (1 - Length / h) * (Length - r_ij_norm)
            );
            Vector3D D_half = D.scalarMultiply(0.5F);
            i.position = i.position.subtract(D_half);
            j.position = j.position.add(D_half);
        });

    }

    /**
     * Simulate the particle-particle interaction due to pressure
     */
    private void doubleDensityRelaxation() {
        for (Particle3D i : this.particles) {
            List<Particle3D> neighbors = i.findNeighbors(this.particles);

            double rho = 0;
            double rho_near = 0;

            // Compute density and near-density
            for (Particle3D j : neighbors) {
                if(i.equals(j)) {
                    continue;
                }
                Vector3D r_ij = j.position.subtract(i.position);
                double r_ij_norm = r_ij.getNorm();
                double q = r_ij_norm / h;
                if (q < 1) {
                    rho = rho + (1 - q) * (1 - q);
                    rho_near = rho_near + (1 - q) * (1 - q) * (1 - q);
                }
            }
            // Compute pressure and near-pressure
            double pressure = k * (rho - rho_0);
            double pressure_near = k_near * rho_near;
            // Displacement
            Vector3D dx = new Vector3D(0,0, 0);
            for (Particle3D j : neighbors) {
                if (j.equals(i)) continue;
                Vector3D r_ij = j.position.subtract(i.position);
                double r_ij_norm = r_ij.getNorm();
                double q = r_ij_norm / h;
                if (q < 1) {
                    Vector3D direction = r_ij_norm != 0 ? r_ij.normalize() : r_ij;
                    // Apply displacements
                    direction = direction.scalarMultiply(
                            0.5 * dt * dt * (pressure * (1 - q) + pressure_near * (1 - q) * (1 - q))
                    );
                    j.position = j.position.add(direction);
                    dx = dx.subtract(direction);
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
            Vector3D n_hat = null;
            if (p.position.getX() < 0) {
                p.position = new Vector3D(0, p.position.getY(), p.position.getZ());
                n_hat = new Vector3D(1, 0, 0);
            }
            if (p.position.getY() < 0) {
                p.position = new Vector3D(p.position.getX(), 0, p.position.getZ());
                n_hat = new Vector3D(0, 1, 0);
            }
            if (p.position.getZ() < 0) {
                p.position = new Vector3D(p.position.getX(), p.position.getY(), 0);
                n_hat = new Vector3D(0,  0, 1);
            }
            if (p.position.getX() > Simulation3D.boxWidth) {
                p.position = new Vector3D(Simulation3D.boxWidth, p.position.getY(), p.position.getZ());
                n_hat = new Vector3D(-1, 0, 0);
            }
            if (p.position.getY() > Simulation3D.canvasHeight) {
                p.position = new Vector3D(p.position.getX(), Simulation3D.canvasHeight, p.position.getZ());
                n_hat = new Vector3D(0, -1, 0);
            }
            if (p.position.getZ() > Simulation3D.canvasLength) {
                p.position = new Vector3D(p.position.getX(), p.position.getY(), Simulation3D.canvasLength);
                n_hat = new Vector3D(0, 0, -1);
            }

            if (n_hat != null) {
                Vector3D v_normal = n_hat.scalarMultiply(p.velocity.dotProduct(n_hat));
                Vector3D v_tangent = p.velocity.subtract(v_normal);
                Vector3D I = v_normal.subtract(v_tangent.scalarMultiply(miu));
                p.velocity = p.velocity.add(I);
            }
        });

        // Rigid bodies non-movable
        this.rigidSpheres.stream().filter(rs -> !rs.movable).collect(Collectors.toList()).forEach(rigidSphere -> {
            this.particles.forEach(particle -> {
                if (rigidSphere.isInside(particle)) {
                    // Compute collision impulse
                    Vector3D v_bar = particle.velocity;
                    Vector3D n_hat = rigidSphere.calculateNHat(particle);
                    Vector3D v_normal = n_hat.scalarMultiply(v_bar.dotProduct(n_hat));
                    Vector3D v_tangent = v_bar.subtract(v_normal);
                    Vector3D I = v_normal.subtract(v_tangent.scalarMultiply(miu));

                    particle.velocity = particle.velocity.add(I);
                    // Extract the particle
                    particle.position = rigidSphere.extract(particle);
                }
            });
        });
    }

    /**
     * Update velocity of particles based on positions.
     */
    private void velocityUpdate() {
        this.particles.forEach(p -> {
            // v = (x - x_pre) / dt
            p.velocity = p.position.subtract(p.previousPosition);
            p.velocity = p.velocity.scalarMultiply(1.0 / dt);
        });
    }
}
