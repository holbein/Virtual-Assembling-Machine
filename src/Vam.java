import java.awt.*;
import java.awt.event.ActionEvent;
import javax.swing.*;

public class Vam extends JFrame{
	
	private static final int FRAME_WIDTH = 800;
	private static final int FRAME_HEIGHT = 800;
	
	public static byte SR = 0;
	public static byte BZ = 0;
	public static byte A = 0;
	public static byte R1 = 0;
	public static byte R2 = 0;
	public static byte R3 = 0;
	public static byte R4 = 0;
	public static byte R5 = 0;
	public static byte R6 = 0;
	public static byte R7 = 0;
	public static byte R8 = 0;
	public static byte R9 = 0;
	public static byte R10 = 0;
	public static byte R11 = 0;
	public static byte R12 = 0;
	public static byte R13 = 0;
	public static byte R14 = 0;
	public static byte R15 = 0;
	
	
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
            	start();
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
	
	private void start() {
		
	}
	
	private void def(String input) {//dafault
		System.out.println("unknown command: \"" + input +"\"");
	}
	
	private void execute(String command, String rest) {
		if(rest.charAt(0) == ' ' ) {
			switch(command) {
			case "ADD":
				add(Integer.valueOf(rest.substring(1)));
			case "DLOAD":
				dload(Integer.valueOf(rest.substring(1)));
			case "DIV":
				div(Integer.valueOf(rest.substring(1)));
			case "JER":
				jer(Integer.valueOf(rest.substring(1)));
			case "JGE":
				jge(Integer.valueOf(rest.substring(1)));
			case "JGT":
				jgt(Integer.valueOf(rest.substring(1)));
			case "JLE":
				jle(Integer.valueOf(rest.substring(1)));
			case "JLT":
				jlt(Integer.valueOf(rest.substring(1)));
			case "JNE":
				jne(Integer.valueOf(rest.substring(1)));
			case "JUMP":
				jump(Integer.valueOf(rest.substring(1)));
			case "LOAD":
				load(Integer.valueOf(rest.substring(1)));
			case "MULT":
				mult(Integer.valueOf(rest.substring(1)));
			case "STORE":
				store(Integer.valueOf(rest.substring(1)));
			case "SUB":
				sub(Integer.valueOf(rest.substring(1)));
			}
			
		}else {
			if(command.equals("END")) {
				end();
			}else {
				def(command+rest);
			}
		}
	}
	
	private void end() {
		//TODO end()
	}
	
	private void add(int number) {
		//TODO add()
	}
	
	private void dload(int number) {
		//TODO dload()
	}
	
	private void div(int number) {
		//TODO div()
	}
	
	private void jer(int number) {
		//TODO jer()
	}
	
	private void jge(int number) {
		//TODO jge()
	}
	
	private void jgt(int number) {
		//TODO jgt()
	}
	
	private void jle(int number) {
		//TODO jle()
	}
	
	private void jlt(int number) {
		//TODO jlt()
	}
	
	private void jne(int number) {
		//TODO jne()
	}
	
	private void jump(int number) {
		//TODO jump()
	}
	
	private void load(int number) {
		//TODO load()
	}
	
	private void mult(int number) {
		//TODO mult()
	}
	
	private void store(int number) {
		//TODO store()
	}
	
	private void sub(int number) {
		//TODO sub()
	}
	
	public void check(String input) {
		switch(input.charAt(0)){
		case 'A'://A
			switch(input.charAt(1)){
			case 'D'://AD
				switch(input.charAt(2)){
				case 'D'://ADD
					execute(input.substring(0, 3), input.substring(3));
				default:
					def(input);
				}
			default:
				def(input);
			}
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
						default:
							def(input);
						}
					default:
						def(input);
					}
				default:
					def(input);
				}
			case 'I'://DI
				switch(input.charAt(2)){
				case 'V'://DIV
					execute(input.substring(0, 3), input.substring(3));
				default:
					def(input);
				}
			default:
				def(input);
			}
		case 'E'://E
			switch(input.charAt(1)){
			case 'N'://EN
				switch(input.charAt(2)){
				case 'D'://END
					execute(input.substring(0, 3), input.substring(3));
				default:
					def(input);
				}
			default:
				def(input);
			}
			
		case 'J'://J
			switch(input.charAt(1)){
			case 'E'://JE
				switch(input.charAt(2)){
				case 'Q'://JEQ
					execute(input.substring(0, 3), input.substring(3));
				default:
					def(input);
				}
			default:
				def(input);
			}
			switch(input.charAt(1)){
			case 'G'://JG
				switch(input.charAt(2)){
				case 'E'://JGE
					execute(input.substring(0, 3), input.substring(3));
				case 'T'://JGT
					execute(input.substring(0, 3), input.substring(3));
				default:
					def(input);
				}
			default:
				def(input);
			}
			switch(input.charAt(1)){
			case 'L'://JL
				switch(input.charAt(2)){
				case 'E'://JLE
					execute(input.substring(0, 3), input.substring(3));
				case 'T'://JLT
					execute(input.substring(0, 3), input.substring(3));
				default:
					def(input);
				}
			default:
				def(input);
			}
			switch(input.charAt(1)){
			case 'N'://JN
				switch(input.charAt(2)){
				case 'E'://JNE
					execute(input.substring(0, 3), input.substring(3));
				default:
					def(input);
				}
			default:
				def(input);
			}
			switch(input.charAt(1)){
			case 'U'://JU
				switch(input.charAt(2)){
				case 'M'://JUM
					switch(input.charAt(3)){
					case 'P'://JUMP
						execute(input.substring(0, 4), input.substring(4));
					default:
						def(input);
					}
				default:
					def(input);
				}
			default:
				def(input);
			}
			
		case 'L'://L
			switch(input.charAt(1)){
			case 'O'://LO
				switch(input.charAt(2)){
				case 'A'://LOA
					switch(input.charAt(3)){
					case 'D'://LOAD
						execute(input.substring(0, 4), input.substring(4));
					default:
						def(input);
					}
				default:
					def(input);
				}
			default:
				def(input);
			}
			
		case 'M'://M
			switch(input.charAt(1)){
			case 'U'://MU
				switch(input.charAt(2)){
				case 'L'://MUL
					switch(input.charAt(3)){
					case 'T'://MULT
						execute(input.substring(0, 4), input.substring(4));
					default:
						def(input);
					}
				default:
					def(input);
				}
			default:
				def(input);
			}
			
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
						default:
							def(input);
						}
					default:
						def(input);
					}
				default:
					def(input);
				}
			case 'U'://SU
				switch(input.charAt(2)){
				case 'B'://SUB
					execute(input.substring(0, 3), input.substring(3));
				default:
					def(input);
				}
			default:
				def(input);
			}
		
		default:
			def(input);
		}
	}
	
	public static void main(String args[]) {
		new Vam();
	}
}
