package simulation3d;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import java.util.List;
import java.util.stream.Collectors;

public class Particle3D {
    public static float PARTICLE_RADIUS = 12;
    public static double h = Fluid3D.h;
    public final int id;
    public Vector3D position;
    public Vector3D previousPosition;
    public Vector3D velocity;
    public double pressure;

    private List<Particle3D> neighbors;
    private boolean isNeighborCalculated;

    public Particle3D(Vector3D position, int id) {
        this.position = position;
        this.previousPosition = new Vector3D(0, 0, 0);
        this.velocity = new Vector3D(0, 0, 0);
        this.id = id;
    }

    /**
     * Search for the neighbors of the particle in the given range.
     */
    public List<Particle3D> findNeighbors(List<Particle3D> searchRange) {
        // If already calculated, early return
        if (isNeighborCalculated) {
            return this.neighbors;
        }

        this.isNeighborCalculated = true;
        this.neighbors = searchRange.stream().filter(p -> Vector3D.distance(p.position, this.position) < h).collect(Collectors.toList());
        return this.neighbors;
    }

    public void cleanNeighbor() {
        this.isNeighborCalculated = false;
    }
}
