/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package newchess;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import static newchess.ChessBoardWindow.CHESSBOARD_HEIGHT;
import static newchess.ChessBoardWindow.CHESSBOARD_WIDTH;
import static newchess.ChessMove.*;

/**
 *
 * @author chris
 */
public class ChessGame {
    
    //A 2D array of each individual chess tile. Height x Width. 0,0 represents the top-left and 7,7 represents the bottom-right.
    private static final ChessTile[][] CHESS_TILES = new ChessTile[CHESSBOARD_HEIGHT][CHESSBOARD_WIDTH]; 
    
    private static ChessBoardWindow CHESS_WINDOW;
    
    private static boolean alliedTurn; 
    
    private static int turnNumber;
    public static int getTurnNumber() {return turnNumber;}
    
    private static Socket socket;
    
    public static void initializeGame (boolean colour, Socket s) {
        //Open chess board and initialize it to set the CHESS_TILES.
        CHESS_WINDOW = new ChessBoardWindow(colour);
        CHESS_WINDOW.open();
        CHESS_WINDOW.initializeBoard(CHESS_TILES);
        ReadThread r = new ReadThread(s);
        r.start();
        WriteThread w = new WriteThread(s);
        w.start();
        socket = s;
        alliedTurn = colour; 
    }
    private static ChessTile selectedTile;
    /*
    Function:
        Calls whenever a tile is clicked from the ChessTile class. Handles a "click" event. Behaves depending on the piece clicked.
    */
    public static void clickTile(ChessTile tileClicked) {
        //TODO: Make a premove and/or chess pieces highlight if you can move to em 
        
        //Piece just clicked (not selected piece necessarily)
        ChessPiece piece = tileClicked.getPiece();
        
        //SpecialMove specialMove = new SpecialMove(ChessMove.NONE);
        //specialMove.setMove(ChessMove.NONE);
        
        //If it isn't your turn to move then you can't do anything.
        if (!alliedTurn) {
            return;
        }
        /*
            -If there is no currently selected tile, and you clicked on a non-blank piece, and you can select the piece, then do so.
            -If there is no currently selected tile and you can't select the piece then return (as you're just clicking pointlessly). 
        */
        else if (selectedTile == null && (!(piece instanceof Blank) || !piece.isAlly())) {
            //If the current piece is on your team then you can select the piece you clicked.
            if (piece.isAlly()) {
                selectedTile = tileClicked;
                selectedTile.highlight();
            }
            return;
        }
        //If the selected piece still doesn't exist (like at the start of the game), then just return bc you can't do anything else. 
        else if (selectedTile == null) {
            return;
        }
        //If you could possibly castle (because you selected a king and clicked on a rook) 
        else if (selectedTile.getPiece() instanceof King && tileClicked.getPiece() instanceof Rook && tileClicked.getPiece().isAlly()) {
            if (selectedTile.getPiece().moveHere(CHESS_TILES, tileClicked.getPosition(), selectedTile.getPosition()) == CASTLE) {
                castle(selectedTile, tileClicked);
                sendMove(selectedTile, tileClicked.getPosition(), ChessMove.CASTLE);
                return;
                
            }
        }
        //If you click on an allied piece and already have a selected piece then select that allied piece you just clicked
        else if (selectedTile != null && piece.isAlly() && !(piece instanceof Blank)) {
            selectedTile.unhighlight();
            selectedTile = tileClicked;
            selectedTile.highlight();
        }
        //If you click on the same piece you have selected then return 
        else if (tileClicked.getButton() == selectedTile.getButton() && !(selectedTile.getPiece() instanceof Blank)) {
            return;
        } 
        //All of the return situations passed, so that means you can theoretically attack or move (at least by each piece's standard). 
        else {
            selectedTile.unhighlight();
            ChessMove move = selectedTile.getPiece().moveHere(CHESS_TILES, tileClicked.getPosition(), selectedTile.getPosition());
            if (move != NONE) 
            {
                //If the piece can't move bc it would put the king in check then "deselect" the piece and return :-) 
                if (!canMove(selectedTile)) {
                    selectedTile = null;
                    return;
                }
                
                System.out.println("Moving " + selectedTile.getPiece().getName() + " to " + tileClicked.getPosition().x + ", " + tileClicked.getPosition().y + " with move " + move.toString());
                switch (move) {
                    case NONE:
                        movePiece(selectedTile, tileClicked.getPosition());
                        break;
                    case CASTLE:
                        //Already handled castling above.
                        System.err.println("Trying to castle after castle condition was already checked for some reason??");
                        break;
                    case PROMOTION:
                        pawnPromotion(selectedTile, tileClicked.getPosition(), new Queen()); //TODO: Make promotion with not just queen 
                        break;
                    case EN_PASSANT:
                        enPassant(selectedTile, tileClicked.getPosition());
                        break;
                }
                sendMove(selectedTile, tileClicked.getPosition(), move);
                //TODO set hasmoved to true for necessary pieces
                //TODO check for stalemate 

            }
            //If you've made it this far then you've completed a move or clicked elsewhere. Selected piece is null now 
            selectedTile = null;
        }
        
    }
    private static boolean canSaveKingFromCheck(ChessTile tile) {
        //TODO
        ChessTile kingTile = findKingTile(tile.getPiece().isAlly());
        ChessTile[] piecesAttackingKing = isUnderAttack(kingTile);
        
        
        return false;
    }
    /*
    Function:
        Tests to see if a chess tile can move w/o putting the king in danger. 
            -If a tile is not under attack, then it can move safely. 
            -If a tile is only under attack by a knight, pawn, or king (either assumed to not already have the king in check) 
            -If a tile is under attack by a bishop, queen, or rook and the path from the bishop, queen, or king that leads to the current piece 
    */
    private static boolean canMove(ChessTile tile) { 
        //Take the position of the attacking piece and the current piece (for example 0,0 and 3,3) and take the separation between the two (-3, -3) and divide by the absolute value (-1, -1) 
        //and then check if the king's position is some multiple of that and if it is then check each piece from the piece being attacked to the king and see if there are any pieces blocking 
        //that distance and if there are then the piece can move and if there aren't then the piece cannot move
        ChessTile kingTile = findKingTile(tile.getPiece().isAlly());
        
        //Checking king in check
        //if (piecesAttackingKing.length == 0) return true;
        
        ChessTile[] piecesAttackingTile = isUnderAttack(tile);
        
        //Checking to make sure the piece being checked is under attack
        if (piecesAttackingTile.length == 0)  return true;
        
        //Checking to see if the pieces attacking the king are only pawns, rooks, and/or a king.
        int counter = 0;
        ChessPiece p;
        for (ChessTile t : piecesAttackingTile) {
            p = t.getPiece();
            if (p instanceof Pawn || p instanceof Rook || p instanceof King) {
                counter++; 
            }
        }
        if (counter == piecesAttackingTile.length) return true;
        
        //Checking to see if the move would put the king in check (bc the piece is defending the king)
        counter = 0;
        for (ChessTile t : piecesAttackingTile) {
            p = t.getPiece();
            Position posChange = new Position(t.getPosition().x - tile.getPosition().x, t.getPosition().y - tile.getPosition().y);
            Position posToKingChange = new Position(t.getPosition().x - kingTile.getPosition().x, t.getPosition().y - kingTile.getPosition().y);
            
            Position posSign = new Position(0, 0);
            if (posChange.x != 0) {
                posSign.x = posChange.x / Math.abs(posChange.x);
            } 
            if (posChange.y != 0) {
                posSign.y = posChange.y / Math.abs(posChange.y);
            }
            
            Position kingPosSign = new Position(0, 0);
            if (posToKingChange.x != 0) {
                kingPosSign.x = posToKingChange.x / Math.abs(posToKingChange.x);
            } 
            if (posToKingChange.y != 0) {
                kingPosSign.y = posToKingChange.y / Math.abs(posToKingChange.y);
            }
            
            if (p instanceof Pawn || p instanceof Rook || p instanceof King) {
                counter++; 
            } 
            //The king isn't in the line of sight of the other piece.
            else if (!Position.positionsEqual(kingPosSign, posSign)) {
                counter++;
            }
            
            //else if (p instanceof Rook) {
            
            //The last check: to see if there are any pieces in the way.
                ChessTile currTile = CHESS_TILES[tile.getPosition().x + posSign.x][tile.getPosition().y + posSign.y];
                while (true) {
                    //if (currTile has reached destination) {
                    
                    //}
                    /*else */if (!(currTile.getPiece() instanceof Blank)) {
                        counter++;
                        break;
                    }
                    currTile = CHESS_TILES[tile.getPosition().x + posSign.x][tile.getPosition().y + posSign.y];
                }
            //} 
            /*else if (p instanceof Bishop) {
                
            } 
            else if (p instanceof Queen) {
                
            } */
        }
        if (counter == piecesAttackingTile.length) return true;
        
        return false;
    }
    private static void movePiece(ChessTile tile, Position toPosition) {
        if (tile.getPiece() instanceof Pawn && Math.abs(tile.getPosition().y - toPosition.y) == 2) {
            ((Pawn) tile.getPiece()).pawnDoubleJumpTurnNumber = ChessGame.getTurnNumber();
            ((Pawn) tile.getPiece()).hasMoved = true;
        } 
        else if (tile.getPiece() instanceof Rook) {
            ((Rook) tile.getPiece()).hasMoved = true;
        } 
        else if (tile.getPiece() instanceof Pawn ) {
            ((Pawn) tile.getPiece()).hasMoved = true;
        } 
        else if (tile.getPiece() instanceof King) {
            ((King) tile.getPiece()).hasMoved = true;
        } //TODO Make this cleaner please 
        
        CHESS_TILES[toPosition.y][toPosition.x].setPiece(tile.getPiece(), tile.getPiece().isWhite(), tile.getPiece().isAlly());
        
        tile.setPiece(new Blank(), false, false);
        tile.getButton().setIcon(null);
        tile.unhighlight();
        
        endTurn();
    }
    
