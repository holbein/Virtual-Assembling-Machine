import java.awt.*;
import java.awt.event.ActionEvent;
import javax.swing.*;

public class Vam extends JFrame{
	
	private static final int FRAME_WIDTH = 800;
	private static final int FRAME_HEIGHT = 800;
	
	private int numberOfLines = 10;
	
	private JPanel panelLeft = new JPanel();
		private JScrollPane scrollPane = new JScrollPane(panelLeft);
			private JTextArea textArea = new JTextArea(numberOfLines, 30);
	
	private JPanel panelRight;
		private JLabel[][] labels = new JLabel[2][18];
		private JButton start;
		private JButton reset;
	
	Vam(){
		setSize(FRAME_WIDTH, FRAME_HEIGHT);
		setResizable(false);
		setLayout(new GridLayout(1, 2));
		setTitle("Virtual Assembler Machine");
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		
		scrollPane.getVerticalScrollBar().setUnitIncrement(10); //sets the scroll-speed
		
		panelLeft.add(addLineNumbering());
		panelLeft.add(textArea);
		
		byte[] by = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
		panelRight = rightPanel(by);
		
		add(scrollPane);
		add(panelRight);
		setVisible(true);
	}
	
	private JPanel addLineNumbering() {
		JPanel ret = new JPanel();
		ret.setLayout(new GridLayout(numberOfLines, 2));
		
		for(int i=0; i<numberOfLines; ++i) {
			ret.add(new JLabel(String.valueOf(i+1)));
			ret.add(new JLabel(":"));
		}
		
		return ret;
	}
	
	public String getFullText() {
		return textArea.getText();
	}

	//line is the same number as the numbering of the lines on the right side 
	public String getTextInLine(int line) {
		String ret = textArea.getText();
		for(int i=0; i<line-1; ++i) {
			ret = ret.substring(ret.indexOf("\n")+1);
		}
		if(ret.contains("\n"))ret = ret.substring(0, ret.indexOf("\n"));
		
		return ret;
	}
	
	//call this method, to update the values 
	public JPanel rightPanel(byte[] by) {
		JPanel ret = new JPanel();
		ret.setLayout(new GridLayout(19, 2, 40, 0));
		
		labels[0][0] = new JLabel("SR");
		labels[0][1] = new JLabel("BZ");
		labels[0][2] = new JLabel("A");
		
		for (int i=0; i<15; ++i) {
			labels[0][i+3] = new JLabel("R"+(i+1));
		}
		
		for(int i=0; i<18; i++) {
			labels[1][i] = new JLabel(String.format("%8s", Integer.toBinaryString(by[i] & 0xFF)).replace(' ', '0'));
		}
		
		for(int i=0; i<18; i++) {
			ret.add(labels[0][i]);
			ret.add(labels[1][i]);
		}
		
		start = new JButton(new AbstractAction("Start"){
            public void actionPerformed(ActionEvent e) {
            	//to do, when JButton "Start" is pressed
            }
		});

		reset = new JButton(new AbstractAction("Reset"){
            public void actionPerformed(ActionEvent e) {
            	//to do, when JButton "Reset" is pressed
            }
		});
		
		ret.add(start);
		ret.add(reset);
		
		return ret;
	}
	
	public static void main(String args[]) {
		new Vam();
	}
}
