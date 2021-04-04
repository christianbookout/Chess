
package newchess;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;

public class WriteThread extends Thread {
    private final Socket socket;
    public WriteThread(Socket s) {
        socket = s;
    }
    private static PrintWriter writer;
    @Override
    public void run() {
        try {
            OutputStream output = socket.getOutputStream();
            writer = new PrintWriter(output, true);
        } catch (IOException e) {
            System.err.println("WriteThread lost connection");
        }
    }
    public static void printMove(String toPrint) {
        writer.println(toPrint);
    }
}
