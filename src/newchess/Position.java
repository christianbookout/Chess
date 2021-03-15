/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package newchess;

/**
 *
 * @author chris
 */
public class Position {
    public int x;
    public int y;
    public Position (int xPos, int yPos) {
        x = xPos;
        y = yPos;
    }
    public static boolean positionsEqual(Position pos1, Position pos2) {
        return pos1.x == pos2.x && pos1.y == pos2.y;
    }
    public boolean equals(Position pos) {
        return x == pos.x && y == pos.y;
    }
}
