import java.awt.GridLayout;
import java.util.ArrayList;

import javax.swing.JLabel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

class MyDocumentListener implements DocumentListener {
    final String newline = "\n";

    Vam parent;

    public MyDocumentListener(Vam parent) {
        this.parent = parent;
    }

    public void insertUpdate(DocumentEvent e) {
        parent.lineNumbering.setLayout(new GridLayout(parent.textArea.getLineCount(), 3));
        while (parent.numberOfLines < parent.textArea.getLineCount()) {
            parent.numberOfLines++;

            if (parent.errorLineList.contains(parent.numberOfLines)){
                if (parent.Regs[Vam.REG_BZ] == parent.numberOfLines+1) {
                    parent.lineNumbering.add(new JLabel(Vam.ARROW_ERROR));
                } else {
                    parent.lineNumbering.add(new JLabel(Vam.ERROR));
                }
            }else {
                if (parent.Regs[Vam.REG_BZ] == parent.numberOfLines+1) {
                    parent.lineNumbering.add(new JLabel(Vam.ARROW));
                } else {
                    parent.lineNumbering.add(new JLabel(Vam.EMPTY));
                }
            }

            if(parent.numberOfLines<10) {
                parent.lineNumbering.add(new JLabel(String.valueOf(parent.numberOfLines)+"  "));
            } else {
                parent.lineNumbering.add(new JLabel(String.valueOf(parent.numberOfLines)));
            }
            parent.lineNumbering.add(new JLabel(":"));
        }
        parent.textChanged = true;
        parent.undo.setEnabled(parent.undoManager.canUndo());
    }

    public void removeUpdate(DocumentEvent e) {
        parent.lineNumbering.setLayout(new GridLayout(parent.textArea.getLineCount(), 3));
        while(parent.textArea.getLineCount() < parent.numberOfLines) {
            parent.lineNumbering.remove(parent.lineNumbering.getComponentCount()-1);
            parent.lineNumbering.remove(parent.lineNumbering.getComponentCount()-1);
            parent.lineNumbering.remove(parent.lineNumbering.getComponentCount()-1);
            parent.numberOfLines--;
        }
        parent.textChanged = true;
        parent.undo.setEnabled(parent.undoManager.canUndo());
    }

    public void changedUpdate(DocumentEvent e) {
        parent.textChanged = true;
        parent.undo.setEnabled(parent.undoManager.canUndo());
    }

}
