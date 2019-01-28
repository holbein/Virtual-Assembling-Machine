
import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputMethodEvent;
import java.awt.event.InputMethodListener;
import java.io.*;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.filechooser.FileNameExtensionFilter;


@SuppressWarnings("serial")
public class Vam extends JFrame{
    private static final String version = "1.2.0";

    private static final int FRAME_WIDTH = 800;
    private static final int FRAME_HEIGHT = 600;

    HashSet<Integer> errorLineList = new HashSet<Integer>(); //List of lines with errors

    private boolean processing = false;
    boolean textChanged;

    private static final int REG_SR = 17;
    static final int REG_BZ = 16;
    private static final int REG_A = 0;
    private static final int NREGS = 15;

    //NB: fuer Regs[REG_SR] Aufbau: (0,0,0,0,0,Overflow,GreaterZero,SmallerZero)
    final int[] Regs = new int[18];

    List <Image> holbeinLogos = new ArrayList<Image>();

    private HashMap<String, Integer> assemblyLabels = new HashMap<String, Integer>();

    int numberOfLines = 1; //do not change this value here

    private JPanel panelLeft;
    private JScrollPane scrollPane;
    JPanel lineNumbering = new JPanel();
    static ImageIcon EMPTY = new ImageIcon(Vam.class.getResource("resources/empty_16x12.png"));
    static ImageIcon ARROW = new ImageIcon(Vam.class.getResource("resources/arrow_16x12.png"));
    static ImageIcon ERROR = new ImageIcon(Vam.class.getResource("resources/error_16x15.png"));
    static ImageIcon ARROW_ERROR = new ImageIcon(Vam.class.getResource("resources/arrow_error_16x15.png"));
    JTextArea textArea = new JTextArea(numberOfLines, 30);

    private JPanel panelRight;
    private JLabel[][] labels = new JLabel[3][19];
    private JButton start;
    private JButton oneStep;
    private JButton reset;

    private JFrame errorFrame; //small JFrame with error message, that pops up when there was an error
    private JPanel errorPanel = new JPanel();
    private JScrollPane errorScroll = new JScrollPane(errorPanel);

    // File menu items
    private JMenuItem save, saveAs, open, quit;
    private String path = "";

    public static String byteToString(int value) {
        return String.format("%8s", Integer.toBinaryString(value & 0xFF)).replace(' ', '0');
    }

    public interface registerWidth {
        public int width();

        // Check for overflow
        boolean isOverflow (int value);

        public int cast(int value);

        public String toBinaryString(int value);
    };

    public class int8Width implements registerWidth {
        public int width() { return 8; }

        public boolean isOverflow (int value) {
            return (value < Byte.MIN_VALUE || Byte.MAX_VALUE < value);
        }

        public int cast (int value) { return (byte) value; }

        public String toBinaryString(int value) {
            return byteToString(value & 0xFF);
        }
    };

    public class int16Width implements registerWidth {
        public int width() { return 16; }

        public boolean isOverflow (int value) {
            return (value < Short.MIN_VALUE || Short.MAX_VALUE < value);
        }

        public int cast(int value) { return (short) value; }

        public String toBinaryString(int value) {
            return String.join(" ",
                byteToString(((value & 0xFF00) >> 8) & 0xFF),
                byteToString(((value & 0x00FF) & 0xFF))
            );
        }
    };

    public class int32Width implements registerWidth {
        public int width() { return 32; }

        public boolean isOverflow (int value) {
            // Really?
            return (value < Integer.MIN_VALUE || Integer.MAX_VALUE < value);
        }

        public int cast(int value) { return value; }

        public String toBinaryString(int value) {
            return String.join(" ",
                byteToString(((value & 0xFF000000) >> 24) & 0xFF),
                byteToString(((value & 0x00FF0000) >> 16) & 0xFF),
                byteToString(((value & 0xFF00) >> 8) & 0xFF),
                byteToString(((value & 0x00FF) & 0xFF))
            );
        }
    };

