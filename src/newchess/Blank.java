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
public class Blank extends ChessPiece {
    
    public Blank() {
        super.setName("Blank");
    }

    @Override
    public ChessMove moveHere(ChessTile[][] chessBoard, Position newPos, Position currPos) {
        System.err.println("Trying to move as a blank piece");
        return ChessMove.NONE;
    }

    /*@Override
    public boolean attackHere(ChessTile[][] chessBoard, Position newPos, Position currPos, SpecialMove specialMove) {
        System.err.println("Trying to attack as a blank piece");
        return false;
    }*/
}
