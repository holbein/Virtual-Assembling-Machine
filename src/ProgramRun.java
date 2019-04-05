
public class ProgramRun extends Thread {

    private final Vam parent;

    public ProgramRun(Vam parent) {
        this.parent = parent;
    }

    @Override
    public void run() {
        while (parent.processing && (0 < parent.Regs[Vam.REG_BZ]) && (parent.Regs[Vam.REG_BZ] <= parent.textArea.getLineCount())) {
            if (parent.processFrame.isVisible()) {
                int holdLine = parent.Regs[Vam.REG_BZ];
                parent.check(parent.getTextInLine(parent.Regs[Vam.REG_BZ]));
                parent.printLine(holdLine);
            } else {
                parent.check(parent.getTextInLine(parent.Regs[Vam.REG_BZ]));
            }
            parent.reDrawRightPanel();
            parent.reDrawLeftIcons();
        }

        //checks if the register-machine has actually been stopped with the "END" command, before hitting the end of the code
        if(parent.processing && (parent.Regs[Vam.REG_BZ] > parent.textArea.getLineCount())){
            parent.error("Line "+parent.Regs[Vam.REG_BZ]+" not found. Maybe add \"END\" to the end of your code.");
        }

        parent.start.setText("Start");
        super.run();
    }
}

/* Endless Loop:

DLOAD 1
ADD 1
STORE 1
JUMP 1

 */