    //in movepiece make sure to set hasMoved to true for kings, rooks, and pawns 
    public static void receiveMove(String[] decode) {
        
        Position fromPos = new Position(7 - Integer.parseInt(decode[0]), 7 - Integer.parseInt(decode[1]));
        Position toPos = new Position(7 - Integer.parseInt(decode[2]), 7 - Integer.parseInt(decode[3]));
        
        ChessTile fromTile = CHESS_TILES[fromPos.y][fromPos.x];
        ChessTile toTile = CHESS_TILES[toPos.y][toPos.x];
        
        fromTile.getPiece().isAlly(false);
        
        if (decode.length == 4) {
            ChessGame.movePiece(CHESS_TILES[fromPos.y][fromPos.x], toPos);

        } else if (decode.length > 4) {
            switch (decode[4]) {
                case "p": //en passant
                    enPassant(fromTile, toPos);
                    break;
                case "c": //castle
                    castle(fromTile, toTile);
                    break;
                case "+": //promotion
                    pawnPromotion(fromTile, toPos, new Queen());
                    break;
                    
            }

        }

        if (isKingInCheckmate(true)) {
            System.out.println("I'm in checkmate D: ");
            endGame(false, "test");
        }
    }
    /*
    Function:
        Sends move from string s to opponenet's chess game
    Inputs:
        - tile:
        - toPosition:
        - -specialMove:
    */
    private static void sendMove(ChessTile tile, Position toPosition, ChessMove moveType) {
        String move;
        move = tile.getPosition().x + "," + tile.getPosition().y + "," + toPosition.x + "," + toPosition.y;
        
        if (moveType != NONE) {
            switch (moveType) {
                case EN_PASSANT: 
                    move = move.concat(",p");
                    break;
                case CASTLE: 
                    move = move.concat(",c");
                    break;
                case PROMOTION: 
                    move = move.concat(",+");
                    break;
                    
            }
        }
        WriteThread.printMove(move);
    }
    
