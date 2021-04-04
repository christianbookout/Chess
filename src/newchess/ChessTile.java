
package newchess;


import java.awt.Color;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.net.URL;
import java.util.ArrayList;
import java.util.Stack;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import static newchess.ChessBoardWindow.HIGHLIGHT;

/*
Class:
    Represents one chess tile. Containts all of the methods to change the information about the chess tile such as:
        - Image icon
        - Background colour
        - Current chess piece occupying the tile
        - The button representing the tile
        - 
*/
public class ChessTile {
    
    private ChessPiece piece;
    private final Color BACKGROUND_COLOR;
    private final JButton button = new JButton();
    private final Position position;
    private static Stack<ChessTile> highlightedTiles = new Stack<ChessTile>();
    private static final Color MOVE_TO_COLOR = new Color(1);
    
     /*
    Function: 
    Creates a "Chess tile", initializing its colour and storing its position.
    Inputs: 
    - c: Background color
    - yPos: Vertical position on the chess board
    - xPos: Horizontal position on the chess board 
    */
    public ChessTile(Color c, int yPos, int xPos) { 
        position = new Position(xPos, yPos);
        BACKGROUND_COLOR = c;
        piece = new Blank();
        button.setBackground(BACKGROUND_COLOR);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.addActionListener((ActionEvent e) -> {
            ChessGame.clickTile(this);
        });
    }
    public ChessPiece getPiece() {return piece;}
    
    public void setPiece(ChessPiece toPiece, boolean isWhite, boolean ia) {
        piece = toPiece;
        piece.isAlly(ia);
        piece.isWhite(isWhite);
        
        if (toPiece instanceof Blank)  {
            button.setIcon(null);
            return;
        }
        
        if (isWhite){
            button.setIcon(icon("w" + toPiece.getName()));
        } else {
            button.setIcon(icon(toPiece.getName()));
        }
    }
    
    public void setPiece(ChessPiece p) {
        piece = p;
        
        if (p instanceof Blank)  {
            button.setIcon(null);
            return;
        }
        
        if (p.isWhite()){
            button.setIcon(icon("w" + p.getName()));
        } else {
            button.setIcon(icon(p.getName()));
        }
    }
    
    public Position getPosition() {
        return position;
    }
    
    public String toString() {
        return piece + " at " + position;
    }
    
    public JButton getButton(){
        return button;
    }
    //Creates an icon w/ the desired name, used to represent each of the chess pieces. Black pieses are normal (name will be Pawn), white pieces have w in front (name will be wPawn). 
    private ImageIcon icon(String name) {
        try {
            URL imageLocation = this
                .getClass()
                .getResource("/resources/" + name + ".png");
            return new ImageIcon(new ImageIcon(imageLocation).getImage().getScaledInstance(30, 50, Image.SCALE_DEFAULT));
        }
        catch (Exception e) {
            System.err.println("Icon failed to initialize with name: " + name);
            return null;
        }

    }
    public void highlight(ChessTile[][] ct) {
        if (!(piece instanceof Blank))
            this.getButton().setBackground(HIGHLIGHT);
        for (ChessTile[] a: ct) {
            for (ChessTile b: a) {
                if (piece.moveHere(ct, b.getPosition(), position) != ChessMove.NONE) {
                    b.getButton().setBackground(b.BACKGROUND_COLOR.darker());
                    highlightedTiles.push(b);
                }
            }
        }
    }
    public void unhighlight() {
        //this.getButton().setIcon(selectedPiece.getButton().getIcon());
        this.getButton().setBackground(BACKGROUND_COLOR);
        this.getButton().setBorderPainted(false);
        while (!highlightedTiles.isEmpty()){
            ChessTile t = highlightedTiles.pop();
            t.getButton().setBackground(t.BACKGROUND_COLOR);;
        }
    }
}

