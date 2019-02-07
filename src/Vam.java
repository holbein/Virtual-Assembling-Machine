
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
import javax.swing.JRadioButtonMenuItem;
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
    /**
     * Version of the Virtual Assembly Machine.
     */
    private static final String VERSION = "1.2.1";

    /**
     * Width and Height of the JFrame of {@link Vam}.
     */
    private static final int FRAME_WIDTH = 810, FRAME_HEIGHT = 600;

    /**
     * {@link HashSet} of lines with errors in them.
     */
    HashSet<Integer> errorLineList = new HashSet<Integer>();

    /**
     * {@link JFrame} containing the {@link processTable}.
     * @see #addProcessTable()
     */
    private final JFrame processFrame = new JFrame("Table of Processes");
    /**
     * Table in {@link processFrame} containing the values of the {@link Regs Registers} at the various points during the running of the Assembly program.
     * @see #addProcessTable()
     */
    private JTable processTable;

    /**
     * Determines, the next command in the Assembly program should be executed.
     */
    private boolean processing = false;
    /**
     * Determines, whether the icon of the {@link JFrame JFrames} should flash, when the frame is not focused.
     */
    boolean flash = true;

    /**
     * Position of the various registers in {@link Regs Regs[]}<p>
     * <code>REG_A</code> position of A (Accumulator = R0).</p><p>
     * <code>NREGS</code>  number of registers (excluding A = Accumulator = R0) (R1, R2, ...,R15 --&gt; NREGS = 15).</p><p>
     * <code>REG_BZ</code>  position of BZ (= command counter).</p><p>
     * <code>REG_SR</code>  position of SR (= status register).<br>
     * <b>Note</b> that if the corresponding bits are activated, that {@link Regs Regs[REG_A]} has the corresponding status.<br>
     * 0b100 stands for an overflow.<br>
     * 0b010 stands for {@link Regs Regs[REG_A]} &gt; 0<br>
     * 0b001 stands for {@link Regs Regs[REG_A]} &lt; 0</p>
     */
    static final int REG_A = 0, NREGS = 15, REG_BZ = 16, REG_SR = 17;

    /**
     * Array of Registers.<p>
     * To get the positions of the different registers see: {@link REG_A} {@link REG_BZ} {@link REG_SR}</p>
     */
    final int[] Regs = new int[18];

    /**
     * {@link List} of Holbein Logos in different sizes, used for the {@link JFrame} of {@link Vam}.
     */
    final List <Image> holbeinLogos = new ArrayList<Image>(5);

    /**
     * {@link HashMap} of labels names (= key), that point to a unique {@link Integer} value representing the line numbering.
     * @see lineNumbering
     */
    private final HashMap<String, Integer> assemblyLabels = new HashMap<String, Integer>();

    /**
     * {@link JPanel} on the left side of the {@link JFrame} of {@link Vam}, containing the {@link JPanel} {@link lineNumbering} on the left
     * and the {@link textArea} filling the rest of the width of the panel.<p>
     * This panel is initialized in {@link addleftPanel()} and is added to the {@link JFrame} of {@link Vam} as part of a {@link JScrollPane}.</p>
     */
    private JPanel panelLeft;
    /**
     * Represents the number of lines the {@link textArea} currently has.<p>
     * Do not change the value assignment here to anything but <code>1</code>, or there will be problems with the Interface showing ting correctly.</p>
     * @see #addleftPanel()
     * @see #reDrawLeftIcons()
     */
    int numberOfLines = 1;
    /**
     * {@link JPanel} on the left side of {@link panelLeft} that has 3 columns and the same number of rows as {@link textArea}, where each cell is filled with a {@link JLabel}.<p>
     * The leftmost column has one of four status icons in it, the middle column has the line number in it and the right column a colon.</p><p>
     * <b>Note</b> that the line numbering starts at line 1 and increments from there line for line.</p>
     * @see EMPTY
     * @see ARROW
     * @see ERROR
     * @see ARROW_ERROR
     * @see numberOfLines
     */
    JPanel lineNumbering = new JPanel();
    /**
     * Images in the left column {@link lineNumbering} for the various statuses.<p>
     * <code>EMPTY</code> is a place holder.</p><p>
     * <code>ARROW</code> is a green arrow, signalizing which line the program is in.</p><p>
     * <code>ERROR</code> is a warning sign, signalizing in which line there is an error.</p><p>
     * <code>ARROW_ERROR</code> is a green arrow and a warning sign, for the case, that in the line the program is in, there is an error.</p>
     */
    static final ImageIcon EMPTY = new ImageIcon(Vam.class.getResource("resources/empty_16x12.png")),
                            ARROW = new ImageIcon(Vam.class.getResource("resources/arrow_16x12.png")),
                            ERROR = new ImageIcon(Vam.class.getResource("resources/error_16x15.png")),
                            ARROW_ERROR = new ImageIcon(Vam.class.getResource("resources/arrow_error_16x15.png"));
    /**
     * The area in {@link panelLeft}, where the user can write his Assembler code into.
     */
    JTextArea textArea = new JTextArea(numberOfLines, 1);

    /**
     * {@link JPanel} on the right side of the {@link JFrame} of {@link Vam}, with 3 columns and the one row more than the length of {@link Regs},
     * filled with {@link JLabel JLabels} from {@link labels}.<p>
     * The first column has the name of the register e.g. "BZ".<br>
     * The third column has the bit value of the register e.g. "00010111".<br>
     * The third column has the decimal value of the register e.g. "23".<br>
     * The last row has the three buttons: {@link start} {@link oneStep} {@link reset} in it.</p>
     * @see #addRightPanel()
     * @see #reDrawRightPanel()
     */
    private JPanel panelRight;
    /**
     * {@link JLabel JLabels} in {@link panelRight}, that have the name, the bit value and the decimal value of the {@link Regs Registers}.<p>
     * <b>Note:</b> the order doe not correspond to the order in the {@link Regs Registers}.</p>
     * @see #addRightPanel()
     * @see #reDrawRightPanel()
     */
    private final JLabel[][] labels = new JLabel[3][19];
    /**
     * Buttons in {@link panelRight}, beneath the {@link labels}. To see, what button does what, click one of the following links.
     * @see #start()
     * @see #oneStep()
     * @see #reset()
     */
    private JButton start, oneStep, reset;

    private JFrame errorFrame; //small JFrame with error message, that pops up when there was an error
    private final JPanel errorPanel = new JPanel();
    private final JScrollPane errorScroll = new JScrollPane(errorPanel);

    /**
     * {@link JMenuItem JMenuItems} that are in the {@link JMenu} "File", that is in the {@link JMenuBar} created in {@link #setMenu()}.
     * @see #save()
     * @see #saveAs()
     * @see #open()
     * @see #setMenu()
     */
    private JMenuItem save, saveAs, open, quit;
    /**
     * {@link JMenuItem} in the {@link JMenu} "Edit" to undo the last change in the {@link textArea}.
     * @see undoManager
     * @see textChanged
     * @see redo
     */
    JMenuItem undo;
    /**
     * {@link JMenuItem} in the {@link JMenu} "Edit" to redo the last change in the {@link textArea}.
     * @see undoManager
     * @see textChanged
     * @see undo
     */
    JMenuItem redo;
    /**
     * Shows, whether the text in the {@link textArea} has changed.
     * @see undoManager
     * @see undo
     * @see redo
     */
    boolean textChanged;
    /**
     * Manages the undoing and redoing of the text in the {@link textArea}.
     * @see undo
     * @see redo
     * @see textChanged
     */
    UndoManager undoManager = new UndoManager();

    /**
     * Path of the file to {@link save}, {@link saveAs} or {@link open}.
     * @see #save()
     * @see #saveAs()
     * @see #open()
     */
    private String path = "";

    /**
     * Reference to an object of the interface {@link RegisterWidth.Handler} in the class {@link RegisterWidth}.<p>
     * <b>Note:</b> Do not change the assignment here, without changing, which {@link JRadioButtonMenuItem} is clicked.</p>
     * @see RegisterWidth.int8Width
     * @see RegisterWidth.int16Width
     * @see RegisterWidth.int32Width
     */
    private RegisterWidth.Handler widthHandler = new RegisterWidth.int8Width();

    /**
     * Checks if the value is an overflow, based on the current number of bits.<p>
     * <b>Note:</b> Does not work for a 32-bit overflow.</p>
     * @param value to be checked for an overflow
     * @return true if the value is an overflow, otherwise false
     */
    private boolean isOverflow(int value) { return widthHandler.isOverflow(value); }
    /**
     * Casts the value into a number with the current number of bits.<p>
     * 8 bits: cast(200) = -56</p>
     * @param value to be cast
     * @return cast value
     */
    private int cast(int value) { return widthHandler.cast(value); }
    /**
     * Turns the value into a {@link String} of bits, corresponding to the value and with a space every byte.
     * @param value to be turned into a {@link String} of bits
     * @return {@link String} of bits, corresponding to the value and with a space every byte
     */
    private String toBitString(int value) { return widthHandler.toBinaryString(value); }

    /**
     * Constructs a new Virtual Assembly Machine.
     */
    Vam() {
        setSize(FRAME_WIDTH, FRAME_HEIGHT);
        setMinimumSize(new Dimension(420, 385));
        setLocationRelativeTo(null);
        setLayout(new GridLayout(1, 2));
        setTitle("Virtual Assembling Machine v." + VERSION);
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
     * Executes the program by calling the constructor {@link #Vam()}.
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
     * Sets the {@link JMenuBar} at the top of the {@link JFrame} of {@link Vam}.
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
                new AbstractAction("Open File...", new ImageIcon(Vam.class.getResource("resources/folder_explore.png"))) {
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

        JRadioButtonMenuItem bit_buttons[] = {
            new JRadioButtonMenuItem(new AbstractAction ("Use 8 Bits"){
                @Override
                public void actionPerformed (ActionEvent e){
                    changeRegisterWidth(new RegisterWidth.int8Width());
                    setMinimumSize(new Dimension(420, 385));
                }
            }),
            new JRadioButtonMenuItem(new AbstractAction ("Use 16 Bits"){
                @Override
                public void actionPerformed (ActionEvent e){
                    changeRegisterWidth(new RegisterWidth.int16Width());
                    setMinimumSize(new Dimension(490, 385));
                }
            }),
            new JRadioButtonMenuItem(new AbstractAction ("Use 32 Bits"){
                @Override
                public void actionPerformed (ActionEvent e){
                    changeRegisterWidth(new RegisterWidth.int32Width());
                    setMinimumSize(new Dimension(750, 385));
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
                    	if(undoManager.canRedo()) {
	                        undoManager.redo();
	                        undo.setEnabled(true);
                    	}
                        redo.setEnabled(undoManager.canRedo());
                    }
                });
        
        redo.setEnabled(false);
        
        undo.setAccelerator(KeyStroke.getKeyStroke("control Z")); //shortcut ctrl+z
        redo.setAccelerator(KeyStroke.getKeyStroke("control Y")); //shortcut ctrl+y

        JMenu edit = new JMenu("Edit");
        
        edit.add(undo);
        edit.add(redo);
        
        edit.addSeparator();
        
        JMenu subMenu = new JMenu("Number of Bits");
        
        for (JRadioButtonMenuItem rb : bit_buttons) {
            buttonGroup.add(rb);
            subMenu.add(rb);
        }
        bit_buttons[0].setSelected(true); // default is 8-bit
        
        edit.add(subMenu);

        JMenuItem showTable = new JMenuItem(new AbstractAction ("Show Table", new ImageIcon(Vam.class.getResource("resources/table.png"))){
            @Override
            public void actionPerformed (ActionEvent e){
                addProcessTable();
            }
        });
        
        JCheckBoxMenuItem editFlash = new JCheckBoxMenuItem(new AbstractAction ("Flash if not used", new ImageIcon(Vam.class.getResource("resources/colouredImages/yellow_16x16.png"))){
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

    /**
     * Is called when the {@link JMenuItem} {@link saveAs} is pressed.
     */
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

    /**
     * Is called when the {@link JMenuItem} {@link save} is pressed.
     */
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

    /**
     * Is called when the {@link JMenuItem} {@link open} is pressed.
     */
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

    /**
     * Is called in {@link open()}.
     * @param file that should be opened.
     */
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

    /**
     * Is called when the {@link JMenuItem} {@link quit} is pressed or when the the {@link MyWindowListener} notices the {@link JFrame} of {@link Vam} being closed.<p>
     * Opens a {@link JDialog}, asking the user, whether he wants to save the changes before exiting.</p>
     */
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
     * @param handler object, who's instance implements {@link RegisterWidth.Handler} and represents the new register width.
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

    /**
     * Adds a Table of Processes, by setting {@link processFrame} visible and adding {@link processTable} to it, when called by the {@link JMenuItem}.
     */
    private void addProcessTable() {        
        if (!processFrame.isVisible()) {
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
            processTable.getTableHeader().setReorderingAllowed(false);

            DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
            rightRenderer.setHorizontalAlignment(JLabel.RIGHT);

            for (int i=1; i<processTable.getColumnCount(); i++) {
                processTable.getColumnModel().getColumn(i).setCellRenderer(rightRenderer);
            }

            processFrame.add(scrollProc);
            processFrame.setVisible(true);
        }        
    }

    /**
     * Adds a line to the {@link processTable} in the {@link processFrame} with the values of the various {@link Regs Registers}.
     * @param line that the command is in in the editor (corresponds to the {@link lineNumbering} on the left side of the {@link textArea}).
     */
    private void printLine(int line) {
        if (assemblyLabels.containsValue(line)
            || getLineNoComment(getTextInLine(line)).equals("")
            || getLineNoComment(getTextInLine(line)).substring(0, 2).equals("--")) {
            return;
        }
        
        String[] row = new String[Regs.length+1];
        row[0] = line+": "+getLineNoComment(getTextInLine(line));
        row[1] = ""+Regs[REG_SR];
        row[2] = ""+Regs[REG_BZ];
        for (int i=0; i<=NREGS; i++) {
            row[i+3] = ""+Regs[i];
        }
        
        ((DefaultTableModel)processTable.getModel()).addRow(row);
    }

    /**
     * Initializes {@link panelLeft} including adding it to the {@link JFrame} of {@link Vam}.
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
     * Updates the icons in {@link lineNumbering}, that are left of the line numbers and in {@link panelLeft}.
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

    /**
     * Initializes {@link panelRight} including adding it to the {@link JFrame} of {@link Vam}.
     */
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

    /**
     * Updates the values of the {@link Regs Registers} displayed in {@link panelRight}, by changing the text of the corresponding {@link labels labels[][]}.
     */
    private void reDrawRightPanel() {
        labels[1][0].setText(toBitString(Regs[REG_SR]));
        labels[2][0].setText(Integer.toString(Regs[REG_SR]));
        labels[1][1].setText(widthHandler.toBinaryString(Regs[REG_BZ]));
        labels[2][1].setText(Integer.toString(Regs[REG_BZ]));
        for (int i=0; i<=NREGS; i++) {
            labels[1][i+2].setText(widthHandler.toBinaryString(Regs[i]));
            labels[2][i+2].setText(Integer.toString(Regs[i]));
        }
    }

    /**
     * Gets the text of one line in the {@link textArea}, that the user has put in.
     * @param line corresponding to the {@link lineNumbering} on the left side of the {@link textArea}.
     * @return full {@link String} of that line. (Use {@link getLineNoComment(String)} to remove potential comments and to trim leading or trailing spaces)
     */
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

    /**
     * Resets the Assembly program to the initial state.
     */
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

    /**
     * Only runs the command in the line, that {@link Regs Regs[REGS_BZ]} is in.
     * @see REG_BZ
     */
    private void oneStep() {
        scanForLabels();

        if (processing && 0 < Regs[REG_BZ] && Regs[REG_BZ] <= textArea.getLineCount()) {
            if (processFrame.isVisible()){
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
     * Runs the Assembly program from the line, that {@link Regs Regs[REGS_BZ]} is in.
     * @see REG_BZ
     */
    private void start() {
        scanForLabels();

        while (processing && 0 < Regs[REG_BZ] && Regs[REG_BZ] <= textArea.getLineCount()) {
            if (processFrame.isVisible()){
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
    /**
     * Checks if the line is syntactically correct and calls the method based on, what the what the command is.
     * @param input rough text of a line in the code, without new lines (= "\n") in it
     */
    private void check(String input) {
        input = getLineNoComment(input);

        //checks if the command is "END"
        if (input.equals("END")) {
            machine_END(-1);
            return;
        }
        
        //checks if the line is empty or there is only a commment in it.
        if(input.equals("") || input.substring(0, 2).equals("--")) {
        	Regs[REG_BZ]++;
        	return;
        }

        int space = input.indexOf(' ');
        if (space == -1) {
            int colon = input.indexOf(':');
            if (colon == -1) {
                defError(input);
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
            defError(input);
        } catch (Exception e) {
            error("Something else bad happened: " + input);
        }
    }

    /**
     * Default for an error message.
     * This method calls {@link #error error(String text)}, with the parameter being the input with some extra text added.<p>
     * <b>Note:</b> Only call this method, when {@link Regs Regs[REG_BZ]} corresponds to the line number you want to have the {@link ERROR ERROR-icon} appear in.</p>
     * @param input {@link String} of the command in the line in which there was the error.
     */
    private void defError(String input) {
        error("Unknown command: \""+ input + "\" in line: "+Regs[REG_BZ]+"!");
    }

    /**
     * Error message, that is printed and shown in the {@link errorFrame}.<p>
     * <b>Note:</b> Only call this method, when {@link Regs Regs[REG_BZ]} corresponds to the line number you want to have the {@link ERROR ERROR-icon} appear in.</p>
     * @param text {@link String} that should be printed and shown in the {@link errorFrame}.
     * @see errorPanel
     */
    @SuppressWarnings("unused")
    private void error(String text) {
        String caller = Thread.currentThread().getStackTrace()[2].getMethodName(); //for debug purposes: shows in which method error(String) was called
        int lineNo = Thread.currentThread().getStackTrace()[2].getLineNumber(); //for debug purposes: show in which line error(String) was called

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

    /**
     * Error message, that is printed and shown in the {@link errorFrame}.<p>
     * <b>Note:</b> If you only want to have an {@link ERROR ERROR-icon} appearing in one line, use the same line for line1 and line2.</p>
     * @param text {@link String} that should be printed and shown in the {@link errorFrame}.
     * @param line1 first line, in which there should be an {@link ERROR ERROR-icon} appearing on the left.
     * @param line2 second line, in which there should be an {@link ERROR ERROR-icon} appearing on the left.
     * @see errorPanel
     * @see lineNumbering
     */
    @SuppressWarnings("unused")
    private void labelError(String text, int line1, int line2) {
        String caller = Thread.currentThread().getStackTrace()[2].getMethodName(); //for debug purposes: shows in which method error(String) was called
        int lineNo = Thread.currentThread().getStackTrace()[2].getLineNumber(); //for debug purposes: show in which line error(String) was called

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

    /**
     * Scans the whole text of {@link textArea} and adds the labels to {@link assemblyLabels}.
     * If there are multiple same labels in different lines, {@link #labelError()} is called.
     */
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
     * Returns the line number, that a specific label is in.
     * @param tag lookup for possible label. Shall not contain leading/trailing space (i.e. trim ahead of time)
     * @return The corresponding line number or -1 if not found.
     */
    private int getAssemblyLabelLine(String tag) {
        return (assemblyLabels.containsKey(tag) ? assemblyLabels.get(tag) : -1);
    }

    /**
     * Checks if the register is a bad register i.e. smaller than zero or greater than {@link NREGS}
     * @param register number corresponding to the {@link Regs Regs[register]}
     * @return false if 0 &lt; register &lt; {@link NREGS} otherwise true.
     */
    public boolean isBadRegister(int register) {
        boolean bad = (register < 0 || register >= NREGS);
        if (bad) {
            error(register+" in line: "+Regs[REG_BZ]+" is an invalid register!");
        }
        return bad;
    }

    /**
     * Sets the last two bits of {@link Regs Regs[REG_SR]}, that show, whether the value is positive or negative.
     * @param value usually of {@link Regs Regs[REG_A]}, that should be checked whether it is positive or negative
     * @see REG_SR
     * @see REG_A
     */
    private void setSignStatus(int value){
        Regs[REG_SR] = 0; // clear all bits
        if (value > 0) {
            Regs[REG_SR] |= 0b10; // set second last bit to 1
        } else if (value < 0) {
            Regs[REG_SR] |= 0b01;// set last bit to 1
        }
    }

    /**
     * Sets all three used bits of {@link Regs Regs[REG_SR]}, that show, whether the value had an overflow and whether it is positive or negative.
     * @param value usually of {@link Regs Regs[REG_A]}, that should be checked
     * @see REG_SR
     * @see REG_A
     */
    private void safeNumberCast(int value){
        setSignStatus(value);

        Regs[REG_SR] &= (~0b100); // clear overflow bit

        if (isOverflow(value)) {
            Regs[REG_SR] |= 0b100; // set overflow bit
        }

        Regs[REG_A] = cast(value);
    }

    /**
     * Copies the value from a register into {@link Regs Regs[REG_A]}.
     * @param register {@link Regs Regs[register]}, that the value should be copied from
     * @see REG_A
     */
    public void machine_LOAD(int register) {
        if (isBadRegister(register)) return;

        Regs[REG_A] = Regs[register];

        setSignStatus(Regs[REG_A]);
        Regs[REG_BZ]++;
    }

    /**
     * Loads a value into {@link Regs Regs[REG_A]}.
     * @param number that should be loaded
     * @see REG_A
     */
    public void machine_DLOAD(int number) {
        if (isOverflow(number)) {
            error(number+" in line: "+Regs[REG_BZ]+" is a too big number");
            return;
        }
        setSignStatus(number);
        Regs[REG_A] = number;
        Regs[REG_BZ]++;
    }

    /**
     * Copies the value from {@link Regs Regs[REG_A]} into a register.
     * @param register {@link Regs Regs[register]}, that the value should be copied into
     * @see REG_A
     */
    public void machine_STORE(int register) {
        if (isBadRegister(register)) return;

        Regs[register] = Regs[REG_A];
        Regs[REG_BZ]++;
    }

    /**
     * Adds the value from a register with the value of {@link Regs Regs[REG_A]} and saves it in {@link Regs Regs[REG_A]}.
     * @param register {@link Regs Regs[register]}, that should be added to the value of {@link Regs Regs[REG_A]}
     * @see REG_A
     */
    public void machine_ADD(int register) {
        if (isBadRegister(register)) return;

        int temp = Regs[REG_A] + Regs[register];

        safeNumberCast(temp);
        Regs[REG_BZ]++;

    }

    /**
     * Subtracts the value from a register off of the value of {@link Regs Regs[REG_A]} and saves it in {@link Regs Regs[REG_A]}.
     * @param register {@link Regs Regs[register]}, that should be subtracted off of the value of {@link Regs Regs[REG_A]}
     * @see REG_A
     */
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

    /**
     * Multiplies the value from a register with the value of {@link Regs Regs[REG_A]} and saves it in {@link Regs Regs[REG_A]}.
     * @param register {@link Regs Regs[register]}, that should be multiplied with the value of {@link Regs Regs[REG_A]}
     * @see REG_A
     */
    public void machine_MULT(int register) {
        if (isBadRegister(register)) return;

        int temp = -1;
        try {
            temp = Regs[REG_A] * Regs[register];
        }catch(Exception e) {
            defError(""+register);
            return;
        }

        safeNumberCast(temp);
        Regs[REG_BZ]++;
    }

    /**
     * Divides value of {@link Regs Regs[REG_A]} by the value from a register and saves it in {@link Regs Regs[REG_A]}.
     * @param register {@link Regs Regs[register]}, that the value of {@link Regs Regs[REG_A]} should be divided by
     * @see REG_A
     */
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

    /**
     * Jumps into another line, by setting {@link Regs Regs[REG_BZ]} to <code>line</code>.
     * @param line to jump into
     * @see REG_BZ
     */
    public void machine_JUMP(int line) {
        if (isOverflow(line)) {
            error("invalid line number: " + line);
            return;
        }

        Regs[REG_BZ] = line;
    }

    /**
     * Jumps into another line, if {@link Regs Regs[REG_A]} is equal to 0, by setting {@link Regs Regs[REG_BZ]} to <code>line</code>.
     * @param line to jump into
     * @see REG_BZ
     * @see REG_A
     */
    public void machine_JEQ(int line) {
        if ((Regs[REG_SR] & 0b111) == 0) {
            // All status bits are clear
            machine_JUMP(line);
        } else {
            Regs[REG_BZ]++;
        }
    }

    /**
     * Jumps into another line, if {@link Regs Regs[REG_A]} is greater than or equal to 0, by setting {@link Regs Regs[REG_BZ]} to <code>line</code>.
     * @param line to jump into
     * @see REG_BZ
     * @see REG_A
     */
    public void machine_JGE(int line) {
        if ((Regs[REG_SR] & 0b001) == 0) {
            // Last status bit is clear
            machine_JUMP(line);
        } else {
            Regs[REG_BZ]++;
        }
    }

    /**
     * Jumps into another line, if {@link Regs Regs[REG_A]} is equal than 0, by setting {@link Regs Regs[REG_BZ]} to <code>line</code>.
     * @param line to jump into
     * @see REG_BZ
     * @see REG_A
     */
    public void machine_JGT(int line) {
        if ((Regs[REG_SR] & 0b010) != 0) {
            // Second last status bit is set
            machine_JUMP(line);
        } else {
            Regs[REG_BZ]++;
        }
    }

    /**
     * Jumps into another line, if {@link Regs Regs[REG_A]} is smaller than or equal to 0, by setting {@link Regs Regs[REG_BZ]} to <code>line</code>.
     * @param line to jump into
     * @see REG_BZ
     * @see REG_A
     */
    public void machine_JLE(int line) {
        if ((Regs[REG_SR] & 0b010) == 0) {
            // Second last status bit is clear
            machine_JUMP(line);
        } else {
            Regs[REG_BZ]++;
        }
    }

    /**
     * Jumps into another line, if {@link Regs Regs[REG_A]} is smaller than 0, by setting {@link Regs Regs[REG_BZ]} to <code>line</code>.
     * @param line to jump into
     * @see REG_BZ
     * @see REG_A
     */
    public void machine_JLT(int line) {
        if ((Regs[REG_SR] & 0b001) != 0) {
            // Last status bit is set
            machine_JUMP(line);
        } else {
            Regs[REG_BZ]++;
        }
    }

    /**
     * Jumps into another line, if {@link Regs Regs[REG_A]} is not equal to 0, by setting {@link Regs Regs[REG_BZ]} to <code>line</code>.
     * @param line to jump into
     * @see REG_BZ
     * @see REG_A
     */
    public void machine_JNE(int line) {
        if ((Regs[REG_SR] & 0b011) != 0) {
            // Either of the last status bits are set
            machine_JUMP(line);
        } else {
            Regs[REG_BZ]++;
        }
    }

    /**
     * Ends the running of the Assembly program.
     * @param unused not used
     */
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
