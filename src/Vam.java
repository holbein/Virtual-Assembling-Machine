
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Image;
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
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
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
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.undo.UndoManager;

/**
 * This class is the main class and when it is executed through {@link #main(String[])} the Virtual Assembling Machine starts running.
 * The classes {@link MyDocumentListener}, {@link MyWindowListener} and {@link RegisterWidth} are also used.
 * @author VictorOle
 * @author SBester001
 * @version 1.2.1
 */
@SuppressWarnings("serial")
public class Vam extends JFrame{
    private static final String version = "1.2.1";

    private static final int FRAME_WIDTH = 810;
    private static final int FRAME_HEIGHT = 600;

    HashSet<Integer> errorLineList = new HashSet<Integer>(); //List of lines with errors

    private JFrame processFrame;
    private JTable processTable;

    private boolean processing = false;
    boolean textChanged;
    UndoManager undoManager = new UndoManager();
    boolean flash = true;

    static final int REG_SR = 17;
    static final int REG_BZ = 16;
    static final int REG_A = 0;
    static final int NREGS = 15;

    //NB: fuer Regs[REG_SR] Aufbau: (0,0,0,0,0,Overflow,GreaterZero,SmallerZero)
    final int[] Regs = new int[18];

    final List <Image> holbeinLogos = new ArrayList<Image>(5);

    private final HashMap<String, Integer> assemblyLabels = new HashMap<String, Integer>();

    int numberOfLines = 1; //do not change this value here

    private JPanel panelLeft;
    JPanel lineNumbering = new JPanel();
    static final ImageIcon EMPTY = new ImageIcon(Vam.class.getResource("resources/empty_16x12.png"));
    static final ImageIcon ARROW = new ImageIcon(Vam.class.getResource("resources/arrow_16x12.png"));
    static final ImageIcon ERROR = new ImageIcon(Vam.class.getResource("resources/error_16x15.png"));
    static final ImageIcon ARROW_ERROR = new ImageIcon(Vam.class.getResource("resources/arrow_error_16x15.png"));
    JTextArea textArea = new JTextArea(numberOfLines, 1);

    private JPanel panelRight;
    private final JLabel[][] labels = new JLabel[3][19];
    private JButton start, oneStep, reset;

    private JFrame errorFrame; //small JFrame with error message, that pops up when there was an error
    private final JPanel errorPanel = new JPanel();
    private final JScrollPane errorScroll = new JScrollPane(errorPanel);

    // File menu items
    private JMenuItem save, saveAs, open, quit;

	JMenuItem undo;

	private JMenuItem redo;
    private String path = "";

    private RegisterWidth.Handler widthHandler = null;

    // Forwards
    private RegisterWidth.Handler widthHandler() {
        if (widthHandler == null) widthHandler = new RegisterWidth.int8Width();
        return widthHandler;
    }

    private boolean isOverflow (int value) { return widthHandler().isOverflow(value); }
    private int cast (int value) { return widthHandler().cast(value); }
    private String toBitString(int value) { return widthHandler().toBinaryString(value); }