    private registerWidth widthHandler_ = null;


    // Forwards
    private registerWidth widthHandler() {
        if (widthHandler_ == null) widthHandler_ = new int8Width();
        return widthHandler_;
    }
    private boolean isOverflow (int value) { return widthHandler().isOverflow(value); }
    private int cast (int value) { return widthHandler().cast(value); }


    Vam() {
        setSize(FRAME_WIDTH, FRAME_HEIGHT);
        setLocationRelativeTo(null);
        setLayout(new GridLayout(1, 2));
        setTitle("Virtual Assembling Machine v." + version);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new MyWindowListener(this));

        holbeinLogos.add(new ImageIcon(Vam.class.getResource("resources/Holbein_Logo_128x128.png")).getImage());
        holbeinLogos.add(new ImageIcon(Vam.class.getResource("resources/Holbein_Logo_64x64.png")).getImage());
        holbeinLogos.add(new ImageIcon(Vam.class.getResource("resources/Holbein_Logo_32x32.png")).getImage());
        holbeinLogos.add(new ImageIcon(Vam.class.getResource("resources/Holbein_Logo_16x16.png")).getImage());
        holbeinLogos.add(new ImageIcon(Vam.class.getResource("resources/Holbein_Logo_8x8.png")).getImage());
        setIconImages(holbeinLogos);

        reset();

        setMenu();

        add(scrollPane);
        add(panelRight);
        setVisible(true);

