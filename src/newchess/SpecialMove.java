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
public class SpecialMove {
    private ChessMove thisMove;
    public static enum ChessMove{
        NONE,
        CASTLE,
        PROMOTION,
        EN_PASSANT;
    }
    public SpecialMove(ChessMove move) {
        thisMove = move;
    }
    public void setMove(ChessMove toMove) {
        thisMove = toMove;
    }
    public ChessMove getMove() {
        return thisMove;
    }
}
