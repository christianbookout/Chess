/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package newchess;

import java.awt.*;
import java.awt.event.ActionEvent;
import javax.swing.*;
/**
 *
 * @author chris
 */


public final class FirstPromptWindow extends GenericWindow {
    public static void main(String[] args) {
        new FirstPromptWindow().open();
    }
    @Override
    public void addContext() {
        setLayout(new FlowLayout(FlowLayout.LEADING));
        JButton connect = new JButton(" Connect To A Server ");
        JButton host = new JButton(" Host A Server ");
        JButton openChess = new JButton(" Play By Yourself ");
        
        connect.setFocusPainted(false);
        host.setFocusPainted(false);
        openChess.setFocusPainted(false);

        connect.addActionListener((ActionEvent e) -> {
            new ConnectWindow().open();
            setVisible(false);
            dispose();
        });
        
        host.addActionListener((ActionEvent e) -> {
            new HostWindow().open();
            setVisible(false);
            dispose();
        });
        /*
        openChess.addActionListener((ActionEvent e) -> {
            new ChessBoardWindow(true).open();
            setVisible(false);
            dispose();
        });*/

        add(connect);
        add(host);
        add(openChess, BorderLayout.SOUTH);
    }
}
