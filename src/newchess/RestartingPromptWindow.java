/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package newchess;


import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;


//CURRENTLY DOESNT DO ANYTHING b/c no restart button in online play :D 
public class RestartingPromptWindow extends GenericWindow{
     private final String promptText = "Are you sure you want to restart?";
    final ChessBoardWindow host;
    RestartingPromptWindow(ChessBoardWindow c) {
        host = c;
    }
    @Override
    public void addContext() {
        JPanel allStuff = new JPanel();
        allStuff.setLayout(new BoxLayout(allStuff, BoxLayout.Y_AXIS));
        JLabel txt = new JLabel(promptText);
        allStuff.add(txt);
        JPanel buttons = new JPanel();
        buttons.setLayout(new FlowLayout(FlowLayout.CENTER));
        JButton yes = new JButton(" Yes ");
        JButton no = new JButton(" No ");
            yes.setBorder(BorderFactory.createRaisedBevelBorder());
            yes.setBackground(Color.LIGHT_GRAY);
            yes.setFocusPainted(false);
            
            no.setBorder(BorderFactory.createRaisedBevelBorder());
            no.setBackground(Color.LIGHT_GRAY);
            no.setFocusPainted(false);
        yes.addActionListener((ActionEvent e) -> {

            new ChessBoardWindow(true).open();
            host.setVisible(false);
            host.removeAll();
            host.dispose();
            //turnNumber = 0;
            setVisible(false);
            dispose();
        });
        no.addActionListener((ActionEvent e) -> {
            setVisible(false);
            dispose();
        });
        buttons.add(yes);
        buttons.add(no);
        
        allStuff.add(buttons);
        add(allStuff);
    }
}