    private static void castle(ChessTile king, ChessTile rook) {
        
        //+2 or -2 
        int kingMove = 0;
        
        //-3, -2, 2, 3
        int rookMove = 0;
        int xDistance = Math.abs(king.getPosition().x - rook.getPosition().x);
        
        //Rook is on the right of the king and gap is 3 spaces
        if (rook.getPosition().x > king.getPosition().x && xDistance == 4) {
            kingMove = 2;
            rookMove = -3;
        } 
        //Rook is on the right of the king and gap is 2 spaces
        else if (rook.getPosition().x > king.getPosition().x && xDistance == 3) {
            kingMove = 2;
            rookMove = -2;
        } 
        //Rook is on the left of the king and gap is 3 spaces
        else if (rook.getPosition().x < king.getPosition().x && xDistance == 4) {
            kingMove = -2;
            rookMove = 3;
        }
        //Rook is on left of the king and gap is 2 spaces
        else {
            kingMove = -2;
            rookMove = 2;
        }
        
        CHESS_TILES[king.getPosition().y][king.getPosition().x + kingMove].setPiece(king.getPiece());
        king.setPiece(new Blank());
        king.getButton().setIcon(null);
        king.unhighlight();
        
        CHESS_TILES[rook.getPosition().y][rook.getPosition().x + rookMove].setPiece(rook.getPiece());
        rook.setPiece(new Blank());
        rook.getButton().setIcon(null);
        endTurn();
    }
    private static void pawnPromotion(ChessTile fromTile, Position toPosition, ChessPiece promotionPiece) {
        
        CHESS_TILES[toPosition.y][toPosition.x].setPiece(promotionPiece, fromTile.getPiece().isWhite(), fromTile.getPiece().isAlly());
        
        fromTile.setPiece(new Blank());
        endTurn();
        
        /*switch (decode[5]) {
                        case "p": //pawn promotion
                            break;
                        case "r": //rook promotion
                            break;
                        case "q": //queen promotion (only one in use for now)
                            
                            
                            break;
                        case "b": //bishop promotion 
                            break;
                        case "n": //knight promotion
                            break;
                    }
                    break;*/
    }
    public static void enPassant(ChessTile pawn, Position toPosition) {
        CHESS_TILES[toPosition.y][toPosition.x].setPiece(pawn.getPiece());
        CHESS_TILES[pawn.getPosition().y][toPosition.x].setPiece(new Blank());
        pawn.setPiece(new Blank());
        endTurn();
    }
    private static void endTurn() {
        alliedTurn = !alliedTurn;
        turnNumber++;
    }
    /*
    Function:
        Searches every piece on the board to find the king of type "isAlly"
    Input:
        - isAlly: Whether to find the king that is on your side (true) or the king on the opponent's side (false). 
    Output:
        - The king found, null if no king is found which should be never. 
    */
    private static ChessTile findKingTile(boolean isAlly) { //TODO: USE THIS WITH EVERY METHOD
        for (ChessTile[] ct: CHESS_TILES) {
            for (ChessTile t : ct) {
                if (t.getPiece() instanceof King && (t.getPiece().isAlly() == isAlly)) {
                    return t;
                }
            }
        }
        System.err.println("No king found from findKing method");
        return null;
    }
    
