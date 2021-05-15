/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package newchess;

import java.io.*;
import java.net.*;
/**
 *
 * @author chris
 */
public final class Host {
    
    private final int port;
    
    public Host(int port) {
        this.port = port;
    }
    
    //i think i did this because constructors throwing exceptions is funny?? dont remember but it works i guess
    public void go() throws IOException{
        try {
            ServerSocket serverSocket = new ServerSocket(/*port*/9090);
            
            Socket socket = serverSocket.accept();
            
            ChessGame.initializeGame(true, socket);
            
            
        } catch (IOException e) {

        } 
    }
}
