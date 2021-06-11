
package newchess;

import java.io.*;
import java.net.*;
/**
 *
 * @author chris
 */
public final class Client { //TODO make a ip and port remembering system like minecraft maybe
    
    public Client(String ip, int port) {
        try {

            Socket socket = new Socket(ip, port);
            
            ChessGame.initializeGame(false, socket);
            

        } catch (UnknownHostException ex) {
            System.err.println("Server not found: " + ex.getMessage());
            
        } catch (IOException ex) {
            System.err.println("I/O error: " + ex.getMessage());
        }
    }
    
}
