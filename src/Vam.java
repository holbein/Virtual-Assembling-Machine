
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.net.URISyntaxException;
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
import javax.swing.JComponent;
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
import javax.swing.JTabbedPane;
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
 * @version 1.3.0
 */
@SuppressWarnings("serial")
public class Vam extends JTabbedPane{
	/**
     * Version of the Virtual Assembly Machine.
     */
    private static final String VERSION = "1.3.0";

    /**
     * Width and Height of the JFrame of {@link Tab}.
     */
    private static final int FRAME_WIDTH = 810, FRAME_HEIGHT = 600;
    
    /**
     * Determines, whether the icon of the {@link JFrame JFrames} should flash, when the frame is not focused.
     */
    boolean flash = true;
    
    /**
     * {@link List} of Holbein Logos in different sizes, used for the {@link JFrame} of {@link Tab}.
     */
    final List <Image> holbeinLogos = new ArrayList<Image>(5);

    /**
     * {@link JMenuItem JMenuItems} that are in the {@link JMenu} "File", that is in the {@link JMenuBar} created in {@link #setMenu()}.
     * @see #save()
     * @see #saveAs()
     * @see #open()
     * @see #setMenu()
     */
    private JMenuItem save, saveAs, open, newFile, quit;
    
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
     * Frame, that displays the commands.
     */
    private JFrame readMe = new JFrame("List of Commands");
    
    public Vam() {
		setSize(FRAME_WIDTH, FRAME_HEIGHT);
        setMinimumSize(new Dimension(420, 385));
        //TODO setLocationRelativeTo(null);
        setLayout(new GridLayout(1, 2));
        setName("Virtual Assembling Machine v." + VERSION);
        addWindowListener(new MyWindowListener(this));
        
        
        
	}
    
    
    
    
    
    
}
