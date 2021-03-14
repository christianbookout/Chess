/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package newchess;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.text.DefaultCaret;

public class ChessBoardWindow extends GenericWindow{
    public static final int CHESSBOARD_WIDTH = 8;
    public static final int CHESSBOARD_HEIGHT = 8;
    //Visual padding effect. For now, it's zero because that looks the best
    private static final int CHESSBOARD_PADDING = 0;
    private static final String TITLE = "Chess";
    
    //The tan tile colour
    public static final Color TAN = Color.decode("#debc99");
    
    //The brown tile colour
    public static final Color DARK_BROWN = Color.decode("#785027");
    
    //The colour of the tile when it is clicked
    public static final Color HIGHLIGHT = Color.decode("#CD6839");
    
    //The panel that contains the chess program
    private final JPanel CHESS_PANEL = new JPanel();
    
    //The panel that contains the chat program
    private final JPanel CHAT_PANEL = new JPanel();
    
    //The text area that contains the messages sent by the chat program
    public final JTextArea CHAT_BOX = new JTextArea();
    
    //The button in the chat panel that allows the user to forfeit the game. 
    public final JButton FORFEIT_BUTTON = new JButton(" Forfeit ");
    
    //The button in the chat panel that allows the user to restart the game.
    //private final JButton RESTART_BUTTON = new JButton(" Restart ");
    
    //The button in the chat panel that allows the user to return to the option select menu
    private final JButton RETURN_BUTTON = new JButton(" Menu ");
    
    //The user's screen dimensions
    private final Dimension SCREEN_SIZE = Toolkit.getDefaultToolkit().getScreenSize();
    
    //True if white, false if black
    private final boolean PLAYER_COLOUR;
    
    public ChessBoardWindow(boolean isWhite) {
        PLAYER_COLOUR = isWhite;
    }
    
    @Override
    public void open() {
        setTitle(TITLE);
        addContext();
    }
    
