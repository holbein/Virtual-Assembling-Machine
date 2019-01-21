
import java.util.*;
import javax.swing.*;

public class Vam extends JFrame{
    
   
    List lines = new ArrayList<JTextField>();
    
    Vam(){
        setSize(800, 800);
        setResizable(false);
        setTitle("Virtual Assembling Machine");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BoxLayout(this.getContentPane(), BoxLayout.Y_AXIS));
        setVisible(true);
        
        for(int i = 0; i<10; i++) {
            addLine(i+1);
        }
        
    }
    
    public void addLine(int number) {
        JPanel panel = new JPanel();
        
        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
        JLabel label = new JLabel(String.valueOf(number));
        lines.add(new JTextField(30));
        ((JTextField)lines.get(number-1)).setSize(680, 30);
        panel.add(label);
        panel.add(((JTextField)lines.get(number-1)));
        panel.setSize(360, 24);
        panel.setVisible(true);
        add(panel);
    }
    
    public static void main(String args[]) {
        new Vam();
    }
}

