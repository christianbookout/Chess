/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package newchess;

import static newchess.ChessMove.*;
import static newchess.Position.positionsEqual;

/**
 *
 * @author 1554306
 */

//import static chess.ChessTile.host;
/*******************
    * CHESS PIECES *
    ****************
    * Each chess piece extends
    * this main class, implementing
    * its methods. It inherits
    * base traits of the class, 
    * which every chess piece has.
    * For example, every chess piece
    * moves and captures other pieces.
    */

  public abstract class ChessPiece {   
      
    //Whether or not the chess piece is "owned" by the player
    private boolean playerOwned = false;
    
    
    public void isAlly(boolean torf) {
        playerOwned = torf;
    }
    
    public boolean isAlly() {
        return playerOwned;
    }
    
    private boolean isWhite = true;
    public void isWhite(boolean torf) {
        isWhite = torf;
    }
    public boolean isWhite() {
        return isWhite;
    }
    
    //The title of the chess piece, for example "Pawn". getName() used mostly for debugging. 
    private String name = "Blank";
    public String getName() {
        return name;
    }
    
    //Sets the name of the chess piece. Used for initializing the board and rewriting. 
    public void setName(String n) {
        name = n;
    }
    
    /*
    Function:
        Attempts to see if you can move from the specified location to the specified location
    Inputs:
        - chessBoard representing the chess tiles 
        - newPos representing the end position
        - currPos representing the starting position
    Outputs:
        ChessMove showing special move, normal move, or none. 
    */
    public abstract ChessMove moveHere(ChessTile[][] chessBoard, Position newPos, Position currPos);
    
    /*
    Function:
        Attempts to see if you can attack a piece at the specified location
    Inputs:
        - chessBoard representing the chess tiles 
        - newPos representing the end position
        - currPos representing the starting position
    Outputs:
        - True if you can attack 
        - False if you cannot
    */
    //public abstract boolean attackHere(ChessTile[][] chessBoard, Position newPos, Position currPos, SpecialMove specialMove);
    
}

class Pawn extends ChessPiece {
    public boolean hasMoved;
    
    //The turn at which you move two squares as a pawn. This is used for en passanting
    public int pawnDoubleJumpTurnNumber;
    
    public Pawn(){
        hasMoved = false;
        pawnDoubleJumpTurnNumber = -1;
        super.setName("Pawn");
    }
    
    @Override 
    public ChessMove moveHere(ChessTile[][] chessBoard, Position newPos, Position currPos) { 
        
        int verticalChange = Math.abs(newPos.y - currPos.y);
        int horizontalChange = newPos.x - currPos.x;
        
        int verticalLimit = 1;
        
        //If you haven't moved yet then you are able to move 2 spaces instead of 1. 
        if (!hasMoved) {
            verticalLimit = 2;
        }
        
        ChessPiece moveToPiece = chessBoard[newPos.y][newPos.x].getPiece();
        
        //If the path your location is completely blank 
        boolean movePosIsBlank = (moveToPiece instanceof Blank && verticalChange == 1)  || (verticalChange == 2 && chessBoard[currPos.y - 1][currPos.x].getPiece() instanceof Blank);
        
        //If the piece ahead is blank and you clicked to a piece you can actually move to then return true
        if (movePosIsBlank && verticalChange <= verticalLimit && horizontalChange == 0){
            //if (Math.abs(verticalChange) == 2 && hasMoved == false) {
                //pawnDoubleJumpTurnNumber = ChessGame.getTurnNumber(); //TODO this will be a problem once i start king checkin 
                //System.out.println("Setting turn number to " + pawnDoubleJumpTurnNumber);
            //}
            if (newPos.y == 0) {
                return PROMOTION;
            }
            return NORMAL;
        } else {
            return NONE;
        }
    }
    
