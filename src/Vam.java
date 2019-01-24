
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileNameExtensionFilter;


@SuppressWarnings("serial")
public class Vam extends JFrame{
	
	private static final String version = "1.0.1";
	
	private static final int FRAME_WIDTH = 800;
	private static final int FRAME_HEIGHT = 600;
	
	private boolean stop = false;
	private HashSet<Integer> errorLineSeen = new HashSet<Integer>();
	
	public byte SR; //Aufbau: (0,0,0,0,0,Overflow,GraterZero,SmallerZero)
	public byte BZ;
	public byte A;
	
	public final byte[] R = new byte[16];
	
	private int numberOfLines = 1;
	
	private JPanel panelLeft = new JPanel();
	
	private JScrollPane scrollPane = new JScrollPane(panelLeft);
	private JPanel lineNumbering = new JPanel();
	private static ImageIcon ARROW_EMPTY = new ImageIcon(Vam.class.getResource("resources/arrow_empty_16x12.png"));
	private static ImageIcon ARROW_GREEN = new ImageIcon(Vam.class.getResource("resources/arrow_green_16x12.png"));
	private static ImageIcon ERROR = new ImageIcon(Vam.class.getResource("resources/error_16x15.png"));
	private static ImageIcon ARROW_ERROR = new ImageIcon(Vam.class.getResource("resources/arrow_error_16x15.png"));
	private JTextArea textArea = new JTextArea(numberOfLines, 30);

	private JPanel panelRight;
	private JLabel[][] labels = new JLabel[3][19];
	private JButton start;
	private JButton oneStep;
	private JButton reset;
	
	private JFrame errorFrame; //small JFrame with error message, that pops up when there was an error
	private JPanel errorPanel = new JPanel();
	private JScrollPane errorScroll = new JScrollPane(errorPanel);
	
	Vam(){
		setSize(FRAME_WIDTH, FRAME_HEIGHT);
		setResizable(false);
		setLayout(new GridLayout(1, 2));
		setTitle("Virtual Assembling Machine v." + version);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		
		List <Image> imgs = new ArrayList<Image>();
		imgs.add(new ImageIcon(getClass().getResource("resources/Holbein_Logo_128x128.png")).getImage());
		imgs.add(new ImageIcon(getClass().getResource("resources/Holbein_Logo_64x64.png")).getImage());
		imgs.add(new ImageIcon(getClass().getResource("resources/Holbein_Logo_32x32.png")).getImage());
		imgs.add(new ImageIcon(getClass().getResource("resources/Holbein_Logo_16x16.png")).getImage());
		imgs.add(new ImageIcon(getClass().getResource("resources/Holbein_Logo_8x8.png")).getImage());
		setIconImages(imgs);
		
		textArea.getDocument().addDocumentListener(new MyDocumentListener());
		scrollPane.getVerticalScrollBar().setUnitIncrement(10); //sets the scroll-speed
		
		reDrawLeftPanel();
		
		panelLeft.add(lineNumbering);
		panelLeft.add(textArea);
		
		reset();
		
		setMenu();
		add(scrollPane);
		add(panelRight);
		setVisible(true);
	}
	
	public static void main(String args[]) {
		new Vam();
	}

	private void setMenu() {
		JMenuBar bar = new JMenuBar();
		JMenu menu = new JMenu("File");
		JMenuItem exp = new JMenuItem("Save");
		JMenuItem imp = new JMenuItem("Open");
		
		exp.addActionListener(new ActionListener() {			
			@Override
			public void actionPerformed(ActionEvent e) {
				exp();
			}
		});
		
		imp.addActionListener(new ActionListener() {			
			@Override
			public void actionPerformed(ActionEvent e) {
				imp();
			}
		});
		
		menu.add(exp);
		menu.add(imp);
		
		bar.add(menu);
		
		setJMenuBar(bar);
	}
	
