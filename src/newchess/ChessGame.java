
package newchess;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import static newchess.ChessBoardWindow.CHESSBOARD_HEIGHT;
import static newchess.ChessBoardWindow.CHESSBOARD_WIDTH;
import static newchess.ChessMove.*;

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
        //All of the return situations passed, so that means you can theoretically attack or move. 
        else {
            selectedTile.unhighlight();
            ChessMove move = selectedTile.getPiece().moveHere(CHESS_TILES, tileClicked.getPosition(), selectedTile.getPosition());
            //Make sure you aren't clicking somewhere you can't actually move
            if (move != NONE) 
            {
                //If the piece can't move bc it would put the king in check then "deselect" the piece and return :-) 
                if (!canLegallyMove(selectedTile)) {
                    selectedTile = null;
                    return;
                }
                //Check to see if the king's in check. If it is, make sure your move would save it from checkmate. 
                if (isUnderAttack(findKingTile(selectedTile.getPiece().isAlly())).length > 0) {
                    
                }
                //System.out.println("Moving " + selectedTile.getPiece().getName() + " to " + tileClicked.getPosition().x + ", " + tileClicked.getPosition().y + " with move " + move.toString());
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
                movePiece(selectedTile, tileClicked.getPosition());
                sendMove(selectedTile, tileClicked.getPosition(), move);
                //TODO check for stalemate 

            }
            //If you've made it this far then you've completed a move or clicked elsewhere. Selected piece is null now 
            selectedTile = null;
        }
        
    }
    /*
    Function:
        Tests to see if a chess tile can move w/o putting the king in danger.
            -If a tile is not under attack, then it can move safely. 
            -If a tile is only under attack by a knight, pawn, or king (either assumed to not already have the king in check) 
            -If a tile is under attack by a bishop, queen, or rook and the path from the bishop, queen, or king that leads to the current piece 
    */
    private static boolean canLegallyMove(ChessTile tile) { 
        
        //Take the position of the attacking piece and the current piece (for example 0,0 and 3,3) and take the separation between the two (-3, -3) and divide by the absolute value (-1, -1) 
        //and then check if the king's position is some multiple of that and if it is then check each piece from the piece being attacked to the king and see if there are any pieces blocking 
        //that distance and if there are then the piece can move and if there aren't then the piece cannot move
        ChessTile kingTile = findKingTile(tile.getPiece().isAlly());
        
        
        ChessTile[] piecesAttackingTile = isUnderAttack(tile);
        
        //Checking to make sure the piece being checked is under attack
        if (piecesAttackingTile.length == 0)  return true;
        
        //Checking to see if the move would put the king in check (bc the piece is defending the king)
        
        //Counter counts to see if each piece attacking the tile is pinning the current tile to the king for each piece attacking the tile 
        int counter = 0;
        ChessPiece p;
        
        //t is an enemy piece (pawn, knight, bishop, rook, or queen) 
        for (ChessTile t : piecesAttackingTile) {
            p = t.getPiece();
            
            //The vector from the piece attacking the king to the tile
            Position posChange = new Position(t.getPosition().x - tile.getPosition().x, t.getPosition().y - tile.getPosition().y);
            
            //The vector from the piece attacking the king to the king
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
            
            
            //Pawns, knights, and kings can't pin a piece to the king
            if (p instanceof Pawn || p instanceof Knight || p instanceof King) {
                counter++; 
                continue;
            } 
            //The king isn't in the line of sight of the other piece.
            else if (!kingPosSign.equals(posSign)) {
                counter++;
                continue;
            }

            //The last check: to see if there are any pieces in the way (b/c a rook can't be pinned to a pawn if the pawn is in the way of the king).
            ChessTile currTile = CHESS_TILES[tile.getPosition().x + posSign.x][tile.getPosition().y + posSign.y];
            
            //Otherwise will result in infinite loop
            if (posSign.equals(new Position(0, 0))) continue;
            
            while (true) {
                //Reached the destination; therefore, you can't move b/c you're pinned. 
                System.out.println("Here");
                if (currTile.getPosition().equals(t.getPosition())) {
                    return false;
                }
                //Reached a piece; therefore, you aren't pinned. 
                if (!(currTile.getPiece() instanceof Blank)) {
                    counter++;
                    break;
                }
                currTile = CHESS_TILES[tile.getPosition().x + posSign.x][tile.getPosition().y + posSign.y];
            }
        }
        //There are no pieces pinning the tile to the king. 
        if (counter == piecesAttackingTile.length) {
            return true;
        }

        return false;
    }
    
    /*
    Function:
        Searches every piece on the board to find the king of type "isAlly"
    Input:
        - isAlly: Whether to find the king that is on your side (true) or the king on the opponent's side (false). 
    Output:
        - The king found, null if no king is found which should be never. 
    */
    private static ChessTile findKingTile(boolean isAlly) {
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
        Finds all of the pieces attacking "tile". returns an array of said pieces. 
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
                    //System.out.println("Testing to see if " + t.getPiece().getName() + " at " + t.getPosition().x + " " + t.getPosition().y + " can attack " + tile.getPiece().getName());
                    //If the piece on the board can attack the specified piece by moving there or attacking it
                    ChessMove move = t.getPiece().moveHere(CHESS_TILES, t.getPosition(), tile.getPosition());
                    if (move != NONE) 
                    {
                        piecesAttackingKing.add(t);
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
        Assuming the king is in check, see if the
    */
    /*
    Ways to save the king from mate: 
        -Attack the piece attacking the king (assuming there is only one piece attacking the king)
        -Move in front of the piece's line of sight (only if the piece is not a horse or pawn) 
    */
    //Precondition: There is at least 1 piece attacking the king TODO make sure this is checked
    private static boolean canSaveKing(ChessTile t, boolean isKingAlly) {
        ChessTile kingTile = findKingTile(isKingAlly);
        ChessTile[] piecesAttackingKing = isUnderAttack(kingTile);
        
        //If the king is in double check then only it can save itself. 
        if (piecesAttackingKing.length == 2) {
            return false;
        }

        /*for (ChessTile[] ct: CHESS_TILES) {
            for (ChessTile t: ct) {*/
        //Blank pieces and enemy pieces can't save the king. Already checked if king can save itself, so we don't need to do that. 
        if (t.getPiece() instanceof Blank || !t.getPiece().isAlly() || t.getPiece() instanceof King) {
            return false;
        }

        //If there is one piece attacking the king and this piece can kill that piece, then it is possible to save the king. 
        if (t.getPiece().moveHere(CHESS_TILES, piecesAttackingKing[0].getPosition(), t.getPosition()) != NONE
                && canLegallyMove(t)) {
            System.out.println(t.getPiece() + " at " + t.getPosition() + " can save king from " + piecesAttackingKing[0]);
            return true; //TODO dunno if enough conditions are checked
        }
        //The only way to save the king from a pawn or a knight is by killing it. If the piece attacking the king is a pawn or knight, then you can't save the king 
        //(already checked if we can kill it). 
        if (piecesAttackingKing[0].getPiece() instanceof Pawn || piecesAttackingKing[0].getPiece() instanceof Knight) {
            return false;
        }
        //Search through every other tile to see if the piece can block the view of the piece attacking the king 
        for (ChessTile[] i : CHESS_TILES) {
            //j is the position that we're checking to see if t can move to to save the king 
            for (ChessTile j : i) {
                //If the piece attacking the king and your selected tile "t" can move to the same position for each of the pieces attacking the king, 
                //then you can save the king from checkmate by moving to that position.

                //Can only block the check's vision by moving to a blank piece (already checked if we can kill a pieceto save the king) 
                if (!(j.getPiece() instanceof Blank)) {
                    continue;
                }

                //The vector from the piece tryna save the king to the king 
                Position posChange = new Position(t.getPosition().x - kingTile.getPosition().x, t.getPosition().y - kingTile.getPosition().y);

                //The vector from the piece attacking the king to the king
                Position posToKingChange = new Position(piecesAttackingKing[0].getPosition().x - kingTile.getPosition().x, piecesAttackingKing[0].getPosition().y - kingTile.getPosition().y);

                Position posSign = new Position(0, 0);
                if (posChange.x != 0) {
                    posSign.x = posChange.x / Math.abs(posChange.x);
                }
                if (posChange.y != 0) {
                    posSign.y = posChange.y / Math.abs(posChange.y);
                }

                Position posToKingSign = new Position(0, 0);
                if (posToKingChange.x != 0) {
                    posToKingSign.x = posToKingChange.x / Math.abs(posToKingChange.x);
                }
                if (posToKingChange.y != 0) {
                    posToKingSign.y = posToKingChange.y / Math.abs(posToKingChange.y);
                }

                //if y posToKingChange is negative and you're moving to a y-position greater than piece attacking king position then you can move
                //if y posToKingChange is positive and you're moving to a y-position less than piece attacking king position then you can move
                boolean blockingVision = ((posToKingChange.y < 0 && j.getPosition().y > piecesAttackingKing[0].getPosition().y) || (posToKingChange.y > 0 && j.getPosition().y < piecesAttackingKing[0].getPosition().y) || posToKingChange.y == 0)
                        && ((posToKingChange.x < 0 && j.getPosition().x > piecesAttackingKing[0].getPosition().x) || (posToKingChange.x > 0 && j.getPosition().x < piecesAttackingKing[0].getPosition().x) || posToKingChange.x == 0);

                if (t.getPiece().moveHere(CHESS_TILES, j.getPosition(), t.getPosition()) != NONE
                        && piecesAttackingKing[0].getPiece().moveHere(CHESS_TILES, j.getPosition(), t.getPosition()) != NONE
                        && posToKingSign.equals(posSign) && canLegallyMove(t)
                        && blockingVision) { //TODO make sure they aren't just the same position but it is a position that is putting the king in check 
                    //done i think

                    System.out.println(t.getPiece() + " at " + t.getPosition() + " can move to " + j.getPosition() + " to save the king " + " blocking vision: " + blockingVision + " kingPosSign check: " + posToKingSign.equals(posSign) + " can legally move the tile " + canLegallyMove(t) + " can move the piece " + t.getPiece().moveHere(CHESS_TILES, j.getPosition(), t.getPosition()));
                    System.out.println("pos to king sign " + posToKingSign + " piece to king ");
                    return true;

                }
                //If your move intercepts each of the pieces attacking the king's views, then it can save the king. 
                /*if (counter == piecesAttackingKing.length && canLegallyMove(t)) {
                                //return testMove(t, piecesAttackingKing[0]);
                                System.out.println(t.getPiece() + " can move to " + j.getPosition() + " to save the king");
                                return true;
                            }*/

            }
        }

        /*}
        }*/
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
                ChessMove move = kingTile.getPiece().moveHere(CHESS_TILES, t.getPosition(), kingTile.getPosition());
                
                //If you can move to a piece that isn't under attack then the king can save itself
                if (move == NORMAL && isUnderAttack(t).length == 0) {
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
    private static boolean isInStalemate(boolean side) {
        //TODO if every piece on the side cannot move to any position on the board then the game is in stalemate. 
        return false;
    }
    
    //Check if the king's in checkmate (at the beginning of the round) 
    private static boolean isKingInCheckmate(boolean isKingAlly) {
        ChessTile kingTile = findKingTile(isKingAlly);
        ChessTile[] piecesAttackingKing = isUnderAttack(kingTile);
        
        //King can't be in checkmate if there are no pieces attacking it 
        if (piecesAttackingKing.length == 0) return false;
        
        //King can't be in checkmate if it can save itself. 
        else if (canKingSaveItself(isKingAlly)) return false;
        
        //King can't be in checkmate if it can be saved by a piece on the board
        for (ChessTile[] p : CHESS_TILES) {
            for (ChessTile t: p) {
                if (canSaveKing(t, true)) {
                    return false;
                }
            }
        }
        
        //King must be in checkmate if the other conditions didn't pass. 
        return true;
    }
    
    
    private static void movePiece(ChessTile tile, Position toPosition) {
        if (tile.getPiece() instanceof Pawn && Math.abs(tile.getPosition().y - toPosition.y) == 2) {
            ((Pawn) tile.getPiece()).pawnDoubleJumpTurnNumber = ChessGame.getTurnNumber();
        } 
        tile.getPiece().hasMoved();
        
        CHESS_TILES[toPosition.y][toPosition.x].setPiece(tile.getPiece(), tile.getPiece().isWhite(), tile.getPiece().isAlly());
        
        tile.setPiece(new Blank(), false, false);
        tile.getButton().setIcon(null);
        tile.unhighlight();
        
        endTurn();
    }
    
    public static void receiveMove(String[] decode) {
        if (decode.length == 1) {
            endGame(true, "you won :D ");
            return;
        }
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
            System.out.println("Game over :*(");
            endGame(false, "you lost D:");
        } else if (isInStalemate(true)) {
            System.out.println("Stalemate :( ");
            endGame(false, "draw!!!");
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
        
        tile.getPiece().hasMoved();
        
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
        System.out.println(move);
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
    }
    //Move the pawn toPosition (completes an en passant) 
    public static void enPassant(ChessTile pawn, Position toPosition) {
        CHESS_TILES[toPosition.y][toPosition.x].setPiece(pawn.getPiece());
        CHESS_TILES[pawn.getPosition().y][toPosition.x].setPiece(new Blank());
        pawn.setPiece(new Blank());
        endTurn();
    }
    //Ends turn, increments turn number, and sets to opponent's turn 
    private static void endTurn() {
        alliedTurn = !alliedTurn;
        turnNumber++;
    }
    
    //Ends the game by disabling each button and printing endText 
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