    /*
    Function:
        Finds all of the pieces attacking "tile". 
    */
    private static ChessTile[] isUnderAttack(ChessTile tile) { 
        //Check every piece on the board to see if it can attack the specified piece 
        ArrayList<ChessTile> piecesAttackingKing = new ArrayList<>();
        for (ChessTile[] ct: CHESS_TILES) {
            for (ChessTile t : ct) {
                //SpecialMove specialMove = new SpecialMove(ChessMove.NONE);
                //Ignore blank pieces and your allied pieces
                if (t.getPiece() instanceof Blank || t.getPiece().isAlly()) {
                    continue;
                }
                try {
                System.out.println("Testing to see if " + t.getPiece().getName() + " at " + t.getPosition().x + " " + t.getPosition().y + " can attack " + tile.getPiece().getName());
                //If the piece on the board can attack the specified piece by moving there or attacking it
                ChessMove move = t.getPiece().moveHere(CHESS_TILES, t.getPosition(), tile.getPosition());
                if (move != NONE) 
                {
                    //if (specialMove.getMove() == ChessMove.NONE) {
                        piecesAttackingKing.add(t);
                        System.out.println(t.getPiece().getName() + " can attack the king");
                    //}
                    //add to list and continue searching if i end up doing TODO 
                    //TODO make sure that you can en passant to block king in check 
                    // ^^ actually i dont know if this is possible lol 
                } 
                } catch (Exception e){
                    System.err.println(e.getMessage());
                }
            }
        }
        //Return the pieces attacking king as an array of type ChessTile (this is how java does it, kinda wild) 
        return piecesAttackingKing.toArray(new ChessTile[0]);
    }
    
