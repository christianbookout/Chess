/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package newchess;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.IOException;
import javax.swing.*;

/**
 *
 * @author chris
 */
public final class HostWindow extends GenericWindow {
    private static final JTextField PORT_INPUT = new JTextField(5);
    @Override
    public void addContext() {
        JPanel allStuff = new JPanel();
        allStuff.setLayout(new FlowLayout(FlowLayout.CENTER));
        
        //For the purpose of being able to hit "enter" instead of the go button
        Action action = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                System.out.println("Go");
                go();
                } catch (IOException ioexc) {
                    System.err.println("Host window action failed to go");
                }
            }
        };

        JLabel porttxt = new JLabel("Enter Port:");
        
        JButton goButton = new JButton(" GO ");
        
        PORT_INPUT.addActionListener(action);
        
        goButton.addActionListener((ActionEvent e) -> {
            try {
                go();
            } catch (IOException ioexc) {
                System.err.println("Failed to add goButton");
            }
        });
        
        allStuff.add(porttxt);
        allStuff.add(PORT_INPUT);
        allStuff.add(goButton);
        add(allStuff);
    }
    private void go() throws IOException{
        String port = PORT_INPUT.getText();
        if (port.isEmpty()) {
            return;
        }
        try {
            Host host = new Host(/*Integer.parseInt(port)*/9090);
            host.go();
        } catch (NumberFormatException e) {
            System.err.println("user entered non-numerical port as host");
        }
        setVisible(false);
        dispose();
    }
}
