/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package newchess;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Arrays;

/**
 *
 * @author chris
 */
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