    /**
     * Constructs a new Virtual Assembly Machine.
     */
    Vam() {
        setSize(FRAME_WIDTH, FRAME_HEIGHT);
        setMinimumSize(new Dimension(500, 385));
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

        addleftPanel();
        addRightPanel();

        reset();

        setMenu();

        setVisible(true);
        
        textArea.getDocument().addUndoableEditListener(undoManager);
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

    /**
     * Sets the {@link JMenuBar} at the top of the JFrame of {@link Vam}.
     */
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
                        askSave();
                    }
                });
        file.add(quit);

        save.setAccelerator(KeyStroke.getKeyStroke("control S")); //shortcut ctrl+s
        saveAs.setAccelerator(KeyStroke.getKeyStroke("control shift S")); //shortcut ctrl+shift+s
        open.setAccelerator(KeyStroke.getKeyStroke("control O")); //shortcut ctrl+o
        quit.setAccelerator(KeyStroke.getKeyStroke("control Q")); //shortcut ctrl+q

        ButtonGroup buttonGroup = new ButtonGroup();

        JRadioButton bit_buttons[] = {
            new JRadioButton(new AbstractAction ("Use 8 Bits"){
                @Override
                public void actionPerformed (ActionEvent e){
                    changeRegisterWidth(new RegisterWidth.int8Width());
                }
            }),
            new JRadioButton(new AbstractAction ("Use 16 Bits"){
                @Override
                public void actionPerformed (ActionEvent e){
                    changeRegisterWidth(new RegisterWidth.int16Width());
                }
            }),
            new JRadioButton(new AbstractAction ("Use 32 Bits"){
                @Override
                public void actionPerformed (ActionEvent e){
                    changeRegisterWidth(new RegisterWidth.int32Width());
                }
            })
        };
        
        
        undo = new JMenuItem(
                new AbstractAction("Undo", new ImageIcon(Vam.class.getResource("resources/arrow_undo.png"))) {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        undoManager.undo();
                        redo.setEnabled(true);
                        if(!undoManager.canUndo()) {
                        	undo.setEnabled(false);
                        }
                    }
                });
        undo.setEnabled(false);
        
        redo = new JMenuItem(
                new AbstractAction("Redo", new ImageIcon(Vam.class.getResource("resources/arrow_redo.png"))) {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        undoManager.redo();
                        undo.setEnabled(true);
                        if(!undoManager.canRedo()) {
                        	redo.setEnabled(false);
                        }
                    }
                });
        
        redo.setEnabled(false);
        
        undo.setAccelerator(KeyStroke.getKeyStroke("control Z")); //shortcut ctrl+z
        redo.setAccelerator(KeyStroke.getKeyStroke("control Y")); //shortcut ctrl+y

        JMenu edit = new JMenu("Edit");
        
        edit.add(undo);
        edit.add(redo);
        
        edit.addSeparator();
        
        JMenu subMenu = new JMenu("Nuber of Bits");
        
        for (JRadioButton rb : bit_buttons) {
            buttonGroup.add(rb);
            subMenu.add(rb);
        }
        bit_buttons[0].setSelected(true); // default is 8-bit
        
        edit.add(subMenu);

        JMenuItem showTable = new JMenuItem(new AbstractAction ("Show Table"){
            @Override
            public void actionPerformed (ActionEvent e){
                updateProcessTableStatus();
            }
        });
        
        JCheckBoxMenuItem editFlash = new JCheckBoxMenuItem(new AbstractAction ("Flash if not used"){
            @Override
            public void actionPerformed (ActionEvent e){
                flash = !flash;
            }
        });
        editFlash.setSelected(true); //default true --> flashing if not used
        editFlash.setAccelerator(KeyStroke.getKeyStroke("control alt F")); //shortcut ctrl+alt+F
        

        JMenu view = new JMenu("View");
        view.add(showTable);
        view.add(editFlash);

        JMenuBar mbar = new JMenuBar();
        mbar.add(file);
        mbar.add(edit);
        mbar.add(view);

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

    public void askSave() {
        if (textChanged) {
            JDialog saveDialog = new JDialog(this, "Save?");
            saveDialog.setIconImage(new ImageIcon(Vam.class.getResource("resources/disk.png")).getImage());
            saveDialog.setSize(320, 100);
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

    /**
     * Changes the register width saved in {@link widthHandler} to the selected width.
     * @param handler Object, who's instance implements {@link RegisterWidth.Handler} and represents the new register width.
     */
    private void changeRegisterWidth(RegisterWidth.Handler handler) {
        widthHandler = handler;
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

    private void updateProcessTableStatus() {        
        if (processFrame == null || !processFrame.isVisible()) {
            processFrame = new JFrame("Table of Processes");
            processFrame.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
            processFrame.setSize(700, 400);
            processFrame.setIconImages(holbeinLogos);
            processFrame.setAlwaysOnTop(true);

            String[] columnNames = {"Command", "SR", "BZ", "A", "R1", "R2", "R3", "R4", "R5", "R6", "R7", "R8", "R9", "R10", "R11", "R12", "R13", "R14", "R15"};
            String[][] data = {{"<Initial>", "0", "1", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0"}};
            DefaultTableModel model = new DefaultTableModel(data, columnNames){
                @Override
                public boolean isCellEditable(int row, int column) {
                   return false;
                }
            };
            processTable = new JTable(model);
            JScrollPane scrollProc = new JScrollPane(processTable);
            processTable.setFillsViewportHeight(true);
            processTable.getColumnModel().getColumn(0).setPreferredWidth(250);
            //Use if you want to disable being able moving the columns:  processingTable.getTableHeader().setReorderingAllowed(false);

            DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
            rightRenderer.setHorizontalAlignment(JLabel.RIGHT);

            for (int i=1; i<processTable.getColumnCount(); i++) {
                processTable.getColumnModel().getColumn(i).setCellRenderer(rightRenderer);
            }

            processFrame.add(scrollProc);
            processFrame.setVisible(true);
        }        
    }

    private void printLine(int line) {
        String[] row = new String[Regs.length+1];
        row[0] = line+": "+getLineNoComment(getTextInLine(line));
        row[1] = ""+Regs[REG_SR];
        row[2] = ""+Regs[REG_BZ];
        for (int i=0; i<=NREGS; i++) {
            row[i+3] = ""+Regs[i];
        }
        
        ((DefaultTableModel)processTable.getModel()).addRow(row);
        if (!processFrame.isVisible()){
            processFrame.setVisible(true);
        }
    }

    /**
     * Initializes the left panel including adding it to the {@link JFrame}.
     */
    private void addleftPanel() {
        panelLeft = new JPanel(new BorderLayout());
        JScrollPane scrollPane = new JScrollPane(panelLeft);

        textArea.getDocument().addDocumentListener(new MyDocumentListener(this));
        scrollPane.getVerticalScrollBar().setUnitIncrement(10); //sets the scroll-speed

        lineNumbering.setLayout(new GridLayout(textArea.getLineCount(), 3));
        lineNumbering.add(new JLabel(EMPTY));
        lineNumbering.add(new JLabel(String.valueOf(numberOfLines)+"  "));
        lineNumbering.add(new JLabel(":"));

        JPanel panelNorth = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.gridy = 0;
        c.gridx = 0;
        panelNorth.add(lineNumbering, c);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 1;
        c.weightx = 1;
        panelNorth.add(textArea, c);
        panelLeft.add(panelNorth, BorderLayout.NORTH);

        add(scrollPane);
    }


    /**
     * Is called to refresh the icons left of the line numbering.
     */
    private void reDrawLeftIcons() {
        lineNumbering.setLayout(new GridLayout(textArea.getLineCount(), 3));

        for(int lineNo=1; lineNo <= numberOfLines; ++lineNo) {
            JLabel lab = (JLabel) lineNumbering.getComponent(3*(lineNo-1));

            if (errorLineList.contains(lineNo)){
                if (Regs[REG_BZ] == lineNo) {
                    lab.setIcon(ARROW_ERROR);
                } else {
                    lab.setIcon(ERROR);
                }
            } else {
                if (Regs[REG_BZ] == lineNo) {
                    lab.setIcon(ARROW);
                } else {
                    lab.setIcon(EMPTY);
                }
            }
        }
    }

    private void addRightPanel() {
        panelRight = new JPanel(new GridBagLayout());

        labels[0][0] = new JLabel("SR", SwingConstants.CENTER);
        labels[0][1] = new JLabel("BZ", SwingConstants.CENTER);
        labels[0][2] = new JLabel("A", SwingConstants.CENTER);

        for (int i=1; i<=NREGS; ++i) {
            labels[0][i+2] = new JLabel("R"+(i), SwingConstants.CENTER);
        }

        String binaryZeros = toBitString(0);

        for(int i=0; i < Regs.length; i++) {
            labels[1][i] = new JLabel(binaryZeros, SwingConstants.CENTER);
            labels[2][i] = new JLabel("0", SwingConstants.CENTER);

            labels[0][i].setBorder(BorderFactory.createLineBorder(Color.BLACK));
            labels[1][i].setBorder(BorderFactory.createLineBorder(Color.BLACK));
            labels[2][i].setBorder(BorderFactory.createLineBorder(Color.BLACK));
        }

        GridBagConstraints c = new GridBagConstraints();
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

        add(panelRight);
    }

    //call this method, to update the values
    private void reDrawRightPanel() {
        labels[1][0].setText(toBitString(Regs[REG_SR]));
        labels[2][0].setText(Integer.toString(Regs[REG_SR]));
        labels[1][1].setText(widthHandler().toBinaryString(Regs[REG_BZ]));
        labels[2][1].setText(Integer.toString(Regs[REG_BZ]));
        for (int i=0; i<=NREGS; i++) {
            labels[1][i+2].setText(widthHandler().toBinaryString(Regs[i]));
            labels[2][i+2].setText(Integer.toString(Regs[i]));
        }
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
        if(processTable != null){
            DefaultTableModel model = (DefaultTableModel)processTable.getModel();
            while (1 < model.getRowCount()){
                model.removeRow(1);
            }
        }

        for (int i=0; i<Regs.length; i++) {
            Regs[i] = 0;
        }

        Regs[REG_BZ] = 1;
        processing = true;

        reDrawRightPanel();
        reDrawLeftIcons();
    }

    private void oneStep() {
        scanForLabels();

        if (processing && 0 < Regs[REG_BZ] && Regs[REG_BZ] <= textArea.getLineCount()) {
            if (processFrame != null && processFrame.isVisible()){
                int holdLine = Regs[REG_BZ];
                check(getTextInLine(Regs[REG_BZ]));
                printLine(holdLine);
            } else {
                check(getTextInLine(Regs[REG_BZ]));
            }
        }

        reDrawRightPanel();
        reDrawLeftIcons();
    }

    private void start() {
        scanForLabels();

        while (processing && 0 < Regs[REG_BZ] && Regs[REG_BZ] <= textArea.getLineCount()) {
            if (processFrame != null && processFrame.isVisible()){
                int holdLine = Regs[REG_BZ];
                check(getTextInLine(Regs[REG_BZ]));
                printLine(holdLine);
            } else {
                check(getTextInLine(Regs[REG_BZ]));
            }
        }

        reDrawRightPanel();
        reDrawLeftIcons();
    }

    /**
     * " JUMP  4   --jumps back to line 4" --> "JUMP  4"
     * @param input full {@link String} of the line
     * @return {@link String} with the comment removed that is then trimmed
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
        } catch (NumberFormatException e) {
            if (command.charAt(0) != 'J' || (value = getAssemblyLabelLine(arg)+1) <= 0) {
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
        } catch (Exception e) {
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

    public boolean isBadRegister(int register) {
        boolean bad = (register <= 0 || register >= NREGS);
        if (bad) {
            error(register+" in line: "+Regs[REG_BZ]+" is an invalid register!");
        }
        return bad;
    }

    private void setSignStatus(int value){
        Regs[REG_SR] = 0; // clear all bits
        if (value > 0) {
            Regs[REG_SR] |= 0b10; // set second last bit to 1
        } else if (value < 0) {
            Regs[REG_SR] |= 0b01;// set last bit to 1
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

    public void machine_LOAD(int register) {
        if (isBadRegister(register)) return;

        Regs[REG_A] = Regs[register];

        setSignStatus(Regs[REG_A]);
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

    public void machine_STORE(int register) {
        if (isBadRegister(register)) return;

        Regs[register] = Regs[REG_A];
        Regs[REG_BZ]++;
    }

    public void machine_ADD(int register) {
        if (isBadRegister(register)) return;

        int temp = Regs[REG_A] + Regs[register];

        safeNumberCast(temp);
        Regs[REG_BZ]++;

    }

    public void machine_SUB(int register) {
        if (isBadRegister(register)) return;

        int temp = -1;
        try {
            temp = Regs[REG_A] - Regs[register];
        } catch(Exception e) {
            error(register+" in line: "+Regs[REG_BZ]+" is not a valid register!");
            return;
        }

        safeNumberCast(temp);
        Regs[REG_BZ]++;
    }

    public void machine_MULT(int register) {
        if (isBadRegister(register)) return;

        int temp = -1;
        try {
            temp = Regs[REG_A] * Regs[register];
        }catch(Exception e) {
            def(""+register);
            return;
        }

        safeNumberCast(temp);
        Regs[REG_BZ]++;
    }

    public void machine_DIV(int register) {
        if (isBadRegister(register)) return;

        int temp = 0;
        try {
            temp = Regs[REG_A] / Regs[register];
        }catch(Exception e) {
            error("Division by zero in line: "+Regs[REG_BZ]+" is not allowed");
            return;
        }

        safeNumberCast(temp);
        Regs[REG_BZ]++;
    }

    public void machine_JUMP(int line) {
        if (isOverflow(line)) {
            error("invalid line number: " + line);
            return;
        }

        Regs[REG_BZ] = line;
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

    public void machine_END(int unused) {
        processing = false;
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