      /******************************
     * SET BUTTON LAYOUT AND STYLE*
     ******************************
     * Create, with a grid layout,
     * buttons which represent
     * each tile for chess pieces.
     * Also setting up the opening
     * window.
     */
    @Override
    public void addContext() {
        CHESS_PANEL.setLayout(new GridLayout(CHESSBOARD_WIDTH, CHESSBOARD_HEIGHT, CHESSBOARD_PADDING, CHESSBOARD_PADDING));
        CHESS_PANEL.setBorder(BorderFactory.createLoweredBevelBorder());
        
        setLayout(new FlowLayout(FlowLayout.LEFT));
        add(CHESS_PANEL);
        
        CHAT_PANEL.setLayout(new BoxLayout(CHAT_PANEL, BoxLayout.Y_AXIS));
        
        DefaultCaret caret = (DefaultCaret)CHAT_BOX.getCaret();
        caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
        {
            CHAT_BOX.setEditable(false);
            CHAT_BOX.setBorder(null);
            CHAT_BOX.setAlignmentY(Component.TOP_ALIGNMENT);
            CHAT_BOX.setBackground(Color.WHITE);
            CHAT_BOX.setOpaque(true);
            CHAT_BOX.setFont(new Font("Arial", Font.PLAIN, 12));
        }
        
        JScrollPane scrollPane = new JScrollPane(CHAT_BOX,
                                    JScrollPane.VERTICAL_SCROLLBAR_NEVER, 
                                    JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        {
            scrollPane.setPreferredSize(new Dimension(235, 250));
            //scrollPane.setPreferredSize(new Dimension((int)(SCREEN_SIZE.width/5), (int)(SCREEN_SIZE.height/2.5)));
            scrollPane.setViewportView(CHAT_BOX);
            scrollPane.setBorder(BorderFactory.createLoweredBevelBorder());
        }
        CHAT_PANEL.add(scrollPane, BorderLayout.CENTER);
        
        JPanel buttonPanel = new JPanel();
        {
            FORFEIT_BUTTON.setBorder(BorderFactory.createRaisedBevelBorder());
            FORFEIT_BUTTON.setBackground(Color.LIGHT_GRAY);
            FORFEIT_BUTTON.setFocusPainted(false);
            FORFEIT_BUTTON.addActionListener((ActionEvent e) -> {
                /*if (turnNumber > 1) {
                    CHESS_TILES[0][0].endGame("Nobody");
                    try {
                        OutputStream output = getSocket().getOutputStream();
                        PrintWriter writer = new PrintWriter(output, true);
                        writer.println("ff");
                    } catch (IOException ex) {}
                }*/
            });
        }
        {
            RETURN_BUTTON.setBorder(BorderFactory.createRaisedBevelBorder());
            RETURN_BUTTON.setBackground(Color.LIGHT_GRAY);
            RETURN_BUTTON.setFocusPainted(false);
            RETURN_BUTTON.addActionListener((ActionEvent e) -> {
                /*if (turnNumber > 1) {
                    new PromptForReturningToMenuWindow(this).open();
                } else {
                    turnNumber = 0;
                    new FirstPromptWindow().open();
                    setVisible(false);
                    removeAll();
                    dispose();
                }*/
            });
        }
        /*if (getSocket() == null)
        {
            RESTART_BUTTON.setBorder(BorderFactory.createRaisedBevelBorder());
            RESTART_BUTTON.setBackground(Color.LIGHT_GRAY);
            RESTART_BUTTON.setFocusPainted(false);
            RESTART_BUTTON.addActionListener((ActionEvent e) -> {
                if (turnNumber > 1) {
                    new RestartingPromptWindow(this).open();
                } else {
                    turnNumber = 0;
                    if (this.getSocket() == null) {
                        new ChessBoard(true).open();
                    } else {
                        new ChessBoard(true, socket).open();
                    }
                    setVisible(false);
                    removeAll();
                    dispose();
                }
            });
            buttonPanel.add(RESTART_BUTTON);
        }*/
        buttonPanel.add(RETURN_BUTTON);
        buttonPanel.add(FORFEIT_BUTTON);
        CHAT_PANEL.add(buttonPanel, BorderLayout.CENTER);
        
        {
            add(CHAT_PANEL);
            pack();
            setResizable(false);
            setSize(950, 535);
            //setSize((int)(SCREEN_SIZE.width/1.65), (int)(SCREEN_SIZE.height/1.4));
            setLocationRelativeTo(null);
            setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            setVisible(true);
        }
    }
    
  
    /****************************
    * CREATE CHESS BOARD LAYOUT *
    *****************************
    * When the game is started,
    * create the layout of a
    * chess board. Set each chess
    * piece to its own position
    * on the board.
    */
    public void initializeBoard(ChessTile[][] chessBoard) {
        
        //Setting chess pieces to either tan or dark brown to create the correct pattern
        for (int i = 0; i < CHESSBOARD_WIDTH; i++) {
            for (int ii = 0; ii < CHESSBOARD_HEIGHT; ii++) {
                if (i % 2 == 0) {
                    if (ii % 2 == 0) {
                        chessBoard[i][ii] = new ChessTile(DARK_BROWN, i, ii);
                        CHESS_PANEL.add(chessBoard[i][ii].getButton());
                    } else {
                        chessBoard[i][ii] = new ChessTile(TAN, i, ii);
                        CHESS_PANEL.add(chessBoard[i][ii].getButton());
                    }
                } 
                
                else {
                    if (ii % 2 == 1) {
                        chessBoard[i][ii] = new ChessTile(DARK_BROWN, i, ii);
                        CHESS_PANEL.add(chessBoard[i][ii].getButton());
                    } else {
                        chessBoard[i][ii] = new ChessTile(TAN, i, ii);
                        CHESS_PANEL.add(chessBoard[i][ii].getButton());
                    }
                }
            }
        }
        //Set all of the chess pieces on the board to the proper piece
        for (int i = 0; i < CHESSBOARD_WIDTH; i++) {
            for (int j = 2; j < CHESSBOARD_HEIGHT-2; j++) {
                chessBoard[j][i].setPiece(new Blank(), false, false);
            }
        }
        for (int i = 0; i < CHESSBOARD_WIDTH; i++) {
            chessBoard[1][i].setPiece(new Pawn(), !PLAYER_COLOUR, false);
            chessBoard[CHESSBOARD_HEIGHT-2][i].setPiece(new Pawn(), PLAYER_COLOUR, true);
        }
        chessBoard[0][0].setPiece(new Rook(), !PLAYER_COLOUR, false);
        chessBoard[0][7].setPiece(new Rook(), !PLAYER_COLOUR, false);
        chessBoard[CHESSBOARD_HEIGHT-1][0].setPiece(new Rook(), PLAYER_COLOUR, true);
        chessBoard[CHESSBOARD_HEIGHT-1][7].setPiece(new Rook(), PLAYER_COLOUR, true);
        
        chessBoard[0][1].setPiece(new Knight(), !PLAYER_COLOUR, false);
        chessBoard[0][6].setPiece(new Knight(), !PLAYER_COLOUR, false);
        chessBoard[CHESSBOARD_HEIGHT-1][1].setPiece(new Knight(), PLAYER_COLOUR, true);
        chessBoard[CHESSBOARD_HEIGHT-1][6].setPiece(new Knight(), PLAYER_COLOUR, true);
        
        chessBoard[0][2].setPiece(new Bishop(), !PLAYER_COLOUR, false);
        chessBoard[0][5].setPiece(new Bishop(), !PLAYER_COLOUR, false);
        chessBoard[CHESSBOARD_HEIGHT-1][2].setPiece(new Bishop(), PLAYER_COLOUR, true);
        chessBoard[CHESSBOARD_HEIGHT-1][5].setPiece(new Bishop(), PLAYER_COLOUR, true);
        
        //Setting the king and queen positions depending on the colour
        if (PLAYER_COLOUR) {
            chessBoard[0][3].setPiece(new Queen(), !PLAYER_COLOUR, false);
            chessBoard[CHESSBOARD_HEIGHT - 1][3].setPiece(new Queen(), PLAYER_COLOUR, true);

            chessBoard[0][4].setPiece(new King(), !PLAYER_COLOUR, false);
            chessBoard[CHESSBOARD_HEIGHT - 1][4].setPiece(new King(), PLAYER_COLOUR, true);
        } else {
            chessBoard[0][3].setPiece(new King(), !PLAYER_COLOUR, false);
            chessBoard[CHESSBOARD_HEIGHT - 1][3].setPiece(new King(), PLAYER_COLOUR, true);

            chessBoard[0][4].setPiece(new Queen(), !PLAYER_COLOUR, false);
            chessBoard[CHESSBOARD_HEIGHT - 1][4].setPiece(new Queen(), PLAYER_COLOUR, true);
        }
        
    }
}

