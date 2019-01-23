
import java.awt.*;
import java.awt.event.ActionEvent;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;


public class Vam extends JFrame{
	
	private static final int FRAME_WIDTH = 800;
	private static final int FRAME_HEIGHT = 600;
	
	boolean stop;
	
	public static byte SR; //Aufbau: (0,0,0,0,0,Overflow,GraterZero,SmallerZero)
	public static byte BZ;
	public static byte A;
	public static byte R1;
	public static byte R2;
	public static byte R3;
	public static byte R4;
	public static byte R5;
	public static byte R6;
	public static byte R7;
	public static byte R8;
	public static byte R9;
	public static byte R10;
	public static byte R11;
	public static byte R12;
	public static byte R13;
	public static byte R14;
	public static byte R15;
	
	
	private int numberOfLines = 1;
	
	private JPanel panelLeft = new JPanel();
		private JScrollPane scrollPane = new JScrollPane(panelLeft);
		private JPanel lineNumbering = new JPanel();
			private JTextArea textArea = new JTextArea(numberOfLines, 30);
	
	private JPanel panelRight;
		private JLabel[][] labels = new JLabel[2][18];
		private JButton start;
		private JButton reset;
	
	private JFrame errorFrame; //small JFrame with error message, that pops up when there was an error 
	
	Vam(){
		setSize(FRAME_WIDTH, FRAME_HEIGHT);
		setResizable(false);
		setLayout(new GridLayout(1, 2));
		setTitle("Virtual Assembler Machine");
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		
		textArea.getDocument().addDocumentListener(new MyDocumentListener());
		scrollPane.getVerticalScrollBar().setUnitIncrement(10); //sets the scroll-speed
		
		lineNumbering.setLayout(new GridLayout(numberOfLines, 2));
		for(int i=0; i<numberOfLines; ++i) {
			if(numberOfLines<10) {
				lineNumbering.add(new JLabel(String.valueOf(numberOfLines)+"   "));
			} else {
				lineNumbering.add(new JLabel(String.valueOf(numberOfLines)));
			}
			lineNumbering.add(new JLabel(":"));
		}
		
		panelLeft.add(lineNumbering);
		panelLeft.add(textArea);
		
		reset();
		
		add(scrollPane);
		add(panelRight);
		setVisible(true);
	}
	
	public static void main(String args[]) {
		new Vam();
	}
	
	private void rightPanel(byte[] by) {
		panelRight = new JPanel();
		panelRight.setLayout(new GridLayout(19, 2));
		
		labels[0][0] = new JLabel("SR");
		labels[0][1] = new JLabel("BZ");
		labels[0][2] = new JLabel("A");
		
		for (int i=0; i<15; ++i) {
			labels[0][i+3] = new JLabel("R"+(i+1));
		}
		
		for(int i=0; i<18; i++) {
			labels[1][i] = new JLabel(String.format("%8s", Integer.toBinaryString(by[i] & 0xFF)).replace(' ', '0'));
			
			labels[0][i].setBorder(BorderFactory.createLineBorder(Color.BLACK));
			labels[1][i].setBorder(BorderFactory.createLineBorder(Color.BLACK));
		}
		
		for(int i=0; i<18; i++) {
			panelRight.add(labels[0][i]);
			panelRight.add(labels[1][i]);
		}
		
		start = new JButton(new AbstractAction("Start"){
            public void actionPerformed(ActionEvent e) {
            	start();
            }
		});

		reset = new JButton(new AbstractAction("Reset"){
            public void actionPerformed(ActionEvent e) {
            	reset();
            }
		});
		
		panelRight.add(start);
		panelRight.add(reset);
	}
	
	//call this method, to update the values 
	private void reDrawRightPanel(byte[] by) {
		for(int i=0; i<18; i++) {
			labels[1][i].setText(String.format("%8s", Integer.toBinaryString(by[i] & 0xFF)).replace(' ', '0'));
		}
	}
	
    class MyDocumentListener implements DocumentListener {
        final String newline = "\n";
 
        public void insertUpdate(DocumentEvent e) {
        	lineNumbering.setLayout(new GridLayout(textArea.getLineCount(), 2));
			while(textArea.getLineCount() > numberOfLines) {
				numberOfLines++;
				if(numberOfLines<10) {
					lineNumbering.add(new JLabel(String.valueOf(numberOfLines)+"  "));
				} else {
					lineNumbering.add(new JLabel(String.valueOf(numberOfLines)));
				}
				lineNumbering.add(new JLabel(":"));
			}
        }
        
