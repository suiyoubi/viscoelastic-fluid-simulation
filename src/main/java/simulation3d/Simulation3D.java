package simulation3d;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class Simulation3D {
    public static int canvasHeight; // Y
    public static int canvasLength; // Z
    public static int boxWidth; // X

    private static Fluid3D fluid;
    private static PrintWriter printWriter;
    private static int frameLimit = 250;
    private static int currentFrame = 0;
    private static int currentStep = 0;

    public static void main(String[] args) {
        int modelNum = 0;
        if (args.length > 1) {
            try {
                modelNum = Integer.parseInt(args[0]);
                if (modelNum < 0 || modelNum > 4) {
                    System.out.println("Model Num Invalid!");
                    System.exit(-1);
                }
            } catch (NumberFormatException e) {
                System.out.println("Model Num Invalid!");
                System.exit(-1);
            }
        }
        if (args.length == 2) {
            try {
                frameLimit = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                System.out.println("Frame Limit Invalid!");
                System.exit(-1);
            }
        }

        PresetModelLoader3D presetModelLoader3D = new PresetModelLoader3D(modelNum);

        canvasHeight = presetModelLoader3D.getHeight();
        canvasLength = presetModelLoader3D.getLength();
        boxWidth = presetModelLoader3D.getWidth();

        fluid = new Fluid3D();

        presetModelLoader3D.initFluid(fluid);
        settings();

        printWriter.println(fluid.getParticles().size());
        printWriter.println(frameLimit);
        printWriter.println(canvasHeight);

        printWriter.println(fluid.getRigidSpheres().size());
        for (RigidSphere3D sphere3D : fluid.getRigidSpheres()) {
            printWriter.println(
                    String.format("%d %d %d %d",
                            (int) sphere3D.center.getX(),
                            (int) sphere3D.center.getY(),
                            (int) sphere3D.center.getZ(),
                            (int) sphere3D.radius));
        }

        while (true) {
            draw();
        }
    }

    public static void settings() {
        try {
            FileWriter fileWriter = new FileWriter("3DSimulationResult.txt");
            printWriter = new PrintWriter(fileWriter);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void draw() {

        // Simulate the fluid
        fluid.simulationStep();
        currentStep += 1;

        // Draw 1 frame each two steps
        if (currentStep % 2 == 1) {
            for (Particle3D particle : fluid.getParticles()) {
                printWriter.println(
                        String.format("%d %d %d",
                                (int) particle.position.getX(),
                                (int) particle.position.getY(),
                                (int) particle.position.getZ()));
            }

            currentFrame += 1;
            System.out.println(String.format("Frame %d Generated", currentFrame));

            if (currentFrame >= frameLimit) {
                printWriter.close();
                System.exit(1);
            }
        }
    }
}