    /*
    Function:
        Makes sure you can move a piece without putting the king in check, or without saving the king from check. 
    */
    //private static boolean canMovePiece(ChessTile fromTile, ChessTile toTile) {
    //    return false;
    //}
    /*
    Function:
        Tests a move to see if it can save the king. Returns true if it can.
    */
    
    //TODO don't use this :)
    private static boolean testMove(ChessTile fromTile, ChessTile toTile) { //TODO: make sure this works (bc pointers are fucked) 
        
        //TODO make sure the chesspiece variables arent changing and breaking stuff
        ChessPiece toTilePiece = toTile.getPiece();
        ChessPiece fromTilePiece = fromTile.getPiece();
        
        toTile.setPiece(fromTile.getPiece());
        fromTile.setPiece(new Blank());
        
        ChessTile kingTile = findKingTile(fromTilePiece.isAlly());
        boolean returnValue = isUnderAttack(kingTile).length == 0;
        
        //TODO make sure this works
        toTile.setPiece(toTilePiece);
        fromTile.setPiece(fromTilePiece);
        
        return returnValue;
    }
    /*
    Function:
        Assuming the king is in check, finds any piece that can move to save the king from mate
    */
    /*
    Ways to save the king from mate: 
        -Attack the piece attacking the king (assuming there is only one piece attacking the king)
        -Move in front of the piece's line of sight (only if the piece is not a horse or pawn) 
    */
    private static boolean canSaveKing(boolean isKingAlly) {
        ChessTile kingTile = findKingTile(isKingAlly);
        ChessTile[] piecesAttackingKing = isUnderAttack(kingTile);
        for (ChessTile[] ct: CHESS_TILES) {
            for (ChessTile t: ct) {
                //Blank pieces and enemy pieces can't save the king
                if (t.getPiece() instanceof Blank || !t.getPiece().isAlly()) continue;
                
                //If there is one piece attacking the king and this piece can kill that piece, then it is possible to save the king. 
                if (piecesAttackingKing.length == 1 && 
                        t.getPiece().moveHere(CHESS_TILES, piecesAttackingKing[0].getPosition(), t.getPosition()) != NONE) {
                    return testMove(t, piecesAttackingKing[0]);
                } 
                //The only way to save the king from a pawn or a knight is by killing it. If the piece attacking the king is a pawn or knight, then you can't save the king. 
                for (ChessTile i : piecesAttackingKing) {
                    if (i.getPiece() instanceof Pawn || i.getPiece() instanceof Knight) {
                        return false;
                    }
                }
                //Search through every other tile to see if the piece can block the view of the piece attacking the king 
                for (ChessTile[] i : CHESS_TILES) {
                    for (ChessTile j : i) {
                        for (ChessTile help : piecesAttackingKing) {
                            //If the piece attacking the king and your selected tile "t" can move to the same position for each of the pieces attacking the king, 
                            //then you can save the king from checkmate by moving to that position.
                            int counter = 0;
                            if (t.getPiece().moveHere(CHESS_TILES, j.getPosition(), t.getPosition()) != NONE
                                && help.getPiece().moveHere(CHESS_TILES, j.getPosition(), t.getPosition()) != NONE) {
                                counter++;
                            }
                            //If your move intercepts each of the pieces attacking the king's views, then it can save the king. 
                            if (counter == piecesAttackingKing.length) {
                                return testMove(t, piecesAttackingKing[0]);
                            }
                        }
                        
                    }
                }
                
            }
        }
        return false;
    }
    /*
    Function:
        Checks to see if the king can move or attack any location to save itself from check (assuming it's in check) 
    */
    private static boolean canKingSaveItself(boolean isKingAlly) {
        ChessTile kingTile = findKingTile(isKingAlly);
        
        for (ChessTile[] ct: CHESS_TILES) {
            for (ChessTile t: ct) {
                //You can't move to your own piece 
                if (t.getPiece().isAlly()) continue;
                
                //You can't move more than 2 spaces away from your king, so no need to even check. 
                if (Math.abs(t.getPosition().x - kingTile.getPosition().x) > 1 || Math.abs(t.getPosition().y - kingTile.getPosition().y) > 1 ) continue;
                
                //SpecialMove specialMove = new SpecialMove(ChessMove.NONE);
                ChessMove move = kingTile.getPiece().moveHere(CHESS_TILES, t.getPosition(), kingTile.getPosition()
                if (move == NORMAL && isUnderAttack(t).length == 0) {
                    //If you can move to a piece that isn't under attack then the king can save itself
                    return true;
                }
                /*//If the king can attack a tile and there are no pieces defending that tile then you can attack there to save yourself.
                else if (kingTile.getPiece().attackHere(CHESS_TILES, t.getPosition(), kingTile.getPosition()) && isUnderAttack(t).length == 0) {
                    return true; 
                }*/
            }
        }
        return false;
    }
    
    private static boolean isKingInCheckmate(boolean isKingAlly) {
        ChessTile kingTile = findKingTile(isKingAlly);
        ChessTile[] piecesAttackingKing = isUnderAttack(kingTile);
        
        //King can't be in checkmate if there are no pieces attacking it 
        if (piecesAttackingKing.length == 0) return false;
        
        //King can't be in checkmate if a piece can save it or it can save itself. 
        else if (canSaveKing(isKingAlly) || canKingSaveItself(isKingAlly)) return false;
        
        //King must be in checkmate if the other conditions didn't pass. 
        return true;
    }
    public static void endGame(boolean allyWon, String endText){
        if (CHESS_TILES[0][0].getButton().isEnabled()) {
            CHESS_WINDOW.CHAT_BOX.append("\n" + endText + "\n");
        } else {
            return;
        }
        
        for (ChessTile[] c: CHESS_TILES) {
            for (ChessTile ct : c) {
                ct.getButton().setEnabled(false);
                ct.getButton().setDisabledIcon(ct.getButton().getIcon());
            }
        }
        CHESS_WINDOW.FORFEIT_BUTTON.setEnabled(false);
        try {
            OutputStream output = socket.getOutputStream();
            PrintWriter writer = new PrintWriter(output, true);
            writer.println(allyWon); //maybe this will work lol 
        } catch (IOException e) {}
    }
    
}
