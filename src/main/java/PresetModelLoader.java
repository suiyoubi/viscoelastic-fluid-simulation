import processing.core.PVector;

import java.io.*;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class PresetModelLoader {

    private final int width;
    private final int height;
    private final List<PVector> particlePositions;
    private final Map<PVector, Float> fixedRBPositions;
    private final Map<PVector, Float> movedRBPositions;
    private BufferedReader bufferedReader;

    public PresetModelLoader(int width, int height) {
        this.width = width;
        this.height = height;
        this.particlePositions = new LinkedList<>();
        this.fixedRBPositions = new HashMap<>();
        this.movedRBPositions = new HashMap<>();
        try {
            InputStream inputStream = getClass().getClassLoader().getResourceAsStream("I-Love-CSC-2549.txt");
            this.bufferedReader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    public void initialize(int model) {
        if(model == 0) {
            // Two water with No rigid body
            addRectWater(width * 0.2, width * 0.6, height * 0.4, height * 0.6);
            addRectWater(0, width, height * 0.8, height);
        } else if(model == 1) {
            // Two water with Movable rigid body
            addRectWater(width * 0.2, width * 0.6, height * 0.4, height * 0.6);
            addRectWater(0, width, height * 0.8, height);

            this.movedRBPositions.put(new PVector(100, 100), 30F);
        } else if(model == 2) {
            // Three water with Non-movable rigid body
            addRectWater(width*0.2, width*0.4, height * 0.1, height * 0.25);
            addRectWater(width * 0.2, width * 0.6, height * 0.4, height * 0.6);
            addRectWater(0, width, height * 0.8, height);

            this.fixedRBPositions.put(new PVector(100, 100), 30F);
            this.fixedRBPositions.put(new PVector(150, 220), 30F);
            this.fixedRBPositions.put(new PVector(250, 200), 20F);
        } else if (model == 3) {
            // I Love 2549
            addRectWater(width * 0.2, width * 0.6, height * 0.4, height * 0.6);
            addRectWater(0, width, height * 0.8, height);

            String line = null;
            try {
                line = bufferedReader.readLine();
                while(line != null) {
                    String[] s = line.split(" ");
                    this.fixedRBPositions.put(new PVector(Float.parseFloat(s[0]), Float.parseFloat(s[1])), 5F);
                    line = bufferedReader.readLine();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (model == 4) {
            // a lot of water cube
            addRectWater(width * 0.2, width * 0.4, height * 0.2, height * 0.4);
            addRectWater(width * 0.6, width * 0.8, height * 0.2, height * 0.4);
            addRectWater(width * 0.3, width * 0.5, height * 0.7, height * 0.9);
            addRectWater(width * 0.6, width * 0.8, height * 0.4, height * 0.6);
            addRectWater(width * 0.9, width * 1.0, height * 0.8, height * 0.9);
            addRectWater(width * 0.8, width * 0.9, height * 0.4, height * 0.6);
            addRectWater(width * 0.4, width * 0.5, height * 0, height * 0.15);

            this.fixedRBPositions.put(new PVector(250, 200), 20F);
        }
    }

    private void addRectWater(double x1, double x2, double y1, double y2) {
        for (int x = (int)x1; x < (int)x2; x += 5)
            for (int y = (int)y1; y < (int)y2; y += 5)
                particlePositions.add(new PVector(x, y));
    }

    public List<PVector> getParticlePositions() {
        return particlePositions;
    }

    public Map<PVector, Float> getFixedRBPositions() {
        return fixedRBPositions;
    }

    public Map<PVector, Float> getMovedRBPositions() {
        return movedRBPositions;
    }
}
