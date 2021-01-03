public class Toggle {
    public static final int toggleColor = 50;

    private final String description;
    private final int x1;
    private final int x2;
    private final int y1;
    private final int y2;
    private final int lineX1;
    private final int lineX2;
    private final int lineY;
    private final Simulation simulation;
    private final int pointY;
    private int pointX;

    public Toggle(String description, float percentage, int x1, int y1, int x2, int y2, int offset1, int offset2, Simulation simulation) {
        this.description = description;
        this.x1 = x1;
        this.x2 = x2;
        this.y1 = y1;
        this.y2 = y2;
        this.simulation = simulation;
        this.lineX1 = x1 + offset1;
        this.lineX2 = x2 - offset2;
        this.lineY = y1 + (y2 - y1) / 2;
        pointX = x1 + offset1 + (int) (percentage * (this.lineX2 - this.lineX1));
        pointY = y1 + (y2 - y1) / 2;
    }

    public void drawToggle() {
        this.simulation.fill(toggleColor);
        this.simulation.color(255);
        this.simulation.text(description, x1, y1, x2, y2);
        this.simulation.strokeWeight(2);
        this.simulation.line(lineX1, lineY, lineX2, lineY);
        this.simulation.strokeWeight(10);
        this.simulation.point(pointX, pointY);
        this.simulation.text((int) (percentage() * 100) + "%", x1 - 55, y1, x2, y2);
    }

    public void checkChange() {
        if (this.simulation.mouseX > x1 && this.simulation.mouseX < x2 && this.simulation.mouseY > y1 && this.simulation.mouseY < y2) {
            this.pointX = this.simulation.mouseX;
            this.pointX = Math.max(pointX, lineX1);
            this.pointX = Math.min(pointX, lineX2);
        }
    }

    public float percentage() {
        return (float) (this.pointX - this.lineX1) / (this.lineX2 - this.lineX1);
    }
}