    /*@Override
    public boolean attackHere(ChessTile[][] chessBoard, Position newPos, Position currPos, SpecialMove specialMove) {
        
        int horizontalChange = Math.abs(newPos.x - currPos.x);
        int verticalChange = newPos.y - currPos.y;
        
        ChessPiece moveToPiece = chessBoard[newPos.y][newPos.x].getPiece();
        
        //Passant piece is the piece to the left or right of your current piece, which is also the piece below the click position. 
        ChessPiece passantPiece = chessBoard[newPos.y+1][newPos.x].getPiece();
        if (passantPiece instanceof Pawn) {
            //System.out.println(((Pawn) passantPiece).pawnDoubleJumpTurnNumber + " pawn double jump turn number, current number:  " + ChessGame.getTurnNumber() + " horizontal & vertical cahnge: " + horizontalChange + " " + verticalChange);
        }
        if (positionsEqual(newPos, currPos)) {
            return false;
        } 
        //If the passant piece is an enemy, it's a pawn, it just double jumped, and it's you are clicking on the right spot then return true
        else if (!passantPiece.isAlly() && passantPiece instanceof Pawn && ((Pawn) passantPiece).pawnDoubleJumpTurnNumber == ChessGame.getTurnNumber() - 1 && horizontalChange == 1 && verticalChange == -1) {
            specialMove.setMove(ChessMove.EN_PASSANT);
            return true;
        }
        else if (verticalChange == -1 && horizontalChange == 1 && !moveToPiece.isAlly() && !(moveToPiece instanceof Blank)) {
            if (newPos.y == 0) {
                specialMove.setMove(ChessMove.PROMOTION);
            }
            //System.out.println(" special move from pawn attackHere " + specialMove.toString());
            return true;
        }
        
        return false;
    }*/
}
class Rook extends ChessPiece{
    public Rook() {
        super.setName("Rook");
    }
    public boolean hasMoved = false;
    @Override
    public ChessMove moveHere(ChessTile[][] chessBoard, Position newPos, Position currPos) {
        
        //boolean isBlank = chessBoard[newPos.y][newPos.x].getPiece() instanceof blank;
        
        //You're correctly moving 
        if (movingCorrect(currPos, newPos) && searchForPiece(chessBoard, newPos, currPos, currPos)) {
            return NORMAL;
        } 
        //You're moving incorrectly
        return NONE;
    }
    
    /*@Override
    public boolean attackHere(ChessTile[][] chessBoard, Position newPos, Position currPos, SpecialMove specialMove) {
        
        //boolean isAlly = chessBoard[newPos.y][newPos.x].getPiece().isAlly();
        //boolean isBlank = chessBoard[newPos.y][newPos.x].getPiece() instanceof blank;
        
        //You're moving correctly
        if (movingCorrect(currPos, newPos)) {
            return searchForPiece(chessBoard, newPos, currPos, currPos);
        } 
        //You are moving incorrectly 
        else return false;
    }*/
    //Determines whether or not the movement is proper for a rook
    private boolean movingCorrect(Position currPos, Position newPos) {
        return (currPos.x == newPos.x && !(newPos.y == currPos.y)) || (currPos.y == newPos.y && !(newPos.x == currPos.x));
    }
    
    /*
    Function:
        Walks across the board in a straight line depending on whether the new piece is to the left, right, above, or below the current piece.
        Recursively searches each piece to make sure the squares up to your location are not occupied. 
    Inputs:
        - chessBoard variable representing each chess tile
        - newPos representing the position attempting to be moved to
        - searchPos representing the position the recursion searches
        - originalPos representing the starting point
    Outputs:
        - True if you can move
        - False if you cannot
    */
    private boolean searchForPiece(ChessTile[][] chessBoard, Position newPos, Position sp, Position originalPos) { 
                                                                                         
        ChessPiece search = chessBoard[sp.y][sp.x].getPiece();
        
        //Otherwise it would directly change the original search position which was causing bugs
        Position searchPos = new Position(sp.x, sp.y);
        
        //If the piece that the search function is currently selecting is not a blank piece then return false, as something is blocking the rook's way
        if ((!(search instanceof Blank) && !positionsEqual(searchPos, newPos)) && !positionsEqual(originalPos, searchPos)) {
            return false;
        } 
        //If the tile has been reached, and the rook can move there
        else if ((search instanceof Blank || (!search.isAlly() && this.isAlly())) && positionsEqual(searchPos, newPos)) {
            return true;
        } 
        //The search function will recursively run until it reaches the specified position.
        else {
            //Setting search position one downwards
            if (searchPos.x == originalPos.x && searchPos.y < newPos.y) {
                searchPos.y++;
            } 
            //Setting search position one upwards
            else if (searchPos.x == originalPos.x && searchPos.y > newPos.y) {
                searchPos.y--;
            } 
            //Setting search position one to the right
            else if (searchPos.y == originalPos.y && searchPos.x < newPos.x) {
                searchPos.x++;
            } 
            //Setting search position one to the left
            else if (searchPos.y == originalPos.y && searchPos.x > newPos.x){
                searchPos.x--;
            }
            //Continue searching. 
            return searchForPiece(chessBoard, newPos, searchPos, originalPos);
            
        }
    }

}
class Knight extends ChessPiece{
    
