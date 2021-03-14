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
public final class ConnectWindow extends GenericWindow {
    
    private static final String IP_TXT = "Enter IP:";
    private static final String PORT_TXT = "Enter Port:";
    private static final JTextField IP_INPUT = new JTextField(20);
    private static final JTextField PORT_INPUT = new JTextField(5);
    @Override
    public void addContext() {
        JPanel allStuff = new JPanel();
        allStuff.setLayout(new FlowLayout(FlowLayout.CENTER));
        
        JLabel iptxt = new JLabel(IP_TXT);
        
        //For the purpose of being able to hit "enter" instead of the go button
        Action action = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.out.println("Go");
                go();
            }
        };
        IP_INPUT.addActionListener(action);

        JLabel porttxt = new JLabel(PORT_TXT);
        
        PORT_INPUT.addActionListener(action);
        
        JButton goButton = new JButton(" GO ");
        //goButton.setFocusPainted(false);
        
        //When the go button is clicked
        goButton.addActionListener((ActionEvent e) -> {
            go();
        });
        
        allStuff.add(iptxt);
        allStuff.add(IP_INPUT);
        allStuff.add(porttxt);
        allStuff.add(PORT_INPUT);
        allStuff.add(goButton);
        add(allStuff);
    }
    private void go() {
        try {
            //String ip = IP_INPUT.getText();
            //String portStr = PORT_INPUT.getText();
            int port = /*Integer.parseInt(portStr)*/ 9090;
            if (IP_INPUT.getText().isEmpty() | String.valueOf(port).length() != 4) {
                System.out.println("Something went wrong. Returning.");
                return;
            }
            Client client = new Client("127.0.0.1", port);
            setVisible(false);
            dispose();
        } catch (NumberFormatException e) {
            System.out.println("Port was not numerical.");
        }
    }
}
