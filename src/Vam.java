
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileNameExtensionFilter;


@SuppressWarnings("serial")
public class Vam extends JFrame{
	private static final String version = "1.1.0";

	private static final int FRAME_WIDTH = 800;
	private static final int FRAME_HEIGHT = 600;

	private HashSet<Integer> errorLineList = new HashSet<Integer>(); //List of lines with errors

	private boolean processing = true;

	private static final int REG_SR = 0;
	private static final int REG_BZ = 1;
	private static final int REG_A = 2;
	private static final int REG_OFFSET = 3;
	private static final int NREGS = 16;

	//NB: fuer Regs[REG_SR] Aufbau: (0,0,0,0,0,Overflow,GreaterZero,SmallerZero)
	private final byte[] Regs = new byte[19];

	private HashMap<String ,Byte> assemblyLabels = new HashMap<String, Byte>();

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

	private JMenuBar bar;
	private JMenu menu;
	private JMenuItem saveAs;
	private JMenuItem save;
	private JMenuItem open;

	private String path = ""; // Bsp.: D:\\Eigene Dateien\\Java Eclipse\\Virtual Assembling Machine\\

	Vam(){
		setSize(FRAME_WIDTH, FRAME_HEIGHT);
		setResizable(false);
		setLayout(new GridLayout(1, 2));
		setTitle("Virtual Assembling Machine v." + version);
		setDefaultCloseOperation(EXIT_ON_CLOSE);

		List <Image> imgs = new ArrayList<Image>();
		imgs.add(new ImageIcon(Vam.class.getResource("resources/Holbein_Logo_128x128.png")).getImage());
		imgs.add(new ImageIcon(Vam.class.getResource("resources/Holbein_Logo_64x64.png")).getImage());
		imgs.add(new ImageIcon(Vam.class.getResource("resources/Holbein_Logo_32x32.png")).getImage());
		imgs.add(new ImageIcon(Vam.class.getResource("resources/Holbein_Logo_16x16.png")).getImage());
		imgs.add(new ImageIcon(Vam.class.getResource("resources/Holbein_Logo_8x8.png")).getImage());
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

	public static void main(String args[]){
		new Vam();
		/*Vam vam = new Vam();
		if(args.length > 0) {
			File file = new File(args[0]); //File Association
			vam.openFile(file);
		}*/

	}

	private void setMenu() {
		bar = new JMenuBar();
		menu = new JMenu("File");
		saveAs = new JMenuItem("Save As...", new ImageIcon(Vam.class.getResource("resources/disk_....png")));
		save = new JMenuItem("Save", new ImageIcon(Vam.class.getResource("resources/disk.png")));
		open = new JMenuItem("Open File...", new ImageIcon(Vam.class.getResource("resources/folder_explore.png")));


		saveAs.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				saveAs();
			}
		});

		save.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				save();
			}
		});

		open.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				open();
			}
		});

		save.setAccelerator(KeyStroke.getKeyStroke('S', Toolkit.getDefaultToolkit ().getMenuShortcutKeyMask())); //shortcut ctrl+s
		open.setAccelerator(KeyStroke.getKeyStroke('O', Toolkit.getDefaultToolkit ().getMenuShortcutKeyMask())); //shortcut ctrl+o

		menu.add(save);
		menu.add(saveAs);
		menu.add(open);

		save.setEnabled(false);

		bar.add(menu);

		setJMenuBar(bar);
	}

	private void saveAs() {
		JFileChooser choose = new JFileChooser();
		choose.setFileSelectionMode(JFileChooser.FILES_ONLY);
		choose.setFileFilter(new FileNameExtensionFilter("VAM program", "vam"));
		if(!path.equals("")) {
			choose.setSelectedFile(new File(path));
		}else {
			choose.setSelectedFile(new File("MyProgram.vam"));
		}

		if(choose.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
			File out = choose.getSelectedFile();
			path = out.getAbsolutePath();
			save.setEnabled(true);
			try {
				PrintWriter writer = new PrintWriter(out);
				writer.print(textArea.getText());
				writer.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private void save() {
		if(!path.equals("")) {
			File out = new File(path);
			try {
				PrintWriter writer = new PrintWriter(out);
				writer.print(textArea.getText());
				writer.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}else {
			save.setEnabled(false);
		}
	}

	private void open() {
		JFileChooser choose = new JFileChooser();
		choose.setFileSelectionMode(JFileChooser.FILES_ONLY);
		choose.setFileFilter(new FileNameExtensionFilter("VAM program", "vam"));
		if(!path.equals("")) {
			choose.setSelectedFile(new File(path));
		}

		if(choose.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
			File file = choose.getSelectedFile();
			openFile(file);
		}
	}

	public void openFile(File file){
		path = file.getAbsolutePath();
		save.setEnabled(true);
		try {
			BufferedReader br = new BufferedReader( new FileReader(file));
			Object[] str = br.lines().toArray();
			String whole = "";
			for(int i = 0; i< str.length; i++) {
				whole += str[i]+"\n";
			}
			br.close();
			textArea.setText(whole);

		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	private void rightPanel() {
		panelRight = new JPanel();
		panelRight.setLayout(new GridLayout(20, 3));

		labels[0][REG_SR] = new JLabel("SR", SwingConstants.CENTER);
		labels[0][REG_BZ] = new JLabel("BZ", SwingConstants.CENTER);
		labels[0][REG_A] = new JLabel("A", SwingConstants.CENTER);

		for (int i=REG_OFFSET; i<Regs.length; ++i) {
			labels[0][i] = new JLabel("R"+(i-REG_OFFSET), SwingConstants.CENTER);
		}

		for(int i=0; i < Regs.length; i++) {
			labels[1][i] = new JLabel("00000000", SwingConstants.CENTER);
			labels[2][i] = new JLabel("0", SwingConstants.CENTER);

			labels[0][i].setBorder(BorderFactory.createLineBorder(Color.BLACK));
			labels[1][i].setBorder(BorderFactory.createLineBorder(Color.BLACK));
			labels[2][i].setBorder(BorderFactory.createLineBorder(Color.BLACK));
		}

		for(int i=0; i < Regs.length; i++) {
			panelRight.add(labels[0][i]);
			panelRight.add(labels[1][i]);
			panelRight.add(labels[2][i]);
		}

		start = new JButton(new AbstractAction("Start") {
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
	private void reDrawRightPanel() {
		for(int i=0; i<Regs.length; i++) {
			labels[1][i].setText(String.format("%8s", Integer.toBinaryString(Regs[i] & 0xFF)).replace(' ', '0'));
			labels[2][i].setText(Byte.toString(Regs[i]));
		}
	}

	//call this method, to update the values
	private void reDrawLeftPanel() {
		lineNumbering.setLayout(new GridLayout(textArea.getLineCount(), 3));
		while(lineNumbering.getComponentCount() != 0) {
			lineNumbering.remove(0);
		}

		for(int lineNo=1; lineNo <= numberOfLines; ++lineNo) {
			if (errorLineList.contains(lineNo)){
				if (Regs[REG_BZ] == lineNo) {
					lineNumbering.add(new JLabel(ARROW_ERROR));
				} else {
					lineNumbering.add(new JLabel(ERROR));
				}
			}else {
				if (Regs[REG_BZ] == lineNo) {
					lineNumbering.add(new JLabel(ARROW_GREEN));
				} else {
					lineNumbering.add(new JLabel(ARROW_EMPTY));
				}
			}

			if(numberOfLines<10) {
				lineNumbering.add(new JLabel(String.valueOf(lineNo)+"   "));
			} else {
				lineNumbering.add(new JLabel(String.valueOf(lineNo)));
			}
			lineNumbering.add(new JLabel(":"));
		}
	}

    class MyDocumentListener implements DocumentListener {
        final String newline = "\n";

		public void insertUpdate(DocumentEvent e) {
        	lineNumbering.setLayout(new GridLayout(textArea.getLineCount(), 3));
			while (numberOfLines < textArea.getLineCount()) {
				numberOfLines++;

				if (errorLineList.contains(numberOfLines)){
					if (Regs[REG_BZ] == numberOfLines+1) {
						lineNumbering.add(new JLabel(ARROW_ERROR));
					} else {
						lineNumbering.add(new JLabel(ERROR));
					}
				}else {
					if (Regs[REG_BZ] == numberOfLines+1) {
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
		public void changedUpdate(DocumentEvent e) { /* unneeded */ }
    }

	// line is the same number as the numbering of the lines on the left side
    // find the corresponding text line from textArea
	private String getTextInLine(int line) {

		String text = textArea.getText();

		// init with -1 for error detection and to increment in first iteration
		int beg = -1, end = -1;
		for (int i=0; i<line; ++i) {
			beg = end+1;
		    end = text.indexOf('\n', beg);
		    if (end < 0) break;
		}
		if (beg < 0) {
	    	return "";
		} else if (end < 0) {
	    	return text.substring(beg);
	    }

		return text.substring(beg, end);
	}

	private void reset() {
		errorLineList.clear();
		errorPanel.removeAll();
		assemblyLabels.clear();

		for(int i=0; i<Regs.length; i++) {
			Regs[i] = 0;
		}

		Regs[REG_BZ] = 1;
		processing = true;

		if (panelRight == null) rightPanel();

		reDrawRightPanel();
		reDrawLeftPanel();
	}

	private void oneStep() {
//		System.out.println("| SR| BZ|  A| R0| R1| R2| R3| R4| R5| R6| R7| R8| R9|R10|R11|R12|R13|R14|R15");
		assemblyLabels.clear();
		for(int i=1; i<=textArea.getLineCount(); i++) {
			if(getTextInLine(i).trim().contains(":")) {
				if(!assemblyLabels.containsValue(i)) {
					addAssemblyLabel(getTextInLine(i).trim().substring(0, getTextInLine(i).trim().indexOf(':')).trim(),i);
				}
			}
		}

		if(processing && 0 < Regs[REG_BZ] && Regs[REG_BZ] <= textArea.getLineCount()){
			check(getTextInLine(Regs[REG_BZ]));
//			printValues();
		}

		reDrawRightPanel();
		reDrawLeftPanel();
	}

	private void start() {
//		System.out.println("| SR| BZ|  A| R0| R1| R2| R3| R4| R5| R6| R7| R8| R9|R10|R11|R12|R13|R14|R15");
		assemblyLabels.clear();
		for(int i=1; i<=textArea.getLineCount(); i++) {
			if(getTextInLine(i).trim().contains(":")) {
				if(!assemblyLabels.containsValue(i)) {
					addAssemblyLabel(getTextInLine(i).trim().substring(0, getTextInLine(i).trim().indexOf(':')).trim(), i);
				}
			}
		}

		while(processing && 0 < Regs[REG_BZ] && Regs[REG_BZ] <= textArea.getLineCount()) {
			check(getTextInLine(Regs[REG_BZ]));
//			printValues();
		}

		reDrawRightPanel();
		reDrawLeftPanel();
	}

	//separates the command and the rest
	private void check(String input) {

	    //remove comments
	    try {
	        input = input.substring(0, input.indexOf(" --"));
	    }catch(Exception e) {}

	    input = input.trim();

		if (input.equals("END")) {
			machine_END(-1);
			return;
		}

		int space = input.indexOf(' ');
		if (space == -1) {
			int colon = input.indexOf(':');
			if (colon == -1) {
				def(input);
				return;
			}
			if(assemblyLabels.containsKey(input.substring(0, colon)) && assemblyLabels.get(input.substring(0, colon)) != Regs[REG_BZ]) {
				labelError("Label: \""+input+"\", used in lines "+assemblyLabels.get(input.substring(0, colon))+" and "+Regs[REG_BZ]+", is only allowed to be used once!",
						assemblyLabels.get(input.substring(0, colon)), Regs[REG_BZ]);
				return;
			}
			++Regs[REG_BZ];
			return;
		}

        String command = input.substring(0, space).trim();
        String arg = input.substring(space+1).trim();

        // Convert to int, or see if it is a labelled line number (starts with 'J')
        int value = -1;
        try {
            value = Integer.parseInt(arg);
        } catch (NumberFormatException ex) {
            if (command.charAt(0) != 'J' || (value = getAssemblyLabelLine(arg)) <= 0) {
                error("Number format exception: \""+ input + "\" in line: "+Regs[REG_BZ]);
                return;
            }
        }

        try {
            Method meth = this.getClass().getMethod("machine_" + command, int.class); //Only for public methods!!!
            //public anyhow: meth.setAccessible(true);
            meth.invoke(this, value);
            return;
		} catch (NoSuchMethodException e) {
        	def(input);
        } catch (Exception ex) {
        	error("Something else bad happened: " + input);
		}
	}

	private void printValues() {
		for (int i=0; i<Regs.length; i++) {
			System.out.print("|"+String.format("%3s", Regs[i]));
		}
		System.out.println();
	}

	private void def(String input) {
        error("Unknown command: \""+ input + "\" in line: "+Regs[REG_BZ]+"!");
	}

	@SuppressWarnings("unused")
	private void error(String text) {
        String caller = Thread.currentThread().getStackTrace()[2].getMethodName(); //for debug purposes: shows in which method def(String) was called
        int lineNo = Thread.currentThread().getStackTrace()[2].getLineNumber(); //for debug purposes: show in which line def(String) was called

        errorLineList.add((int)Regs[REG_BZ]);

        System.err.println(caller+" "+lineNo);
		System.err.println(text);
		JLabel lError = new JLabel(text);
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

		processing = false;
		Regs[REG_BZ]++;
	}

	@SuppressWarnings("unused")
	private void labelError(String text, int line1, int line2) {
        String caller = Thread.currentThread().getStackTrace()[2].getMethodName(); //for debug purposes: shows in which method def(String) was called
        int lineNo = Thread.currentThread().getStackTrace()[2].getLineNumber(); //for debug purposes: show in which line def(String) was called

        errorLineList.add(line1);
        errorLineList.add(line2);

        System.err.println(caller+" "+lineNo);
		System.err.println(text);
		JLabel lError = new JLabel(text);
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

		processing = false;
		errorPanel.add(lError);
		errorFrame.add(errorScroll);
		errorFrame.setVisible(true);
		Regs[REG_BZ]++;
	}

	private void addAssemblyLabel(String assemblyLabel, int line) {
		try {//checks if the label could be mixed up with an integer value
			Integer.parseInt(assemblyLabel);
			labelError("Label: \""+assemblyLabel+":\" in line: "+line+" can be confused with a value.", line, line);
		} catch(NumberFormatException e) {
			if(assemblyLabel.length() != 0) {
				if(assemblyLabels.containsKey(assemblyLabel)) {
					labelError("Label: \""+assemblyLabel+":\", used in lines "+assemblyLabels.get(assemblyLabel)+" and "+line+", is only allowed to be used once!", assemblyLabels.get(assemblyLabel), line);
				} else {
					assemblyLabels.put(assemblyLabel, (byte)line);
					//System.out.println(assemblyLabels.toString());
				}
			} else {
				labelError("Label: \""+assemblyLabel+":\" in line: "+line+" is not allowed.", line, line);
			}
		}
	}

	private int getAssemblyLabelLine(String assemblyLabel) {
		if(assemblyLabels.containsKey(assemblyLabel.trim())) {
			return assemblyLabels.get(assemblyLabel.trim());
		}
		return -1;
	}

	public void machine_END(int unused) {
		processing = false;
		Regs[REG_BZ]++;
	}

	private void setSignStatus(int value){
		Regs[REG_SR] = (byte) (Regs[REG_SR] & Byte.valueOf("-1111100",2)); //clear last 2 bits
		if (value > 0) {
			Regs[REG_SR] = (byte) (Regs[REG_SR] | Byte.valueOf("00000010",2)); //set bit before last to 1
		}else if (value < 0) {
			Regs[REG_SR] = (byte) (Regs[REG_SR] | Byte.valueOf("00000001",2));//set last bit to 1
		}
	}

	private byte safeByteCast(int value){
		setSignStatus(value);

		if (Byte.MIN_VALUE <= value && value <= Byte.MAX_VALUE) {
			Regs[REG_SR] = (byte) (Regs[REG_SR] & Byte.valueOf("-1111011",2));//set 6. bit to 0
			return (byte)value;
		}else {
			Regs[REG_SR] = (byte) (Regs[REG_SR] | Byte.valueOf("00000100",2));//set 6. bit to 1

			return (byte)(value & Integer.valueOf("11111111",2));
		}
	}

	public void machine_ADD(int number) {
		processing = (number >= 0 && number < NREGS);
		if (!processing){
			error(number+" in line:"+Regs[REG_BZ]+" is not a valid register!");
			return;
		}

		int temp = Regs[REG_A] + Regs[number + REG_OFFSET];

		Regs[REG_A] = safeByteCast(temp);
		Regs[REG_BZ]++;

	}

	public void machine_DLOAD(int number) {
		if(number<128&&number>-129) {
			Regs[REG_A] = (byte)number;
		}else {
			error(number+" in line:"+Regs[REG_BZ]+" is a too big number");
		}
		Regs[REG_BZ]++;
	}

	public void machine_DIV(int number) {
		processing = (number >= 0 && number < NREGS);
		if (!processing){
			error(number+" in line:"+Regs[REG_BZ]+" is not a valid register!");
			return;
		}

		int temp = 0;
		try {
			temp = Regs[REG_A] / Regs[number+REG_OFFSET];
		}catch(Exception e) {
			error("Division by zero in line"+Regs[REG_BZ]+" is not allowed");
			return;
		}

		Regs[REG_A] = safeByteCast(temp);
		Regs[REG_BZ]++;
	}

	public void machine_JEQ(int number) {
		String status = String.format("%8s", Integer.toBinaryString(Regs[REG_SR] & 0xFF)).replace(' ', '0');
		if(status.charAt(6)=='0'&&status.charAt(7)=='0') {
			Regs[REG_BZ]=(byte)number;
		}else {
			Regs[REG_BZ]++;
		}
	}

	public void machine_JGE(int number) {
		String status = String.format("%8s", Integer.toBinaryString(Regs[REG_SR] & 0xFF)).replace(' ', '0');
		if(status.charAt(7)=='0') {
			Regs[REG_BZ]=(byte)number;
		}else {
			Regs[REG_BZ]++;
		}
	}

	public void machine_JGT(int number) {
		String status = String.format("%8s", Integer.toBinaryString(Regs[REG_SR] & 0xFF)).replace(' ', '0');
		if(status.charAt(6)=='1') {
			Regs[REG_BZ]=(byte)number;
		}else {
			Regs[REG_BZ]++;
		}
	}

	public void machine_JLE(int number) {
		String status = String.format("%8s", Integer.toBinaryString(Regs[REG_SR] & 0xFF)).replace(' ', '0');
		if(status.charAt(6)=='0') {
			Regs[REG_BZ]=(byte)number;
		}else {
			Regs[REG_BZ]++;
		}
	}

	public void machine_JLT(int number) {
		String status = String.format("%8s", Integer.toBinaryString(Regs[REG_SR] & 0xFF)).replace(' ', '0');
		if(status.charAt(7)=='0') {
			Regs[REG_BZ]=(byte)number;
		}else {
			Regs[REG_BZ]++;
		}
	}

	public void machine_JNE(int number) {
		String status = String.format("%8s", Integer.toBinaryString(Regs[REG_SR] & 0xFF)).replace(' ', '0');
		if(status.charAt(6)=='1'||status.charAt(7)=='1') {
			Regs[REG_BZ]=(byte)number;
		}else {
			Regs[REG_BZ]++;
		}
	}

	public void machine_JUMP(int number) {
		Regs[REG_BZ] = (byte)number;
	}

	public void machine_LOAD(int number) {
		processing = (number >= 0 && number < NREGS);
		if (!processing) {
			error(number+" in line:"+Regs[REG_BZ]+" is not a valid register!");
			return;
		}

		Regs[REG_A] = Regs[number+REG_OFFSET];

		setSignStatus(Regs[REG_A]);

		Regs[REG_BZ]++;
	}

	public void machine_MULT(int number) {
		processing = (number >= 0 && number < NREGS);
		if (!processing) {
			error(number+" in line:"+Regs[REG_BZ]+" is not a valid register!");
			return;
		}

		int temp = -1;
		try {
			temp = Regs[REG_A] * Regs[number+REG_OFFSET];
		}catch(Exception e) {
			def(""+number);
			return;
		}

		Regs[REG_A] = safeByteCast(temp);
		Regs[REG_BZ]++;
	}

	public void machine_STORE(int number) {
		try {
			Regs[number+REG_OFFSET] = Regs[REG_A];
		}catch(Exception e) {
			error(number+" in line:"+Regs[REG_BZ]+" is not a valid register!");
			return;
		}
		Regs[REG_BZ]++;
	}

	public void machine_SUB(int number) {
		int temp = -1;
		try {
			temp = Regs[REG_A] - Regs[number+REG_OFFSET];
		}catch(Exception e) {
			error(number+" in line:"+Regs[REG_BZ]+" is not a valid register!");
			return;
		}

		Regs[REG_A] = safeByteCast(temp);
		Regs[REG_BZ]++;
	}
}

/* Just a simple program to test:
DLOAD 10
STORE 1
DLOAD 2
ADD 1
STORE 2
END

DLOAD 1
STORE 0
DLOAD 3
asdf:
SUB 0
JGE asdf
END
 */
