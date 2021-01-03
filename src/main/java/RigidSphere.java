import processing.core.PVector;

public class RigidSphere {
    PVector center;
    PVector previousCenter;
    float radius;
    float mass;
    boolean movable;
    PVector velocity;
    int red;
    int green;
    int blue;

    public RigidSphere(PVector center, float radius) {
        this.center = center;
        this.radius = radius;
        this.movable = false;
        this.velocity = new PVector(0, 0);
        // Assign random color for visualization
        this.red = (int) (Math.random() * 256);
        this.green = (int) (Math.random() * 256);
        this.blue = (int) (Math.random() * 256);
    }

    public RigidSphere(PVector center, float radius, float mass) {
        this(center, radius);
        this.movable = true;
        this.mass = mass;
    }

    /**
     * Whether a given particle is inside the sphere or not
     */
    public boolean isInside(Particle particle) {
        return PVector.dist(this.center, particle.position) < radius;
    }

    /**
     * Calculate the normal vector of a particle respect to this sphere
     */
    public PVector calculateNHat(Particle particle) {
        return PVector.sub(particle.position, this.center).normalize();
    }

    /**
     * Extract the particle from the sphere
     */
    public PVector extract(Particle particle) {
        return PVector.add(this.center, PVector.mult(this.calculateNHat(particle), this.radius));
    }
}
