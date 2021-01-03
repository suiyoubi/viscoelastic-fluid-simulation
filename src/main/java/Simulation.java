import processing.core.PApplet;
import processing.core.PVector;


public class Simulation extends PApplet {
    public static final int canvasHeight = 300;
    private static final int MASS = 5;
    private static final int canvasWidth = 600;
    public static final int boxWidth = canvasWidth / 2;
    private static final int wallStroke = 10;
    private static final int rigidBodyStroke = 0;
    private static final float PURE_WHITE = 255;
    private static final int fountainCenterX = boxWidth / 2;
    private static final int fountainCenterY = 150;
    public boolean isAddParticle = true;
    private FluidSystem fluidSystem;
    private ControlPanel controlPanel;
    private PresetModelLoader presetModelLoader;
    public boolean isFountain = false;
    private static int modelNum = 0;

    public static void main(String[] args) {
        // args check
        if(args.length == 1) {
            try {
                modelNum = Integer.parseInt(args[0]);
                if(modelNum < 0 || modelNum > 5) {
                    System.out.println("Invalid model number!");
                    System.exit(-1);
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid model number!");
                System.exit(-1);
            }
        }
        // Initialize the GUI
        PApplet.main(Simulation.class);
    }

    @Override
    public void settings() {
        // GUI Size
        this.size(canvasWidth, canvasHeight);
    }

    @Override
    public void setup() {

        int model = modelNum;

        this.fluidSystem = new FluidSystem();
        this.controlPanel = new ControlPanel(this, this.fluidSystem);
        this.presetModelLoader = new PresetModelLoader(boxWidth, height);

        // Load Preset Model
        this.presetModelLoader.initialize(model);
        this.presetModelLoader.getParticlePositions().forEach(position -> {
            this.fluidSystem.addParticle(position);
        });
        this.presetModelLoader.getFixedRBPositions().forEach((position, radius) -> {
            this.fluidSystem.addFixedRigidBody(position, radius);
        });
        this.presetModelLoader.getMovedRBPositions().forEach((position, radius) -> {
            this.fluidSystem.addMovableRigidBody(position, radius, MASS);
        });

        if(model == 5) {
            isFountain = true;
        }
    }

    private void addFluid() {
        for (RigidSphere rigidSphere : this.fluidSystem.getRigidSpheres()) {
            if (PVector.dist(rigidSphere.center, new PVector(mouseX, mouseY)) < rigidSphere.radius) {
                return;
            }
        }
        this.fluidSystem.addParticle(new PVector(mouseX, mouseY));
    }

    private void addFluidWithVelocity() {
        int times = 0;
        while (times++ < 5)
            this.fluidSystem.addParticleWithVelocity(
                    new PVector(fountainCenterX + random(-5, 5), fountainCenterY + random(-5, 5)),
                    new PVector(0, -3F)
            );
    }

    @Override
    public void mousePressed() {
        this.controlPanel.checkPressed();
    }

    @Override
    public void draw() {
        // Reset the canvas
        this.background(PURE_WHITE);

        // Add fluids
        if (isAddParticle && mousePressed && mouseX > 0 && mouseX < boxWidth) {
            this.addFluid();
        }

        // Simulate the fluid
        if (controlPanel.isPlay) {
            this.fluidSystem.simulationStep();
        }

        if (isFountain && controlPanel.isPlay) {
            this.addFluidWithVelocity();
        }

        // Draw the fluid
        strokeWeight(Particle.PARTICLE_RADIUS * 2);
        double maxPressure = this.fluidSystem.getParticles().stream().map(p -> p.pressure).max(Float::compare).orElse(0F);
        double minPressure = this.fluidSystem.getParticles().stream().map(p -> p.pressure).min(Float::compare).orElse(0F);
        for (Particle particle : this.fluidSystem.getParticles()) {
            // Third-Order Color Function
            int color = (int) (256 * Math.pow((particle.pressure - minPressure) / (maxPressure - minPressure), 3));
            stroke(252 - color, 236, 12);
            PVector position = particle.position;
            point(position.x, position.y);
        }

        // Draw the box wall
        strokeWeight(wallStroke);
        stroke(0);
        line(0, 0, boxWidth, 0);
        line(0, 0, 0, height);
        line(boxWidth, height, boxWidth, 0);
        line(boxWidth, height, 0, height);

        // Draw Rigid bodies
        strokeWeight(rigidBodyStroke);
        for (RigidSphere rigidSphere : this.fluidSystem.getRigidSpheres()) {
            fill(rigidSphere.red, rigidSphere.green, rigidSphere.blue);
            ellipse(rigidSphere.center.x, rigidSphere.center.y, rigidSphere.radius * 2, rigidSphere.radius * 2);
        }

        // Display info
        this.controlPanel.drawDisplayPanel();
    }
}
