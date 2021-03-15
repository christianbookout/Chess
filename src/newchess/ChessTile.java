
package newchess;


import java.awt.Color;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.net.URL;
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
    /*
    Function:
        Set a piece without changing the icon to test for mate 
    */
    /*public void testSetPiece(ChessPiece p) {
        piece = p;
    }*/
    private final Color BACKGROUND_COLOR;
    private final JButton button = new JButton();
    private final Position position;
    public Position getPosition() {
        return position;
    }
    
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
    public void highlight() {
        if (!(piece instanceof Blank))
            this.getButton().setBackground(HIGHLIGHT);
    }
    public void unhighlight() {
        //this.getButton().setIcon(selectedPiece.getButton().getIcon());
        this.getButton().setBackground(BACKGROUND_COLOR);
        this.getButton().setBorderPainted(false);
    }
}

