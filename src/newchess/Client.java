
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

            Socket socket = new Socket(/*ip*/"127.0.0.1", /*port*/9090);
            
            ChessGame.initializeGame(false, socket);
            

        } catch (UnknownHostException ex) {
            System.err.println("Server not found: " + ex.getMessage());
            
        } catch (IOException ex) {
            System.err.println("I/O error: " + ex.getMessage());
        }
    }
    
}