    public Knight() {
        super.setName("Knight");
    }
    @Override
    public ChessMove moveHere(ChessTile[][] chessBoard, Position newPos, Position currPos) {
        int horizontalChange = Math.abs(newPos.x - currPos.x);
        int verticalChange = (Math.abs(newPos.y - currPos.y));
        //Is the piece you're trying to move to blank? 
        boolean isBlank = chessBoard[newPos.y][newPos.x].getPiece() instanceof Blank;
        
        if (isBlank && movingCorrect(verticalChange, horizontalChange)) {
            return NORMAL;
        } else return NONE; 
    }

/*    @Override
    public boolean attackHere(ChessTile[][] chessBoard, Position newPos, Position currPos, SpecialMove specialMove) {
        //If you try to click on your current location, return false
        if (newPos.x == currPos.x && newPos.y == currPos.y) {
            return false;
        }
        int horizontalChange = Math.abs(newPos.x - currPos.x);
        int verticalChange = (Math.abs(newPos.y - currPos.y));
        
        //Is the piece you're tryna attack an enemy? 
        boolean notAlly = !(chessBoard[newPos.y][newPos.x].getPiece().isAlly());
        
        //:-) 
        return  notAlly && movingCorrect(verticalChange, horizontalChange);
    }*/
    private boolean movingCorrect(int verticalChange, int horizontalChange) {
        return (verticalChange == 2 && horizontalChange == 1) || (verticalChange == 1 && horizontalChange == 2);
    }
}

class Bishop extends ChessPiece{
    public Bishop() {
        super.setName("Bishop");
    }
    
    @Override
    public ChessMove moveHere(ChessTile[][] chessBoard, Position newPos, Position currPos) {
        int horizontalChange = Math.abs(newPos.x - currPos.x);
        int verticalChange = Math.abs(newPos.y - currPos.y);
        
        if (horizontalChange == verticalChange && searchForPiece(chessBoard, newPos, currPos, currPos)) {
            return NORMAL;
        }
        else return NONE;
    }
/*    @Override
    public boolean attackHere(ChessTile[][] chessBoard, Position newPos, Position currPos, SpecialMove specialMove) {
        int horizontalChange = Math.abs(newPos.x - currPos.x);
        int verticalChange = Math.abs(newPos.y - currPos.y);
        
        if (horizontalChange == verticalChange) {
            return searchForPiece(chessBoard, newPos, currPos, currPos);
        }
        return false;
    } */
    