	private void exp() {
		JFileChooser choose = new JFileChooser();
		choose.setFileSelectionMode(JFileChooser.FILES_ONLY);
		choose.setFileFilter(new FileNameExtensionFilter(".txt", "txt"));
		choose.setSelectedFile(new File("MyVAMprogram.txt"));
		
		if(choose.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
			File out = choose.getSelectedFile();
			try {
				PrintWriter writer = new PrintWriter(out);
				writer.print(textArea.getText());
				writer.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	private void imp() {
		try {
			JFileChooser choose = new JFileChooser();
			choose.setFileSelectionMode(JFileChooser.FILES_ONLY);
			choose.setFileFilter(new FileNameExtensionFilter(".txt", "txt"));
			
			if(choose.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
				File file = choose.getSelectedFile();
				BufferedReader br = new BufferedReader( new FileReader(file));
				Object[] str = br.lines().toArray();
				String whole = "";
				for(int i = 0; i< str.length; i++) {
					whole += str[i]+"\n";
				}
				br.close();
				textArea.setText(whole);
			} 
		}catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void rightPanel(byte[] by) {
		panelRight = new JPanel();
		panelRight.setLayout(new GridLayout(20, 3));
		
		labels[0][0] = new JLabel("SR", SwingConstants.CENTER);
		labels[0][1] = new JLabel("BZ", SwingConstants.CENTER);
		labels[0][2] = new JLabel("A", SwingConstants.CENTER);
		
		for (int i=0; i<16; ++i) {
			labels[0][i+3] = new JLabel("R"+(i), SwingConstants.CENTER);
		}
		
		for(int i=0; i<19; i++) {
			labels[1][i] = new JLabel(String.format("%8s", Integer.toBinaryString(by[i] & 0xFF)).replace(' ', '0'), SwingConstants.CENTER);
			labels[2][i] = new JLabel(Byte.toString(by[i]), SwingConstants.CENTER);
			
			labels[0][i].setBorder(BorderFactory.createLineBorder(Color.BLACK));
			labels[1][i].setBorder(BorderFactory.createLineBorder(Color.BLACK));
			labels[2][i].setBorder(BorderFactory.createLineBorder(Color.BLACK));
		}
		
		for(int i=0; i<19; i++) {
			panelRight.add(labels[0][i]);
			panelRight.add(labels[1][i]);
			panelRight.add(labels[2][i]);
		}
		
		start = new JButton(new AbstractAction("Start"){
            public void actionPerformed(ActionEvent e) {
            	start();
            }
		});

		oneStep = new JButton(new AbstractAction("One step"){
            public void actionPerformed(ActionEvent e) {
            	oneStep();
            }
		});
		
		reset = new JButton(new AbstractAction("Reset"){
            public void actionPerformed(ActionEvent e) {
            	reset();
            }
		});
		
		panelRight.add(start);
		panelRight.add(oneStep);
		panelRight.add(reset);
	}
	
	//call this method, to update the values 
	private void reDrawRightPanel(byte[] by) {
		for(int i=0; i<19; i++) {
			labels[1][i].setText(String.format("%8s", Integer.toBinaryString(by[i] & 0xFF)).replace(' ', '0'));
			labels[2][i].setText(Byte.toString(by[i]));
		}
	}

	//call this method, to update the values 
	private void reDrawLeftPanel() {
		lineNumbering.setLayout(new GridLayout(textArea.getLineCount(), 3));
		while(lineNumbering.getComponentCount() != 0) {
			lineNumbering.remove(0);
		}
		
		for(int i=0; i<numberOfLines; ++i) {
			if (errorLineSeen.contains(i+1)){
				if(BZ == i+1) {
					lineNumbering.add(new JLabel(ARROW_ERROR));
				} else {
					lineNumbering.add(new JLabel(ERROR));
				}
			}else {
				if(BZ == i+1) {
					lineNumbering.add(new JLabel(ARROW_GREEN));
				} else {
					lineNumbering.add(new JLabel(ARROW_EMPTY));
				}
			}
			
			if(numberOfLines<10) {
				lineNumbering.add(new JLabel(String.valueOf(i+1)+"   "));
			} else {
				lineNumbering.add(new JLabel(String.valueOf(i+1)));
			}
			lineNumbering.add(new JLabel(":"));
		}
	}
	
    class MyDocumentListener implements DocumentListener {
        final String newline = "\n";
 
        public void insertUpdate(DocumentEvent e) {
        	lineNumbering.setLayout(new GridLayout(textArea.getLineCount(), 3));
			while(textArea.getLineCount() > numberOfLines) {
				numberOfLines++;
				
				if (errorLineSeen.contains(numberOfLines)){
					if((BZ-1) == numberOfLines) {
						lineNumbering.add(new JLabel(ARROW_ERROR));
					} else {
						lineNumbering.add(new JLabel(ERROR));
					}
				}else {
					if((BZ-1) == numberOfLines) {
						lineNumbering.add(new JLabel(ARROW_GREEN));
					} else {
						lineNumbering.add(new JLabel(ARROW_EMPTY));
					}
				}
				
				if(numberOfLines<10) {
					lineNumbering.add(new JLabel(String.valueOf(numberOfLines)+"  "));
				} else {
					lineNumbering.add(new JLabel(String.valueOf(numberOfLines)));
				}
				lineNumbering.add(new JLabel(":"));
			}
        }
        
        public void removeUpdate(DocumentEvent e) {
        	lineNumbering.setLayout(new GridLayout(textArea.getLineCount(), 3));
			while(textArea.getLineCount() < numberOfLines) {
				lineNumbering.remove(lineNumbering.getComponentCount()-1);
				lineNumbering.remove(lineNumbering.getComponentCount()-1);
				lineNumbering.remove(lineNumbering.getComponentCount()-1);
				numberOfLines--;
			}
        }
        public void changedUpdate(DocumentEvent e) {
            //Plain text components don't fire these events.
        }
    }
    
	//line is the same number as the numbering of the lines on the right side 
	private String getTextInLine(int line) {
		String ret = textArea.getText();
		for(int i=0; i<line-1; ++i) {
			ret = ret.substring(ret.indexOf("\n")+1);
		}
		if(ret.contains("\n"))ret = ret.substring(0, ret.indexOf("\n"));
		
		return ret;
	}

	private void reset() {
		errorLineSeen.clear();
		errorPanel.removeAll();
		
		stop = false;
		SR = 0;
		BZ = 1;
		A = 0;
		
		for(int i=0; i<16; i++) {
			R[i] = 0; 
		}
		
		byte[] by = {SR, BZ, A, R[0], R[1], R[2], R[3], R[4], R[5], R[6], R[7], R[8], R[9], R[10], R[11], R[12], R[13], R[14], R[15]};
		if(panelRight == null) rightPanel(by);
		else reDrawRightPanel(by);
		reDrawLeftPanel();
	}
	
	private void oneStep() {
		if(!stop && 0 < BZ && BZ <= textArea.getLineCount()){
			check(getTextInLine(BZ).trim());
		}
		
		byte[] by = {SR, BZ, A, R[0], R[1], R[2], R[3], R[4], R[5], R[6], R[7], R[8], R[9], R[10], R[11], R[12], R[13], R[14], R[15]};
		reDrawRightPanel(by);
		reDrawLeftPanel();
	}
	
	private void start() {
		while(!stop && 0 < BZ && BZ <= textArea.getLineCount()) {
			check(getTextInLine(BZ).trim());
		}
		byte[] by = {SR, BZ, A, R[0], R[1], R[2], R[3], R[4], R[5], R[6], R[7], R[8], R[9], R[10], R[11], R[12], R[13], R[14], R[15]};
		reDrawRightPanel(by);
		reDrawLeftPanel();
	}

	//separates the command and the rest
	private void check(String input) {
		
		if (input.equals("END")) {
			execute("END", 0);
			return;
		}
		
		int space = input.indexOf(' ');			
		if (space == -1) { // rubbish
			def(input);
			return;
		}
		
		String com = input.substring(0, space);
		try {
			execute(com, Integer.parseInt(input.substring(space+1)));
		}catch (Exception e) {
			// TODO: handle exception
			def(" '"+input.substring(space+1)+"'"+" is not a valid number!");
			machine_end(0);
		}
	}

	
	private void def(String input) {//default
        String caller = Thread.currentThread().getStackTrace()[2].getMethodName(); //for debug purposes: shows in which method def(String) was called
        int lineNo = Thread.currentThread().getStackTrace()[2].getLineNumber(); //for debug purposes: show in which line def(String) was called
        
        errorLineSeen.add((int)BZ);
        
		JLabel lError = new JLabel("Unknown command: \""+ input +"\" in line: "+ BZ +"!");
		lError.setForeground(Color.RED);
		
		if(errorFrame == null || !errorFrame.isDisplayable()) {
			errorFrame = new JFrame("Error");
			errorFrame.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
	        errorFrame.setSize(400, 150);
			List <Image> imgs = new ArrayList<Image>();
			imgs.add(new ImageIcon(getClass().getResource("resources/Holbein_Logo_128x128.png")).getImage());
			imgs.add(new ImageIcon(getClass().getResource("resources/Holbein_Logo_64x64.png")).getImage());
			imgs.add(new ImageIcon(getClass().getResource("resources/Holbein_Logo_32x32.png")).getImage());
			imgs.add(new ImageIcon(getClass().getResource("resources/Holbein_Logo_16x16.png")).getImage());
			imgs.add(new ImageIcon(getClass().getResource("resources/Holbein_Logo_8x8.png")).getImage());
			errorFrame.setIconImages(imgs);
			
	        errorPanel.setLayout(new BoxLayout(errorPanel, BoxLayout.PAGE_AXIS));
		}
		
		errorPanel.add(lError);
		errorFrame.add(errorScroll);
		errorFrame.setVisible(true);
		
		System.err.println("Unknown command: \""+ input +"\" in line: "+ BZ +"!");
		BZ++;
	}
	
	//calls the right method 
	private void execute(String command, int rest) {

		switch(command) {
		case "END":
			end();
			break;
		case "ADD":
			machine_add(rest);
			break;
		case "DLOAD":
			machine_dload(rest);
			break;
		case "DIV":
			machine_div(rest);
			break;
		case "JEQ":
			machine_jeq(rest);
			break;
		case "JGE":
			machine_jge(rest);
			break;
		case "JGT":
			machine_jgt(rest);
			break;
		case "JLE":
			machine_jle(rest);
			break;
		case "JLT":
			machine_jlt(rest);
			break;
		case "JNE":
			machine_jne(rest);
			break;
		case "JUMP":
			machine_jump(rest);
			break;
		case "LOAD":
			machine_load(rest);
			break;
		case "MULT":
			machine_mult(rest);
			break;
		case "STORE":
			machine_store(rest);
			break;
		case "SUB":
			machine_sub(rest);
			break;
		default:
			def(command+" "+rest);
			break;
		}
	}
	
	private void end() {
		stop = true;
		BZ++;
	}
	
	private void machine_end(int unused) {
		stop = true;
		BZ++;
	}
	
	private void machine_add(int number) {
		int temp = -1;
		try {
			temp = A + R[number];
		}catch(Exception e) {
			def(number+"is not a valid register!");
			stop = true;
			return;
		}
		
		SR = (byte) (SR & Byte.valueOf("-1111100",2)); //clear last 2 bits
		if(temp>0) {
			SR = (byte) (SR | Byte.valueOf("00000010",2)); //set bit before last to 1
		}else {
			if(temp<0) {
				SR = (byte) (SR | Byte.valueOf("00000001",2));//set last bit to 1
			}
		}
		if(temp<128&&temp>-129) {
			A=(byte)temp;
			SR = (byte) (SR & Byte.valueOf("-1111011",2));//set 6. bit to 0
		}else {								
			A = (byte)(temp & Integer.valueOf("11111111",2));
			SR = (byte) (SR | Byte.valueOf("00000100",2));//set 6. bit to 1
		}
		
		BZ++;
		
	}
	
	private void machine_dload(int number) {
		if(number<128&&number>-129) {
			A = (byte)number;
		}else {
			def(" '"+number+"'"+" is a too big number");
		}
		BZ++;
	}
	

	private void machine_div(int number) {
		int temp = -1;
		try {
			temp = A / R[number];
		}catch(Exception e) {
			def(number+"is not a valid register!");
			stop = true;
			return;
		}
		
		SR = (byte) (SR & Byte.valueOf("-1111100",2)); //set last 2 bits to 0
		if(temp>0) {
			SR = (byte) (SR | Byte.valueOf("00000010",2)); //set bit before last to 1
		}else {
			if(temp<0) {
				SR = (byte) (SR | Byte.valueOf("00000001",2));//set last bit to 1
			}
		}
		if(temp<128&&temp>-129) {
			A=(byte)temp;
			SR = (byte) (SR & Byte.valueOf("-1111011",2));//set 6. bit to 0
		}else {								
			A = (byte)(temp & Integer.valueOf("11111111",2));
			SR = (byte) (SR | Byte.valueOf("00000100",2));//set 6. bit to 1
		}
		BZ++;
	}
	
	private void machine_jeq(int number) {
		String status = String.format("%8s", Integer.toBinaryString(SR & 0xFF)).replace(' ', '0');
		if(status.charAt(6)=='0'&&status.charAt(7)=='0') {
			BZ=(byte)number;
		}else {
			BZ++;
		}
	}
	
	private void machine_jge(int number) {
		String status = String.format("%8s", Integer.toBinaryString(SR & 0xFF)).replace(' ', '0');
		if(status.charAt(7)=='0') {
			BZ=(byte)number;
		}else {
			BZ++;
		}
	}
	
	private void machine_jgt(int number) {
		String status = String.format("%8s", Integer.toBinaryString(SR & 0xFF)).replace(' ', '0');
		if(status.charAt(6)=='1') {
			BZ=(byte)number;
		}else {
			BZ++;
		}
	}
	
	private void machine_jle(int number) {
		String status = String.format("%8s", Integer.toBinaryString(SR & 0xFF)).replace(' ', '0');
		if(status.charAt(6)=='0') {
			BZ=(byte)number;
		}else {
			BZ++;
		}
	}
	
	private void machine_jlt(int number) {
		String status = String.format("%8s", Integer.toBinaryString(SR & 0xFF)).replace(' ', '0');
		if(status.charAt(7)=='0') {
			BZ=(byte)number;
		}else {
			BZ++;
		}
	}
	
	private void machine_jne(int number) {
		String status = String.format("%8s", Integer.toBinaryString(SR & 0xFF)).replace(' ', '0');
		if(status.charAt(6)=='1'||status.charAt(7)=='1') {
			BZ=(byte)number;
		}else {
			BZ++;
		}
	}
	
	private void machine_jump(int number) {
		BZ = (byte)number;
	}
	
	private void machine_load(int number) {
		try {
			A = R[number];
		}catch(Exception e) {
			def(number+"is not a valid register!");
			stop = true;
			return;
		}
		
		SR = (byte) (SR & Byte.valueOf("-1111100",2)); //set last 2 bits to 0
		if(A>0) {
			SR = (byte) (SR | Byte.valueOf("00000010",2)); //set bit before last to 1
		}else {
			if(A<0) {
				SR = (byte) (SR | Byte.valueOf("00000001",2));//set last bit to 1
			}
		}
		BZ++;
	}
	

	private void machine_mult(int number) {
		
		int temp = -1;
		try {
			temp = A * R[number];
		}catch(Exception e) {
			def(number+"is not a valid register!");
			stop = true;
			return;
		}
		
		SR = (byte) (SR & Byte.valueOf("-1111100",2)); //set last 2 bits to 0
		if(temp>0) {
			SR = (byte) (SR | Byte.valueOf("00000010",2)); //set bit before last to 1
		}else {
			if(temp<0) {
				SR = (byte) (SR | Byte.valueOf("00000001",2));//set last bit to 1
			}
		}
		if(temp<128&&temp>-129) {
			A=(byte)temp;

			SR = (byte) (SR & Byte.valueOf("-1111011",2));//set 6. bit to 0
		}else {								
			A = (byte)(temp & Integer.valueOf("11111111",2));
			SR = (byte) (SR | Byte.valueOf("00000100",2));//set 6. bit to 1
		}
		BZ++;
	}
	

	private void machine_store(int number) {
		
		try {
			R[number] = A;
		}catch(Exception e) {
			def(number+"is not a valid register!");
			stop = true;
			return;
		}
		BZ++;
	}
	
	private void machine_sub(int number) {
		int temp = -1;
		try {
			temp = A - R[number];
		}catch(Exception e) {
			def(number+"is not a valid register!");
			stop = true;
			return;
		}
		
		SR = (byte) (SR & Byte.valueOf("-1111100",2)); //set last 2 bits to 0
		if(temp>0) {
			SR = (byte) (SR | Byte.valueOf("00000010",2)); //set bit before last to 1
		}else {
			if(temp<0) {
				SR = (byte) (SR | Byte.valueOf("00000001",2));//set last bit to 1

			}
		}
		if(temp<128&&temp>-129) {
			A=(byte)temp;
			SR = (byte) (SR & Byte.valueOf("-1111011",2));//set 6. bit to 0
		}else {								
			A = (byte)(temp & Integer.valueOf("11111111",2));
			SR = (byte) (SR | Byte.valueOf("00000100",2));//set 6. bit to 1
		}
		BZ++;
	}
}

/* Just a simple program to test:
DLOAD 10
STORE 1
DLOAD 2
ADD 1
STORE 2
END
 */
