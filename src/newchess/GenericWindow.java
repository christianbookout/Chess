
package newchess;
import javax.swing.*;

public abstract class GenericWindow extends JFrame{
    /*
    Function: 
    The generic open method for a window, initiliazes window properly
    */
    public void open() {
        addContext();
        //setTitle(TITLE);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }
    /*
    Function:
    Abstract function to initialize variables and add frame context
    */
    public abstract void addContext();
}