        public void removeUpdate(DocumentEvent e) {
        	lineNumbering.setLayout(new GridLayout(textArea.getLineCount(), 2));
			while(textArea.getLineCount() < numberOfLines) {
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
		stop = false;
		SR = 0;
		BZ = 1;
		A = 0;
		R1 = 0;
		R2 = 0;
		R3 = 0;
		R4 = 0;
		R5 = 0;
		R6 = 0;
		R7 = 0;
		R8 = 0;
		R9 = 0;
		R10 = 0;
		R11 = 0;
		R12 = 0;
		R13 = 0;
		R14 = 0;
		R15 = 0;
		
		byte[] by = {SR, BZ, A, R1, R2, R3, R4, R5, R6, R7, R8, R9, R10, R11, R12, R13, R14, R15};
		if(panelRight == null) rightPanel(by);
		else reDrawRightPanel(by);
	}
	
	private void start() {
		while(!stop && 0 < BZ && BZ <= textArea.getLineCount()) {
			check(getTextInLine(BZ).trim());
		}
		byte[] by = {SR, BZ, A, R1, R2, R3, R4, R5, R6, R7, R8, R9, R10, R11, R12, R13, R14, R15};
		reDrawRightPanel(by);
	}

		//separates the command and the rest
	public void check(String input) {
		
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
		
		execute(com, Integer.parseInt(input.substring(space+1)));
	}

	
	private void def(String input) {//default
        String caller = Thread.currentThread().getStackTrace()[2].getMethodName(); //for debug purposes: shows in which method def(String) was called
        int lineNo = Thread.currentThread().getStackTrace()[2].getLineNumber(); //for debug purposes: show in which line def(String) was called
        
		JLabel lError = new JLabel("Unknown command: \""+ input +"\" in line: "+ BZ +"!");
		lError.setForeground(Color.RED);
		
		if(errorFrame == null || !errorFrame.isDisplayable()) {
			errorFrame = new JFrame("Error");
			errorFrame.setLayout(new BoxLayout(errorFrame, BoxLayout.PAGE_AXIS));
			errorFrame.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
	        	errorFrame.setSize(400, 100);
		}
		
		errorFrame.add(lError);
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
			add(rest);
			break;
		case "DLOAD":
			dload(rest);
			break;
		case "DIV":
			div(rest);
			break;
		case "JEQ":
			jeq(rest);
			break;
		case "JGE":
			jge(rest);
			break;
		case "JGT":
			jgt(rest);
			break;
		case "JLE":
			jle(rest);
			break;
		case "JLT":
			jlt(rest);
			break;
		case "JNE":
			jne(rest);
			break;
		case "JUMP":
			jump(rest);
			break;
		case "LOAD":
			load(rest);
			break;
		case "MULT":
			mult(rest);
			break;
		case "STORE":
			store(rest);
			break;
		case "SUB":
			sub(rest);
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
	
	private void add(int number) {
		int i = 0;
		int temp;
		switch(number) {
			case 1: i = R1; break;
			case 2: i = R2; break;
			case 3: i = R3; break;
			case 4: i = R4; break;
			case 5: i = R5; break;
			case 6: i = R6; break;
			case 7: i = R7; break;
			case 8: i = R8; break;
			case 9: i = R9; break;
			case 10: i = R10; break;
			case 11: i = R11; break;
			case 12: i = R12; break;
			case 13: i = R13; break;
			case 14: i = R14; break;
			case 15: i = R15; break;
			default: 
				System.out.println(number+"is not a valid register!");
				end();
				break;
		}
		temp=A+i;
		
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
	
	private void dload(int number) {
		if(number<128&&number>-129) {
			A = (byte)number;
		}else {
			System.out.println(number+" is too big");
		}
		BZ++;
	}
	
	private void div(int number) {
		int i = -1;
		switch(number) {
			case 1: i = R1; break;
			case 2: i = R2; break;
			case 3: i = R3; break;
			case 4: i = R4; break;
			case 5: i = R5; break;
			case 6: i = R6; break;
			case 7: i = R7; break;
			case 8: i = R8; break;
			case 9: i = R9; break;
			case 10: i = R10; break;
			case 11: i = R11; break;
			case 12: i = R12; break;
			case 13: i = R13; break;
			case 14: i = R14; break;
			case 15: i = R15; break;
			default: 
				System.out.println(number+"is not a valid register!");
				i=0;
				end();
				break;
		}
		int temp=A/i;

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
	
	private void jeq(int number) {
		String status = String.format("%8s", Integer.toBinaryString(SR & 0xFF)).replace(' ', '0');
		if(status.charAt(6)=='0'&&status.charAt(7)=='0') {
			BZ=(byte)number;
		}else {
			BZ++;
		}
	}
	
	private void jge(int number) {
		String status = String.format("%8s", Integer.toBinaryString(SR & 0xFF)).replace(' ', '0');
		if(status.charAt(7)=='0') {
			BZ=(byte)number;
		}else {
			BZ++;
		}
	}
	
	private void jgt(int number) {
		String status = String.format("%8s", Integer.toBinaryString(SR & 0xFF)).replace(' ', '0');
		if(status.charAt(6)=='1') {
			BZ=(byte)number;
		}else {
			BZ++;
		}
	}
	
	private void jle(int number) {
		String status = String.format("%8s", Integer.toBinaryString(SR & 0xFF)).replace(' ', '0');
		if(status.charAt(6)=='0') {
			BZ=(byte)number;
		}else {
			BZ++;
		}
	}
	
	private void jlt(int number) {
		String status = String.format("%8s", Integer.toBinaryString(SR & 0xFF)).replace(' ', '0');
		if(status.charAt(7)=='0') {
			BZ=(byte)number;
		}else {
			BZ++;
		}
	}
	
	private void jne(int number) {
		String status = String.format("%8s", Integer.toBinaryString(SR & 0xFF)).replace(' ', '0');
		if(status.charAt(6)=='1'||status.charAt(7)=='1') {
			BZ=(byte)number;
		}else {
			BZ++;
		}
	}
	
	private void jump(int number) {
		BZ = (byte)number;
	}
	
	private void load(int number) {
		byte i;
		switch(number) {
			case 1: i = R1; break;
			case 2: i = R2; break;
			case 3: i = R3; break;
			case 4: i = R4; break;
			case 5: i = R5; break;
			case 6: i = R6; break;
			case 7: i = R7; break;
			case 8: i = R8; break;
			case 9: i = R9; break;
			case 10: i = R10; break;
			case 11: i = R11; break;
			case 12: i = R12; break;
			case 13: i = R13; break;
			case 14: i = R14; break;
			case 15: i = R15; break;
			default: 
				System.out.println(number+"is not a valid register!");
				i=0;
				end();
				break;
		}
		A=i;
		
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
	
	private void mult(int number) {
		int i;
		int temp;
		switch(number) {
			case 1: i = R1; break;
			case 2: i = R2; break;
			case 3: i = R3; break;
			case 4: i = R4; break;
			case 5: i = R5; break;
			case 6: i = R6; break;
			case 7: i = R7; break;
			case 8: i = R8; break;
			case 9: i = R9; break;
			case 10: i = R10; break;
			case 11: i = R11; break;
			case 12: i = R12; break;
			case 13: i = R13; break;
			case 14: i = R14; break;
			case 15: i = R15; break;
			default: 
				System.out.println(number+"is not a valid register!");
				i=0;
				end();
				break;
		}
		temp=A*i;
		
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
	
	private void store(int number) {
		switch(number) {
			case 1: R1 = A; break;
			case 2: R2 = A; break;
			case 3: R3 = A; break;
			case 4: R4 = A; break;
			case 5: R5 = A; break;
			case 6: R6 = A; break;
			case 7: R7 = A; break;
			case 8: R8 = A; break;
			case 9: R9 = A; break;
			case 10: R10 = A; break;
			case 11: R11 = A; break;
			case 12: R12 = A; break;
			case 13: R13 = A; break;
			case 14: R14 = A; break;
			case 15: R15 = A; break;
			default: 
				System.out.println(number+"is not a valid register!");
				end();
				break;
		}
		BZ++;
	}
	
	private void sub(int number) {
		int i;
		int temp;
		switch(number) {
			case 1: i = R1; break;
			case 2: i = R2; break;
			case 3: i = R3; break;
			case 4: i = R4; break;
			case 5: i = R5; break;
			case 6: i = R6; break;
			case 7: i = R7; break;
			case 8: i = R8; break;
			case 9: i = R9; break;
			case 10: i = R10; break;
			case 11: i = R11; break;
			case 12: i = R12; break;
			case 13: i = R13; break;
			case 14: i = R14; break;
			case 15: i = R15; break;
			default: 
				System.out.println(number+"is not a valid register!");
				i=0;
				end();
				break;
		}
		temp=A-i;
		
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
