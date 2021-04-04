
package newchess;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.Arrays;

public class ReadThread extends Thread {
    private final Socket socket;
    public ReadThread(Socket s) {
        socket = s;
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
            }
        } catch (IOException e) {
            System.err.println("ReadThread lost connection");
        }
    }
}
