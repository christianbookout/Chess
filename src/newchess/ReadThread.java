/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package newchess;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.Arrays;

/**
 *
 * @author chris
 */
public class ReadThread extends Thread {
    private final Socket socket;
    public ReadThread(Socket s) {
        socket = s;
        System.out.println("here???");
    }
    @Override
    public void run() {
        try {
        InputStream input = socket.getInputStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(input));

        while (true) {
            String move = reader.readLine(); 
            String[] decode = move.split(",");
            ChessGame.receiveMove(decode);
            /*
            if (decode.length == 1) {
                if (decode[0].equalsIgnoreCase("ff")) {
                    //board.CHESS_TILES[0][0].endGame("Nobody");
                    continue;
                }
                //board.CHESS_TILES[0][0].endGame(decode[0]);
                continue;
            }
            
            
            int v = 7 - Integer.parseInt(decode[0]);
            int h = 7 - Integer.parseInt(decode[1]);
            int tov = 7 - Integer.parseInt(decode[2]);
            int toh = 7 - Integer.parseInt(decode[3]);
            if (decode.length == 4) {//Move should be in this format: [vPos][hPos][tovPos][tohPos]
                
                System.out.println(ChessGame.CHESS_TILES[v][h].piece.getName() + " at " + v + ", " + h + " piece received to move to " + tov + ", " + toh);
                ChessGame.movePiece(board.CHESS_TILES[v][h], toh, tov);
                
            } else if (decode.length == 5) {//Move should be in this format: [vPos][hPos][tovPos][tohPos][piece]
                //This is used if two pieces have to switch positions. Piece is the piece that is not represented by lastClicked (which is vPos, hPos)
                
                //The piece to position at v and h positions
                switch (decode[4]) {
                    case "p": //pawn
                        break;
                    case "r": //rook
                        break;
                    case "k": //king
                        break;
                    case "q": //queen
                        break;
                }
                
            }
*/
        }
        } catch (IOException e) {
            System.err.println("ReadThread lost connection");
        }
    }
}
