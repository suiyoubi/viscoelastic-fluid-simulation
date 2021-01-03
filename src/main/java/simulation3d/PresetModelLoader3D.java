package simulation3d;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

public class PresetModelLoader3D {
    public static int maxX = 200; // X
    public static int maxY = 600; // Y
    public static int maxZ = 200; // Z
    private final int model;


    public PresetModelLoader3D(int model) {
        this.model = model;

        if (model == 1) {
            maxX = 200;
            maxY = 600;
            maxZ = 200;
        } else if (model == 2) {
            maxX = 600;
            maxY = 400;
            maxZ = 200;
        } else if (model == 3) {
            maxX = 400;
            maxY = 800;
            maxZ = 200;
        } else if (model == 4) {
            maxX = 400;
            maxY = 800;
            maxZ = 400;
        }
    }


    public void initFluid(Fluid3D fluid) {
        int step = 20;
        if (model == 0) {
            for (int x = (int) (maxX * 0.4); x < maxX * 0.8; x += step)
                for (int y = (int) (maxY * 0.0); y < maxY * 0.4; y += step)
                    for (int z = (int) (maxZ * 0.2); z < maxZ * 0.8; z += step)
                        fluid.addParticle(new Vector3D(x, y, z));


            for (int x = (int) 0; x < maxX * 1; x += step)
                for (int y = (int) (maxY * 0.6); y < maxY * 1; y += step)
                    for (int z = (int) (maxZ * 0.5); z < maxZ * 1; z += step)
                        fluid.addParticle(new Vector3D(x, y, z));
        } else if (model == 1) {
            for (int x = (int) (maxX * 0.0); x < maxX * 0.3; x += step)
                for (int y = (int) (maxY * 0.2); y < maxY * 1; y += step)
                    for (int z = (int) (maxZ * 0.2); z < maxZ * 0.8; z += step)
                        fluid.addParticle(new Vector3D(x, y, z));
        } else if (model == 2) {
            for (int x = (int) (maxX * 0.0); x < maxX * 0.4; x += step)
                for (int y = (int) (maxY * 0.4); y < maxY * 1; y += step)
                    for (int z = (int) (maxZ * 0); z < maxZ * 0.4; z += step)
                        fluid.addParticle(new Vector3D(x, y, z));
            for (int x = (int) (maxX * 0.8); x < maxX * 1; x += step)
                for (int y = (int) (maxY * 0); y < maxY * 1; y += step)
                    for (int z = (int) (maxZ * 0.7); z < maxZ * 1; z += step)
                        fluid.addParticle(new Vector3D(x, y, z));
        } else if (model == 3) {
            for (int x = (int) (maxX * 0.3); x < maxX * 0.7; x += step)
                for (int y = (int) (maxY * 0.0); y < maxY * 0.4; y += step)
                    for (int z = (int) (maxZ * 0.4); z < maxZ * 0.7; z += step)
                        fluid.addParticle(new Vector3D(x, y, z));
            fluid.addFixedRigidBody(new Vector3D(0.5 * maxX, 0.7 * maxY, 0.5 * maxZ), 150);
            fluid.addFixedRigidBody(new Vector3D(0.3 * maxX, 0.8 * maxY, 0.6 * maxZ), 120);
            fluid.addFixedRigidBody(new Vector3D(0.4 * maxX, 0.7 * maxY, 0.9 * maxZ), 100);
        } else if (model == 4) {
            fluid.gravity = new Vector3D(0, 0, 0);
            for (int x = (int) (maxX * 0.3); x < maxX * 1; x += step)
                for (int y = (int) (maxY * 0.2); y < maxY * 0.4; y += step)
                    for (int z = (int) (maxZ * 0.4); z < maxZ * 0.7; z += step)
                        fluid.addParticle(new Vector3D(x, y, z));

            for (int x = (int) (maxX * 0.7); x < maxX * 0.9; x += step)
                for (int y = (int) (maxY * 0.5); y < maxY * 0.7; y += step)
                    for (int z = (int) (maxZ * 0.6); z < maxZ * 1; z += step)
                        fluid.addParticle(new Vector3D(x, y, z));

            for (int x = (int) (maxX * 0.4); x < maxX * 0.6; x += step)
                for (int y = (int) (maxY * 0.5); y < maxY * 1; y += step)
                    for (int z = (int) (maxZ * 0.0); z < maxZ * 0.4; z += step)
                        fluid.addParticle(new Vector3D(x, y, z));

            for (int x = (int) (maxX * 0); x < maxX * 1; x += step)
                for (int y = (int) (maxY * 0.42); y < maxY * 0.43; y += step)
                    for (int z = (int) (maxZ * 0.0); z < maxZ * 1; z += step)
                        fluid.addParticle(new Vector3D(x, y, z));
        }
    }

    public int getHeight() {
        return maxY;
    }

    public int getLength() {
        return maxZ;
    }

    public int getWidth() {
        return maxX;
    }
}