        textArea.addInputMethodListener(new InputMethodListener() {
            @Override
            public void inputMethodTextChanged(InputMethodEvent event) {
                textChanged = true;
            }
            @Override
            public void caretPositionChanged(InputMethodEvent event) {
                // TODO Auto-generated method stub
            }
        });
    }

    /**
     * Executes the program by calling the constructor {@link #Vam()}
     */
    public static void main(String args[]) {
        new Vam();
        /*Vam vam = new Vam();
        if (args.length > 0) {
            File file = new File(args[0]); //File Association
            vam.openFile(file);
        }*/

    }

    @SuppressWarnings("deprecation")
    private void setMenu() {
        JMenu file = new JMenu("File");

        save = new JMenuItem(
                new AbstractAction("Save", new ImageIcon(Vam.class.getResource("resources/disk.png"))) {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        save();
                    }
                });
        save.setEnabled(false);
        file.add(save);

        saveAs = new JMenuItem(
            new AbstractAction("Save As...", new ImageIcon(Vam.class.getResource("resources/disk_2.png"))) {
                @Override
                public void actionPerformed(ActionEvent e) {
                    saveAs();
                }
            });
        file.add(saveAs);

        open = new JMenuItem(
                new AbstractAction("Open File..", new ImageIcon(Vam.class.getResource("resources/folder_explore.png"))) {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        open();
                    }
                });
        file.add(open);

        quit = new JMenuItem(
                new AbstractAction("Quit", new ImageIcon(Vam.class.getResource("resources/cancel.png"))) {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        dispose();
                    }
                });
        file.add(quit);

        save.setAccelerator(KeyStroke.getKeyStroke('S', Toolkit.getDefaultToolkit ().getMenuShortcutKeyMask())); //shortcut ctrl+s
        open.setAccelerator(KeyStroke.getKeyStroke('O', Toolkit.getDefaultToolkit ().getMenuShortcutKeyMask())); //shortcut ctrl+o
        quit.setAccelerator(KeyStroke.getKeyStroke('Q', Toolkit.getDefaultToolkit ().getMenuShortcutKeyMask())); //shortcut ctrl+q

        ButtonGroup buttonGroup = new ButtonGroup();

        JRadioButton bit_buttons[] = {
            new JRadioButton(new AbstractAction ("Use 8 Bits"){
                @Override
                public void actionPerformed (ActionEvent e){
                    changeRegisterWidth(new int8Width());
                }
            }),
            new JRadioButton(new AbstractAction ("Use 16 Bits"){
                @Override
                public void actionPerformed (ActionEvent e){
                    changeRegisterWidth(new int16Width());
                }
            }),
            new JRadioButton(new AbstractAction ("Use 32 Bits"){
                @Override
                public void actionPerformed (ActionEvent e){
                    changeRegisterWidth(new int32Width());
                }
            })
        };


        JMenu edit = new JMenu("Edit");
        for (JRadioButton rb : bit_buttons) {
            buttonGroup.add(rb);
            edit.add(rb);
        }
        bit_buttons[0].setSelected(true); // default is 8-bit

        JMenuBar mbar = new JMenuBar();
        mbar.add(file);
        mbar.add(edit);

        setJMenuBar(mbar);
    }

    private void saveAs() {
        JFileChooser choose = new JFileChooser();
        choose.setFileSelectionMode(JFileChooser.FILES_ONLY);
        choose.setFileFilter(new FileNameExtensionFilter("VAM program", "vam"));
        if (!path.equals("")) {
            choose.setSelectedFile(new File(path));
        } else {
            choose.setSelectedFile(new File("MyProgram.vam"));
        }

        if (choose.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File out = choose.getSelectedFile();
            path = out.getAbsolutePath();
            save.setEnabled(true);
            try {
                PrintWriter writer = new PrintWriter(out);
                writer.print(textArea.getText());
                writer.close();
                textChanged = false;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void save() {
        if (!path.equals("")) {
            File out = new File(path);
            try {
                PrintWriter writer = new PrintWriter(out);
                writer.print(textArea.getText());
                writer.close();
                textChanged = false;
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            save.setEnabled(false);
        }
    }

    private void open() {
        JFileChooser choose = new JFileChooser();
        choose.setFileSelectionMode(JFileChooser.FILES_ONLY);
        choose.setFileFilter(new FileNameExtensionFilter("VAM program", "vam"));
        if (!path.equals("")) {
            choose.setSelectedFile(new File(path));
        }

        if (choose.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = choose.getSelectedFile();
            openFile(file);
        }
    }

    public void openFile(File file) {
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
            textChanged = false;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void onRegisterWidthChange() {
        if (isOverflow(Regs[REG_A])) {
            error("Overflow in A!");
        }

        safeNumberCast(Regs[REG_A]);

        if (isOverflow(Regs[REG_BZ])) {
            error("Overflow in BZ!");
        }
        Regs[REG_BZ] = cast(Regs[REG_BZ]);


        for(int i=1; i<=NREGS; i++) {
            if (isOverflow(Regs[i])) {
                error("Overflow in R"+(i));
            }
            Regs[i] = cast(Regs[i]);
        }

        reDrawRightPanel();
    }

    private void changeRegisterWidth(registerWidth handler) {
        widthHandler_ = handler;
        onRegisterWidthChange();
    }

    private void rightPanel() {
        panelRight = new JPanel();
        panelRight.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();

        labels[0][0] = new JLabel("SR", SwingConstants.CENTER);
        labels[0][1] = new JLabel("BZ", SwingConstants.CENTER);
        labels[0][2] = new JLabel("A", SwingConstants.CENTER);

        for (int i=1; i<=NREGS; ++i) {
            labels[0][i+2] = new JLabel("R"+(i), SwingConstants.CENTER);
        }

        String binaryZeros = widthHandler().toBinaryString(0);

        for(int i=0; i < Regs.length; i++) {
            labels[1][i] = new JLabel(binaryZeros, SwingConstants.CENTER);
            labels[2][i] = new JLabel("0", SwingConstants.CENTER);

            labels[0][i].setBorder(BorderFactory.createLineBorder(Color.BLACK));
            labels[1][i].setBorder(BorderFactory.createLineBorder(Color.BLACK));
            labels[2][i].setBorder(BorderFactory.createLineBorder(Color.BLACK));
        }

        c.fill = GridBagConstraints.BOTH;
        c.weighty = 1;
        for(int i=0; i < Regs.length; i++) {
            c.gridx = 0;
            c.gridy = i;
            panelRight.add(labels[0][i], c);
            c.weightx = 1;
            c.gridx = 1;
            panelRight.add(labels[1][i], c);
            c.weightx = 1;
            c.gridx = 2;
            panelRight.add(labels[2][i], c);
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

        c.gridy = Regs.length;
        c.gridx = 0;
        panelRight.add(start, c);
        c.gridx = 1;
        panelRight.add(oneStep, c);
        c.gridx = 2;
        panelRight.add(reset, c);
    }

    //call this method, to update the values
    private void reDrawRightPanel() {
        labels[1][0].setText(widthHandler().toBinaryString(Regs[REG_SR]));
        labels[2][0].setText(Integer.toString(Regs[REG_SR]));
        labels[1][1].setText(widthHandler().toBinaryString(Regs[REG_BZ]));
        labels[2][1].setText(Integer.toString(Regs[REG_BZ]));
        for (int i=0; i<=NREGS; i++) {
            labels[1][i+2].setText(widthHandler().toBinaryString(Regs[i]));
            labels[2][i+2].setText(Integer.toString(Regs[i]));
        }
    }

    private void leftPanel() {
        panelLeft = new JPanel();
        scrollPane = new JScrollPane(panelLeft);

        textArea.getDocument().addDocumentListener(new MyDocumentListener(this));
        scrollPane.getVerticalScrollBar().setUnitIncrement(10); //sets the scroll-speed

        lineNumbering.setLayout(new GridLayout(textArea.getLineCount(), 3));
        lineNumbering.add(new JLabel(EMPTY));
        lineNumbering.add(new JLabel(String.valueOf(numberOfLines)+"  "));
        lineNumbering.add(new JLabel(":"));

        panelLeft.add(lineNumbering);
        panelLeft.add(textArea);
    }


    //call this method, to update the values
    private void reDrawLeftIcons() {
        lineNumbering.setLayout(new GridLayout(textArea.getLineCount(), 3));

        for(int lineNo=1; lineNo <= numberOfLines; ++lineNo) {
            if (errorLineList.contains(lineNo)){
                if (Regs[REG_BZ] == lineNo) {
                    ((JLabel)lineNumbering.getComponent(3*(lineNo-1))).setIcon(ARROW_ERROR);
                } else {
                    ((JLabel)lineNumbering.getComponent(3*(lineNo-1))).setIcon(ERROR);
                }
            } else {
                if (Regs[REG_BZ] == lineNo) {
                    ((JLabel)lineNumbering.getComponent(3*(lineNo-1))).setIcon(ARROW);
                } else {
                    ((JLabel)lineNumbering.getComponent(3*(lineNo-1))).setIcon(EMPTY);
                }
            }
        }
    }

    /*class MyDocumentListener implements DocumentListener {
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
                } else {
                    if (Regs[REG_BZ] == numberOfLines+1) {
                        lineNumbering.add(new JLabel(ARROW));
                    } else {
                        lineNumbering.add(new JLabel(EMPTY));
                    }
                }

                if (numberOfLines<10) {
                    lineNumbering.add(new JLabel(String.valueOf(numberOfLines)+"  "));
                } else {
                    lineNumbering.add(new JLabel(String.valueOf(numberOfLines)));
                }
                lineNumbering.add(new JLabel(":"));
            }
            textChanged = true;
        }

        public void removeUpdate(DocumentEvent e) {
            lineNumbering.setLayout(new GridLayout(textArea.getLineCount(), 3));
            while (textArea.getLineCount() < numberOfLines) {
                lineNumbering.remove(lineNumbering.getComponentCount()-1);
                lineNumbering.remove(lineNumbering.getComponentCount()-1);
                lineNumbering.remove(lineNumbering.getComponentCount()-1);
                numberOfLines--;
            }
            textChanged = true;
        }

        public void changedUpdate(DocumentEvent e) {
            textChanged = true;
        }
    }*/

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
        if (panelLeft == null) leftPanel();

        reDrawRightPanel();
        reDrawLeftIcons();
    }

    private void oneStep() {
        // System.out.println("| SR| BZ|  A| R0| R1| R2| R3| R4| R5| R6| R7| R8| R9|R10|R11|R12|R13|R14|R15");
        scanForLabels();

        if (processing && 0 < Regs[REG_BZ] && Regs[REG_BZ] <= textArea.getLineCount()) {
            check(getTextInLine(Regs[REG_BZ]));
            // printValues();
        }

        reDrawRightPanel();
        reDrawLeftIcons();
    }

    private void start() {
        // System.out.println("| SR| BZ|  A| R0| R1| R2| R3| R4| R5| R6| R7| R8| R9|R10|R11|R12|R13|R14|R15");
        scanForLabels();

        while (processing && 0 < Regs[REG_BZ] && Regs[REG_BZ] <= textArea.getLineCount()) {
            check(getTextInLine(Regs[REG_BZ]));
            // printValues();
        }

        reDrawRightPanel();
        reDrawLeftIcons();
    }

    private void printValues() {
        for (int i=0; i<Regs.length; i++) {
            System.out.print("+---");
        }
        System.out.println();
        for (int i=0; i<Regs.length; i++) {
            String fmt = String.format("%d", Regs[i]);
            if (fmt.length() > 3) { fmt = fmt.substring(fmt.length()-3); }
            System.out.print("|" + fmt);
        }
        System.out.println();
    }

    /**
     * " JUMP  4   --jumps back to line 4" --> "JUMP  4"
     * @param input full {@link java.lang.String String} of the line
     * @return String with the comment removed that is then trimmed
     */
    private String getLineNoComment(String input) {
        // Strip out (trailing) comments of the form " --..."
        return input.replaceFirst("\\s--.*$", "").trim();
    }

    //separates the command and the rest
    private void check(String input) {
        input = getLineNoComment(input);

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
            if (assemblyLabels.containsKey(input.substring(0, colon)) && assemblyLabels.get(input.substring(0, colon)) != Regs[REG_BZ]) {
                labelError("Label: \""+input+"\", used in lines "+assemblyLabels.get(input.substring(0, colon))+" and "+Regs[REG_BZ]+", is only allowed to be used once!",
                        assemblyLabels.get(input.substring(0, colon)), Regs[REG_BZ]);
                return;
            }
            ++Regs[REG_BZ];
            return;
        }

        String command = input.substring(0, space).trim();
        String arg = input.substring(space+1).trim();

        // Convert to int, or see if it is a labeled line number (starts with 'J')
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

    private void def(String input) {
        error("Unknown command: \""+ input + "\" in line: "+Regs[REG_BZ]+"!");
    }

    @SuppressWarnings("unused")
    private void error(String text) {
        String caller = Thread.currentThread().getStackTrace()[2].getMethodName(); //for debug purposes: shows in which method def(String) was called
        int lineNo = Thread.currentThread().getStackTrace()[2].getLineNumber(); //for debug purposes: show in which line def(String) was called

        errorLineList.add(Regs[REG_BZ]);

        System.err.println(text);
        JLabel lError = new JLabel(text);
        lError.setForeground(Color.RED);

        if (errorFrame == null || !errorFrame.isDisplayable()) {
            errorFrame = new JFrame("Error");
            errorFrame.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
            errorFrame.setSize(400, 150);
            errorFrame.setIconImages(holbeinLogos);

            errorPanel.setLayout(new BoxLayout(errorPanel, BoxLayout.PAGE_AXIS));
        }
        
        errorPanel.add(lError);
        errorFrame.add(errorScroll);
        errorFrame.setLocationRelativeTo(this);
        errorFrame.setVisible(true);
        
        processing = false;
    }

    @SuppressWarnings("unused")
    private void labelError(String text, int line1, int line2) {
        String caller = Thread.currentThread().getStackTrace()[2].getMethodName(); //for debug purposes: shows in which method def(String) was called
        int lineNo = Thread.currentThread().getStackTrace()[2].getLineNumber(); //for debug purposes: show in which line def(String) was called

        errorLineList.add(line1);
        errorLineList.add(line2);

        System.err.println(text);
        JLabel lError = new JLabel(text);
        lError.setForeground(Color.RED);

        if (errorFrame == null || !errorFrame.isDisplayable()) {
            errorFrame = new JFrame("Error");
            errorFrame.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
            errorFrame.setSize(400, 150);
            errorFrame.setIconImages(holbeinLogos);

            errorPanel.setLayout(new BoxLayout(errorPanel, BoxLayout.PAGE_AXIS));
        }
        
        processing = false;
        errorPanel.add(lError);
        errorFrame.add(errorScroll);
        errorFrame.setLocationRelativeTo(this);
        errorFrame.setVisible(true);
    }

    public void askSave() {
        if (textChanged) {
            JDialog saveDialog = new JDialog(this, "Save?");
            saveDialog.setIconImage(new ImageIcon(Vam.class.getResource("resources/disk.png")).getImage());
            saveDialog.setSize(350, 150);
            saveDialog.setModal(true);
            saveDialog.setAlwaysOnTop(false);
            saveDialog.setLocationRelativeTo(this);

            JPanel savePanel = new JPanel();
            JLabel saveLabel = new JLabel("Do you want to save your changes before you exit?");
            JButton positive = new JButton("YES");
            positive.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    if (path.equals("")) {
                        saveAs();
                    } else {
                        save();
                    }
                    System.exit(0);
                }
            });

            JButton negative = new JButton("NO");
            negative.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    System.exit(0);
                }
            });

            savePanel.add(saveLabel);
            savePanel.add(positive);
            savePanel.add(negative);

            saveDialog.add(savePanel);
            saveDialog.setVisible(true);
        } else {
            System.exit(0);
        }
    }

    private void scanForLabels() {
        assemblyLabels.clear();

        for(int lineNo=1; lineNo<=textArea.getLineCount(); ++lineNo) {
            String tag = getLineNoComment(getTextInLine(lineNo));
            int colon = tag.indexOf(':');
            if (colon == -1) continue;

            tag = tag.substring(0, colon).trim();

            if (tag.matches("^\\d+$")) {
                labelError("Label: \""+tag+":\" in line: "+lineNo+" can be confused with a value.", lineNo, lineNo);
                continue;
            }

            if (tag.length() != 0) {
                int val = getAssemblyLabelLine(tag);
                if (val == -1) {
                    // Not previously there
                    assemblyLabels.put(tag, lineNo);
                    //System.out.println(assemblyLabels.toString());
                } else {
                    labelError("Label: \""+tag+":\", used in lines "+ val+" and "+lineNo+", is only allowed to be used once!", val, lineNo);
                }
            } else {
                labelError("Empty label not allowed in line: "+lineNo, lineNo, lineNo);
            }
        }
    }

    /**
     * @param tag lookup for possible label. Shall not contain leading/trailing space (ie, trimmed)
     * @return The corresponding line number or -1 if not found.
     */
    private int getAssemblyLabelLine(String tag) {
        return (assemblyLabels.containsKey(tag) ? assemblyLabels.get(tag) : -1);
    }

    public void machine_END(int unused) {
        processing = false;
        Regs[REG_BZ]++;
    }

    private void setSignStatus(int value){
        Regs[REG_SR] = 0; //clear all bits
        if (value > 0) {
            Regs[REG_SR] |= 0b10; //set second last bit to 1
        } else if (value < 0) {
            Regs[REG_SR] |= 0b01;//set last bit to 1
        }
    }

    private void safeNumberCast(int value){
        setSignStatus(value);

        Regs[REG_SR] &= (~0b100); // clear overflow bit

        if (isOverflow(value)) {
            Regs[REG_SR] |= 0b100; // set overflow bit
        }

        Regs[REG_A] = cast(value);
    }

    public void machine_ADD(int number) {
        if (isBadRegister(number)) return;

        int temp = Regs[REG_A] + Regs[number];

        safeNumberCast(temp);
        Regs[REG_BZ]++;

    }

    public void machine_DLOAD(int number) {
        if (isOverflow(number)) {
            error(number+" in line: "+Regs[REG_BZ]+" is a too big number");
            return;
        }
        setSignStatus(number);
        Regs[REG_A] = number;
        Regs[REG_BZ]++;
    }

    public void machine_DIV(int number) {
        if (isBadRegister(number)) return;

        int temp = 0;
        try {
            temp = Regs[REG_A] / Regs[number];
        }catch(Exception e) {
            error("Division by zero in line: "+Regs[REG_BZ]+" is not allowed");
            return;
        }

        safeNumberCast(temp);
        Regs[REG_BZ]++;
    }

    public void machine_JEQ(int line) {
        if ((Regs[REG_SR] & 0b111) == 0) {
            // All status bits are good (clear)
            machine_JUMP(line);
        } else {
            Regs[REG_BZ]++;
        }
    }

    public void machine_JGE(int line) {
        if ((Regs[REG_SR] & 0b001) == 0) {
            // Last status bit is good (clear)
            machine_JUMP(line);
        } else {
            Regs[REG_BZ]++;
        }
    }

    public void machine_JGT(int line) {
        if ((Regs[REG_SR] & 0b010) != 0) {
            // Second last status bit is set
            machine_JUMP(line);
        } else {
            Regs[REG_BZ]++;
        }
    }

    public void machine_JLE(int line) {
        if ((Regs[REG_SR] & 0b010) == 0) {
            // Second last status bit is clear
            machine_JUMP(line);
        } else {
            Regs[REG_BZ]++;
        }
    }

    public void machine_JLT(int line) {
        if ((Regs[REG_SR] & 0b001) == 0) {
            // Last status bit is clear
            machine_JUMP(line);
        } else {
            Regs[REG_BZ]++;
        }
    }

    public void machine_JNE(int line) {
        if ((Regs[REG_SR] & 0b011) != 0) {
            // Either of the last status bits are set
            machine_JUMP(line);
        } else {
            Regs[REG_BZ]++;
        }
    }

    public void machine_JUMP(int line) {
        if (isOverflow(line)) {
            error("invalid line number: " + line);
            return;
        }

        Regs[REG_BZ] = line;
    }

    public void machine_LOAD(int number) {
        if (isBadRegister(number)) return;

        Regs[REG_A] = Regs[number];

        setSignStatus(Regs[REG_A]);
        Regs[REG_BZ]++;
    }

    public void machine_MULT(int number) {
        if (isBadRegister(number)) return;

        int temp = -1;
        try {
            temp = Regs[REG_A] * Regs[number];
        }catch(Exception e) {
            def(""+number);
            return;
        }

        safeNumberCast(temp);
        Regs[REG_BZ]++;
    }


    public boolean isBadRegister(int number) {
        boolean bad = (number <= 0 || number >= NREGS);
        if (bad) {
            error(number+" in line: "+Regs[REG_BZ]+" is an invalid register!");
        }
        return bad;
    }

    public void machine_STORE(int number) {
        if (isBadRegister(number)) return;

        Regs[number] = Regs[REG_A];
        Regs[REG_BZ]++;
    }

    public void machine_SUB(int number) {
        processing = (number >= 0 && number <= NREGS);
        if (!processing) {
            error(number+" in line: "+Regs[REG_BZ]+" is not a valid register!");
            return;
        }

        int temp = -1;
        try {
            temp = Regs[REG_A] - Regs[number];
        } catch(Exception e) {
            error(number+" in line: "+Regs[REG_BZ]+" is not a valid register!");
            return;
        }

        safeNumberCast(temp);
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