    private boolean searchForPiece(ChessTile[][] chessBoard, Position newPos, Position sp, Position originalPos) {
        ChessPiece search = chessBoard[sp.y][sp.x].getPiece();
        
        int horizontalChange = newPos.x - originalPos.x;
        int verticalChange = newPos.y - originalPos.y;
        
        boolean isBlank = search instanceof Blank;
        
        //Otherwise it would directly change the original search position which was causing bugs
        Position searchPos = new Position(sp.x, sp.y);
        
        //If the piece that the search function is currently selecting is not a blank piece then return false, as something is blocking the bishop's way
        if ((!isBlank && !(positionsEqual(searchPos, newPos))) && !(positionsEqual(searchPos, originalPos))) {
            return false;
        } 
        //If the tile has been reached, and the bishop can move there
        else if (isBlank || ((search.isAlly() && !this.isAlly()) || (!search.isAlly() && this.isAlly())) && (positionsEqual(searchPos, originalPos))) {
            return true;
        } 
        //If the tile has been reached, and the bishop can't move there
        else if ((positionsEqual(searchPos, newPos)) && !isBlank) {
            return false;
        } 
        //The search function will recursively run until it reaches the specified position.
        else {
            //Setting search position one up-right
            if (horizontalChange == -verticalChange && verticalChange < 0) {
                searchPos.x++;
                searchPos.y--;
            } 
            //Setting search position one up-left
            else if (horizontalChange == verticalChange && verticalChange < 0) {
                searchPos.x--;
                searchPos.y--;
            } 
            //Setting search position one down-right
            else if (horizontalChange == verticalChange && verticalChange > 0) {
                searchPos.x++;
                searchPos.y++;
            } 
            //Setting search position one down-left
            else {
                searchPos.x--;
                searchPos.y++;
            }
            return searchForPiece(chessBoard, newPos, searchPos, originalPos);
        }
    }
}
class Queen extends ChessPiece{
    public Queen() {
        super.setName("Queen");
    }
    @Override
    public ChessMove moveHere(ChessTile[][] chessBoard, Position newPos, Position currPos) {
        int horizontalChange = Math.abs(newPos.x - currPos.x);
        int verticalChange = Math.abs(newPos.y - currPos.y);
        if (chessBoard[newPos.y][newPos.x].getPiece() instanceof Blank 
                && (horizontalChange == verticalChange 
                || ((currPos.x == newPos.x && !(newPos.y == currPos.y)) 
                || (currPos.y == newPos.y && !(newPos.x == currPos.x))))
                && searchForPiece(chessBoard, newPos, currPos, currPos)) {
            return NORMAL;
        } 
        else return NONE;
    }
    private boolean searchForPiece(ChessTile[][] chessBoard, Position newPos, Position sp, Position originalPos) {
        int horizontalChange = newPos.x - sp.x;
        int verticalChange = newPos.y - sp.y;
        
        ChessPiece search = chessBoard[sp.y][sp.x].getPiece();
        
        //Otherwise it would directly change the original search position which was causing bugs
        Position searchPos = new Position(sp.x, sp.y);
        
            
        //If the piece that the search function is currently selecting is not a blank piece then return false, as something is blocking the queen's way
        if (!(search instanceof Blank) && !positionsEqual(searchPos, newPos) && !positionsEqual(searchPos, originalPos)) {
            return false;
        }
        //If the tile has been reached, and the queen can move there or attack then return true
        else if (positionsEqual(searchPos, newPos) && (search instanceof Blank || (!search.isAlly() && !(search instanceof Blank)))) {
            return true;
        } 
        //The search function will recursively run until it reaches the specified position.
        else {
            //Setting search position one up-right
            if (-horizontalChange == verticalChange && verticalChange < 0) { 
                searchPos.x++;
                searchPos.y--;
            } 
            //Setting search position one up-left
            else if (horizontalChange == verticalChange && verticalChange < 0) { //broken
                searchPos.x--;
                searchPos.y--;
            } 
            //Setting search position one down-right
            else if (horizontalChange == verticalChange && verticalChange > 0) {  //broken
                searchPos.x++;
                searchPos.y++;
            } 
            //Setting search position one down-left
            else if (horizontalChange == -verticalChange && verticalChange > 0 ){
                searchPos.x--;
                searchPos.y++;
            }
            //Setting search position one downwards
            else if (searchPos.x == originalPos.x && searchPos.y < newPos.y) {
                searchPos.y++;
            } 
            //Setting search position one upwards
            else if (searchPos.x == originalPos.x && searchPos.y > newPos.y) {
                searchPos.y--;
            } 
            //Setting search position one to the right
            else if (searchPos.y == originalPos.y && searchPos.x < newPos.x) {
                searchPos.x++;
            } 
            //Setting search position one to the left
            else if (searchPos.y == originalPos.y && searchPos.x > newPos.x){
                searchPos.x--;
            }
            
            return searchForPiece(chessBoard, newPos, searchPos, originalPos);
        }
    }
/*    @Override
    public boolean attackHere(ChessTile[][] chessBoard, Position newPos, Position currPos, SpecialMove specialMove) {
        int horizontalChange = Math.abs(newPos.x - currPos.x);
        int verticalChange = Math.abs(newPos.y - currPos.y);
        if (!(chessBoard[newPos.y][newPos.x].getPiece() instanceof Blank)
                && !chessBoard[newPos.y][newPos.x].getPiece().isAlly()
                && (horizontalChange == verticalChange 
                || ((currPos.x == newPos.x && !(newPos.y == currPos.y)) 
                || (currPos.y == newPos.y && !(newPos.x == currPos.x))))) {
            return searchForPiece(chessBoard, newPos, currPos, currPos);
        } 
        return false;
    */
}

class King extends ChessPiece{
    public King() {
        super.setName("King");
    }
    public boolean hasMoved = false;
    @Override
    public ChessMove moveHere(ChessTile[][] chessBoard, Position newPos, Position currPos) {
        
        int verticalChange = Math.abs(newPos.y - currPos.y);
        int horizontalChange = Math.abs(newPos.x - currPos.x);
        ChessPiece rook = chessBoard[newPos.y][newPos.x].getPiece();
        
        if (rook.isAlly() && rook instanceof Rook &&  ((Rook)rook).hasMoved == false && this.hasMoved == false) {
            //TODO only allow if the castle doesn't put the king in check and the king already isn't in check
            return CASTLE;
        }
        else if (chessBoard[newPos.y][newPos.x].getPiece() instanceof Blank && (verticalChange + horizontalChange == 1 || (verticalChange == 1 && horizontalChange == 1))) {
            return NORMAL;
        }
        else return NONE;
    }

/*    @Override
    public boolean attackHere(ChessTile[][] chessBoard, Position newPos, Position currPos, SpecialMove specialMove) {
        
        int verticalChange = Math.abs(newPos.y - currPos.y);
        int horizontalChange = Math.abs(newPos.x - currPos.x);
        
        if (chessBoard[newPos.y][newPos.x].getPiece() instanceof Blank && (verticalChange + horizontalChange == 1 || (verticalChange == 1 && horizontalChange == 1))) {
            return true; 
        }
        else return false;
    }*/
}