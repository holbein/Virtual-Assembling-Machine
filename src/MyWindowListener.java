import java.awt.Image;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ImageIcon;

public class MyWindowListener implements WindowListener{
    Vam parent;
    boolean iconified;

    private static final int sleepTime = 150;

    List <Image> red = new ArrayList<Image>();
    List <Image> yellow = new ArrayList<Image>();

    public MyWindowListener(Vam parent) {
        this.parent = parent;

        red.add(new ImageIcon(Vam.class.getResource("resources/colouredImages/red_128x128.png")).getImage());
        red.add(new ImageIcon(Vam.class.getResource("resources/colouredImages/red_64x64.png")).getImage());
        red.add(new ImageIcon(Vam.class.getResource("resources/colouredImages/red_32x32.png")).getImage());
        red.add(new ImageIcon(Vam.class.getResource("resources/colouredImages/red_16x16.png")).getImage());

        yellow.add(new ImageIcon(Vam.class.getResource("resources/colouredImages/yellow_128x128.png")).getImage());
        yellow.add(new ImageIcon(Vam.class.getResource("resources/colouredImages/yellow_64x64.png")).getImage());
        yellow.add(new ImageIcon(Vam.class.getResource("resources/colouredImages/yellow_32x32.png")).getImage());
        yellow.add(new ImageIcon(Vam.class.getResource("resources/colouredImages/yellow_16x16.png")).getImage());
    }

    @Override
    public void windowActivated(WindowEvent e) {
        iconified = false;
        parent.setIconImages(parent.holbeinLogos);
    }

    @Override
    public void windowClosed(WindowEvent e) {
        // TODO Auto-generated method stub
    }

    @Override
    public void windowClosing(WindowEvent e) {
        parent.askSave();
    }

    @Override
    public void windowDeactivated(WindowEvent e) {
        iconified = !parent.noFlash;
        new coloreChanger(parent).start();
    }

    @Override
    public void windowDeiconified(WindowEvent e) {
        iconified = false;
        parent.setIconImages(parent.holbeinLogos);
    }

    @Override
    public void windowIconified(WindowEvent e) {
        iconified = !parent.noFlash;
        new coloreChanger(parent).start();
    }

    @Override
    public void windowOpened(WindowEvent e) {
        // TODO Auto-generated method stub
    }

    class coloreChanger extends Thread {
        Vam parent;
        public coloreChanger(Vam parent) {
            this.parent = parent;
        }

        @Override
        public void run() {
            long i = 0;
            while(iconified) {
                if(i%2 == 0) {
                    parent.setIconImages(red);
                } else {
                    parent.setIconImages(yellow);
                }

                i++;
                try
                {
                    sleep(sleepTime);
                }
                catch (InterruptedException e1) {e1.printStackTrace(); }
            }
        }
    }
}

/****/
