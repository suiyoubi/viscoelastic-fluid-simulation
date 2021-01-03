package simulation3d;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import simulation3d.Particle3D;

public class RigidSphere3D {
    Vector3D center;
    Vector3D previousCenter;
    float radius;
    float mass;
    boolean movable;
    Vector3D velocity;
    int red;
    int green;
    int blue;

    public RigidSphere3D(Vector3D center, float radius) {
        this.center = center;
        this.radius = radius;
        this.movable = false;
        this.velocity = new Vector3D(0, 0, 0);
        // Assign random color for visualization
        this.red = (int) (Math.random() * 256);
        this.green = (int) (Math.random() * 256);
        this.blue = (int) (Math.random() * 256);
    }

    public RigidSphere3D(Vector3D center, float radius, float mass) {
        this(center, radius);
        this.movable = true;
        this.mass = mass;
    }

    /**
     * Whether a given particle is inside the sphere or not
     */
    public boolean isInside(Particle3D particle) {
        return Vector3D.distance(this.center, particle.position) < radius;
    }

    /**
     * Calculate the normal vector of a particle respect to this sphere
     */
    public Vector3D calculateNHat(Particle3D particle) {
        return particle.position.subtract(this.center).normalize();
    }

    /**
     * Extract the particle from the sphere
     */
    public Vector3D extract(Particle3D particle) {
        return this.center.add(this.calculateNHat(particle).scalarMultiply(this.radius));
    }
}
