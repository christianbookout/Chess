
package newchess;

public class Position { //TODO maybe make this comparable so it's nicer
    protected int x;
    protected int y;
    public Position (int xPos, int yPos) {
        x = xPos;
        y = yPos;
    }
    
    public String toString() {
        return "x: " + x + ", y: " +  y;
    }
    
    public boolean equals(Position pos) {
        return x == pos.x && y == pos.y;
    }
}
