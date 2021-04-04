
package newchess;

public class Blank extends ChessPiece {
    
    public Blank() {
        super.setName("Blank");
        super.isAlly(false);
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
