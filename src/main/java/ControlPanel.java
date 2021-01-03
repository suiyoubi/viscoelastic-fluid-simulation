import processing.core.PVector;

public class ControlPanel {

    private static final int revertGX1 = 460;
    private static final int revertGX2 = 600;
    private static final int revertGY1 = 270;
    private static final int revertGY2 = 300;
    private static final int modeGX1 = 460;
    private static final int modeGX2 = 600;
    private static final int modeGY1 = 240;
    private static final int modeGY2 = 270;
    private static final int gravityX1 = 460;
    private static final int gravityX2 = 600;
    private static final int gravityY1 = 210;
    private static final int gravityY2 = 240;
    private static final int clearBodyX1 = 440;
    private static final int clearBodyY1 = 120;
    private static final int clearBodyX2 = 600;
    private static final int clearBodyY2 = 150;
    private static final int playX1 = 510;
    private static final int playX2 = 600;
    private static final int playY1 = 85;
    private static final int playY2 = 115;
    private static final PVector gravityMin = new PVector(0, (float) 0.0);
    private static final PVector gravityMax = new PVector(0, (float) 0.1);
    private static final float plasticityMin = 0;
    private static final float plasticityMax = 2F;
    private static final float viscosityMin = 0;
    private static final float viscosityMax = 1.2F;
    private final Simulation simulation;
    private final FluidSystem fluidSystem;
    private final Toggle gravityToggle;
    private final Toggle plasticityToggle;
    private final Toggle viscosityToggle;
    private long previousTime = 0;
    private int revertGravityColor = 50;
    private int mouseModeColor = 50;
    private int playColor = 50;
    private int gravityRevert = 1;
    public boolean isPlay = true;


    public ControlPanel(Simulation simulation, FluidSystem fluidSystem) {
        this.simulation = simulation;
        this.fluidSystem = fluidSystem;
        this.gravityToggle = new Toggle("Gravity", 0.3F, gravityX1, gravityY1, gravityX2, gravityY2, 80, 10, simulation);
        this.plasticityToggle = new Toggle("Plasticity", 0.15F, gravityX1, gravityY1 - 30, gravityX2, gravityY2 - 30, 90, 10, simulation);
        this.viscosityToggle = new Toggle("Viscosity", 0F, gravityX1, gravityY1 - 60, gravityX2, gravityY2 - 60, 90, 10, simulation);
    }

    private boolean isInsideRect(int x1, int y1, int x2, int y2) {
        return this.simulation.mouseX > x1 && this.simulation.mouseX < x2 && this.simulation.mouseY > y1 && this.simulation.mouseY < y2;
    }

    public void drawDisplayPanel() {

        // FPS
        long current = System.currentTimeMillis();
        long diff = current - previousTime;
        previousTime = current;
        double FPS = 1.0 / (diff / 1000.0);
        this.simulation.textSize(30);
        this.simulation.fill(0);
        this.simulation.text(String.format("%d FPS", (int) FPS), 500, 30);

        // Num of Particles
        this.simulation.textSize(20);
        this.simulation.text(String.format("%d Particles", (int) this.fluidSystem.getParticles().size()), 460, 60);

        // Num of Spheres
        this.simulation.fill(0);
        this.simulation.text(String.format("%d Spheres", this.fluidSystem.getRigidSpheres().size()), 475, 80);

        // Revert Gravity
        this.simulation.fill(revertGravityColor);
        this.simulation.text("Revert Gravity", revertGX1, revertGY1, revertGX2, revertGY2);

        // Mouse Mode
        this.simulation.fill(mouseModeColor);
        if (this.simulation.isAddParticle) {
            this.simulation.text("Add Particle", modeGX1, modeGY1, modeGX2, modeGY2);
        } else {
            this.simulation.text("Add RigidBody", modeGX1, modeGY1, modeGX2, modeGY2);
        }

        // Fluid Parameters
        this.gravityToggle.drawToggle();
        this.plasticityToggle.drawToggle();
        this.viscosityToggle.drawToggle();

        // Clear RigidBody
        this.simulation.text("Clear RigidBody", clearBodyX1, clearBodyY1, clearBodyX2, clearBodyY2);
        this.simulation.textSize(20);

        // Clear Fluid
        this.simulation.text("Clear Fluid", clearBodyX1-120, clearBodyY1, clearBodyX2-120, clearBodyY2);
        this.simulation.textSize(20);

        // Play or Pause
        this.simulation.textSize(30);
        this.simulation.fill(playColor);
        if (this.isPlay) {
            this.simulation.text("Play", playX1, playY1, playX2, playY2);
        } else {
            this.simulation.text("Pause",  playX1, playY1, playX2, playY2);
        }
    }
    public void checkPressed() {

        if (isInsideRect(revertGX1, revertGY1, revertGX2, revertGY2)) {
            this.gravityRevert = (this.gravityRevert == 1) ? -1 : 1;
            revertGravityColor = (255 - revertGravityColor);
        }
        if (isInsideRect(clearBodyX1, clearBodyY1, clearBodyX2, clearBodyY2)) {
            this.fluidSystem.getRigidSpheres().clear();
        }
        if (isInsideRect(clearBodyX1-120, clearBodyY1, clearBodyX2-120, clearBodyY2)) {
            this.fluidSystem.getParticles().clear();
        }
        if (isInsideRect(modeGX1, modeGY1, modeGX2, modeGY2)) {
            this.simulation.isAddParticle = !this.simulation.isAddParticle;
            mouseModeColor = (255 - mouseModeColor);
        }
        if (isInsideRect(playX1, playY1, playX2, playY2)) {
            this.isPlay = !isPlay;
            playColor = (255 - playColor);
        }

        if (!simulation.isAddParticle && this.simulation.mouseX < Simulation.boxWidth) {
            //this.fluidSystem.addFixedRigidBody(new PVector(this.simulation.mouseX, this.simulation.mouseY), simulation.random(5, 30));
            this.fluidSystem.addFixedRigidBody(new PVector(this.simulation.mouseX, this.simulation.mouseY), 15);
        }

        this.gravityToggle.checkChange();
        PVector gravity = PVector.add(PVector.mult(PVector.sub(gravityMax, gravityMin), this.gravityToggle.percentage()), gravityMin);
        this.fluidSystem.gravity = PVector.mult(gravity, gravityRevert);

        this.plasticityToggle.checkChange();
        this.fluidSystem.alpha = this.plasticityToggle.percentage() * (plasticityMax - plasticityMin) + plasticityMin;

        this.viscosityToggle.checkChange();
        this.fluidSystem.delta = (float) Math.sqrt(this.viscosityToggle.percentage()) * (viscosityMax - viscosityMin) + viscosityMin;

    }
}
