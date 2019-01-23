import java.awt.*;
import java.awt.event.ActionEvent;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;


public class Vam extends JFrame{
	
	private static final int FRAME_WIDTH = 800;
	private static final int FRAME_HEIGHT = 800;
	
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
	
    class MyDocumentListener implements DocumentListener {
        final String newline = "\n";
 
        public void insertUpdate(DocumentEvent e) {
            if(textArea.getLineCount() > numberOfLines) {
				numberOfLines++;
				lineNumbering.setLayout(new GridLayout(numberOfLines, 2));
				if(numberOfLines<10) {
					lineNumbering.add(new JLabel(String.valueOf(numberOfLines)+"   "));
				} else {
					lineNumbering.add(new JLabel(String.valueOf(numberOfLines)));
				}
				lineNumbering.add(new JLabel(":"));
			}
        }
        
        public void removeUpdate(DocumentEvent e) {
            if(textArea.getLineCount() < numberOfLines) {
				numberOfLines--;
				lineNumbering.setLayout(new GridLayout(numberOfLines, 2));
				lineNumbering.remove(lineNumbering.getComponentCount()-1);
				lineNumbering.remove(lineNumbering.getComponentCount()-1);
			}
        }
        public void changedUpdate(DocumentEvent e) {
            //Plain text components don't fire these events.
        }
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
            	start();
            }
		});

		reset = new JButton(new AbstractAction("Reset"){
            public void actionPerformed(ActionEvent e) {
            	reset();
            }
		});
		
		ret.add(start);
		ret.add(reset);
		
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
		panelRight = rightPanel(by);
	}
	
	private void start() {
		while(!stop) {
			check(getTextInLine(BZ).trim());
		}
		byte[] by = {SR, BZ, A, R1, R2, R3, R4, R5, R6, R7, R8, R9, R10, R11, R12, R13, R14, R15};
		panelRight = rightPanel(by);
	}
	
	private void def(String input) {//default
		/*JFrame frame = new JFrame("Error");
		frame.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		frame.setSize(200, 100);
		
		JLabel lError = new JLabel("Unknown command: \"" + input +"\"!");
		lError.setForeground(Color.RED);
		
		frame.add(lError);
		frame.setVisible(true);*/        //Use only when there are no loop-bugs!!!!
		System.out.println("unknown command: \"" + input +"\"");
		BZ++;
	}
	
	//calls the right method 
	private void execute(String command, int rest) {
		switch(command) {
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
		case "END":
			end();
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
											//          11111111112222222222333
		}else {								// 12345678901234567890123456789012
			A = (byte)(temp & Integer.valueOf("00000000000000000000000011111111",2));
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
											//          11111111112222222222333
		}else {								// 12345678901234567890123456789012
			A = (byte)(temp & Integer.valueOf("00000000000000000000000011111111",2));
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
											//          11111111112222222222333
		}else {								// 12345678901234567890123456789012
			A = (byte)(temp & Integer.valueOf("00000000000000000000000011111111",2));
			SR = (byte) (SR | Byte.valueOf("00000100",2));//set 6. bit to 1
		}
		BZ++;
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
		/*
		switch(com) {
			case "LOAD": case "STORE": case "ADD": case "SUB": case "MULT": case "DIV":{
				try {
					int number = Integer.parseInt(input.substring(space+1));
					if(0 < number && number < 16) {
						execute(com, number);
					}else def(input);
				}catch(Exception e) {
					def(input);
				}
			}
			break;
			
			case "DLOAD":{
				try {
					int number = Integer.parseInt(input.substring(space+1));
					if(-129 < number && number < 128) {
						execute(com, number);
					}else def(input);
				}catch(Exception e) {
					def(input);
				}
			}
			break;
			
			case "JUMP": case "JGE": case "JGT": case "JLE": case "JLT": case "JEQ": case "JNE":{
				try {
					int number = Integer.parseInt(input.substring(space+1));
					if(0 < number && number <= textArea.getLineCount()) {
						execute(com, number);
					}
					else {
						def(input);
					}
				}catch(Exception e) {
					def(input);
				}
			}
			break;
			
			default: def(input);
		}
		/*
		switch(input.charAt(0)){
		case 'A'://A
			switch(input.charAt(1)){
			case 'D'://AD
				switch(input.charAt(2)){
				case 'D'://ADD
					execute(input.substring(0, 3), input.substring(3));
					break;
				default:
					def(input);
					break;
				}
				break;
			default:
				def(input);
				break;
			}
			break;
		case 'D'://D
			switch(input.charAt(1)){
			case 'L'://DL
				switch(input.charAt(2)){
				case 'O'://DLO
					switch(input.charAt(3)){
					case 'A'://DLOA
						switch(input.charAt(4)){
						case 'D'://DLOAD
							execute(input.substring(0, 5), input.substring(5));
							break;
						default:
							def(input);
							break;
						}
						break;
					default:
						def(input);
						break;
					}
					break;
				default:
					def(input);
					break;
				}
				break;
			case 'I'://DI
				switch(input.charAt(2)){
				case 'V'://DIV
					execute(input.substring(0, 3), input.substring(3));
					break;
				default:
					def(input);
					break;
				}
				break;
			default:
				def(input);
				break;
			}
			break;
		case 'E'://E
			switch(input.charAt(1)){
			case 'N'://EN
				switch(input.charAt(2)){
				case 'D'://END
					execute(input.substring(0, 3), input.substring(3));
					break;
				default:
					def(input);
					break;
				}
				break;
			default:
				def(input);
				break;
			}
			break;
			
		case 'J'://J
			switch(input.charAt(1)){
			case 'E'://JE
				switch(input.charAt(2)){
				case 'Q'://JEQ
					execute(input.substring(0, 3), input.substring(3));
					break;
				default:
					def(input);
					break;
				}
				break;
			default:
				def(input);
				break;
			case 'G'://JG
				switch(input.charAt(2)){
				case 'E'://JGE
					execute(input.substring(0, 3), input.substring(3));
					break;
				case 'T'://JGT
					execute(input.substring(0, 3), input.substring(3));
					break;
				default:
					def(input);
					break;
				}
				break;
			case 'L'://JL
				switch(input.charAt(2)){
				case 'E'://JLE
					execute(input.substring(0, 3), input.substring(3));
					break;
				case 'T'://JLT
					execute(input.substring(0, 3), input.substring(3));
					break;
				default:
					def(input);
					break;
				}
				break;
			case 'N'://JN
				switch(input.charAt(2)){
				case 'E'://JNE
					execute(input.substring(0, 3), input.substring(3));
					break;
				default:
					def(input);
					break;
				}
				break;
			case 'U'://JU
				switch(input.charAt(2)){
				case 'M'://JUM
					switch(input.charAt(3)){
					case 'P'://JUMP
						execute(input.substring(0, 4), input.substring(4));
						break;
					default:
						def(input);
						break;
					}
					break;
				default:
					def(input);
					break;
				}	
				break;			
			}
			break;
		case 'L'://L
			switch(input.charAt(1)){
			case 'O'://LO
				switch(input.charAt(2)){
				case 'A'://LOA
					switch(input.charAt(3)){
					case 'D'://LOAD
						execute(input.substring(0, 4), input.substring(4));
						break;
					default:
						def(input);
						break;
					}
					break;
				default:
					def(input);
					break;
				}
				break;
			default:
				def(input);
				break;
			}
			break;
			
		case 'M'://M
			switch(input.charAt(1)){
			case 'U'://MU
				switch(input.charAt(2)){
				case 'L'://MUL
					switch(input.charAt(3)){
					case 'T'://MULT
						execute(input.substring(0, 4), input.substring(4));
						break;
					default:
						def(input);
						break;
					}
					break;
				default:
					def(input);
					break;
				}
				break;
			default:
				def(input);
				break;
			}
			break;
			
		case 'S'://S
			switch(input.charAt(1)){
			case 'T'://ST
				switch(input.charAt(2)){
				case 'O'://STO
					switch(input.charAt(3)){
					case 'R'://STOR
						switch(input.charAt(4)){
						case 'E'://STORE
							execute(input.substring(0, 5), input.substring(5));
							break;
						default:
							def(input);
							break;
						}
						break;
					default:
						def(input);
						break;
					}
					break;
				default:
					def(input);
					break;
				}
				break;
			case 'U'://SU
				switch(input.charAt(2)){
				case 'B'://SUB
					execute(input.substring(0, 3), input.substring(3));
					break;
				default:
					def(input);
					break;
				}
				break;
			default:
				def(input);
				break;
			}
			break;
		
		default:
			def(input);
			break;
		}
		}*/
	}
	
	public static void main(String args[]) {
		new Vam();
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
