import processing.core.PVector;

import java.util.List;
import java.util.stream.Collectors;

public class Particle {
    public static float PARTICLE_RADIUS = 5;
    public static double h = FluidSystem.h;
    public final int id;
    public PVector position;
    public PVector previousPosition;
    public PVector velocity;
    public float pressure;

    private List<Particle> neighbors;
    private boolean isNeighborCalculated;

    public Particle(PVector position, int id) {
        this.position = position;
        this.previousPosition = new PVector(0, 0);
        this.velocity = new PVector(0, 0);
        this.id = id;
    }

    /**
     * Search for the neighbors of the particle in the given range.
     */
    public List<Particle> findNeighbors(List<Particle> searchRange) {
        // If already calculated, early return
        if (isNeighborCalculated) {
            return this.neighbors;
        }

        this.isNeighborCalculated = true;
        this.neighbors = searchRange.stream().filter(p -> PVector.dist(p.position, this.position) < h).collect(Collectors.toList());
        return this.neighbors;
    }

    public void cleanNeighbor() {
        this.isNeighborCalculated = false;
    }
}
