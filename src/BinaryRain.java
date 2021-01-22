import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import javax.swing.*;

public final class BinaryRain extends JDialog {
    public static void main(String[] args) {
        BinaryRain r = new BinaryRain();
        r.setVisible(true);
        r.start();
    }

    private BinaryRain() {
        try {
            init();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null, "Failed to init.\n" + ex, "BinaryRain", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
    }

    private Dimension size;
    private Color foreground, background;
    public char[] RAIN_CHARACTERS;
    private Font rainFont;

    private void init() {
        size = Toolkit.getDefaultToolkit().getScreenSize();
        foreground = Color.GREEN;
        background = Color.BLACK;
        rainFont = new Font("arial", Font.BOLD, 15);
        RAIN_CHARACTERS = new char[126 - 33 + 1];
        for (int c = 0, i = 33, l = RAIN_CHARACTERS.length; c < l; c++, i++)
            RAIN_CHARACTERS[c] = (char) i;
        setUndecorated(true);
//    setAlwaysOnTop(true);
//    setResizable(false);
//    setTitle("Binary Rain");
//    setLocationRelativeTo(null);
        BufferedImage cursor = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB_PRE);
        setCursor(Toolkit.getDefaultToolkit().createCustomCursor(cursor, new Point(8, 8), "Disable Cursor"));
        setSize(size);
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent event) {
                if ((event.isAltDown() && event.getKeyCode() == KeyEvent.VK_F4) || (event.getKeyCode() == KeyEvent.VK_ESCAPE)) {
                    setVisible(false);
                    System.exit(0);
                }
            }
        });
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent event) {
                if (!isRaining())
                    isStart = false;
                System.exit(0);
            }
        });
        add(panel, BorderLayout.CENTER);
    }

    private synchronized void newRain() {
        Rain r = new Rain((int) (Math.random() * 40 + 10), (int) (Math.random() * size.width), (int) (Math.random() * -60 * 15), (int) (Math.random() * 8 + 2), (float) (Math.random() * 10 + 10));
        rains.add(r);
        new Thread(r).start();
    }

    private ArrayList<Rain> rains = new ArrayList<>();

    public void start() {
        for (int c = 0, s = 108; c < s; c++)
            newRain();
        isStart = true;
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (isStart)
                    panel.repaint();
            }
        }).start();
    }

    public boolean isRaining() {
        return isStart;
    }

    public String getRandomChar() {
        return String.valueOf(RAIN_CHARACTERS[(int) (Math.random() * RAIN_CHARACTERS.length)]);
    }

    private RainPanel panel = new RainPanel();
    private boolean isStart = false;

    private final class RainPanel extends JPanel {
        @Override
        public void paint(Graphics g) {
            if (isStart) {
                BufferedImage img = new BufferedImage(size.width, size.height, BufferedImage.TYPE_INT_RGB);
                Graphics2D g2 = (Graphics2D) img.getGraphics();
                g2.setColor(background);
                g2.fillRect(0, 0, size.width, size.height);
                g2.setColor(foreground);
                Collection<Rain> collection = (Collection<Rain>) rains.clone();
                for (Iterator<Rain> it = collection.iterator(); it.hasNext(); ) {
                    Rain r = it.next();
                    if (r.isEnd()) {
                        rains.remove(r);
                        newRain();
                        continue;
                    }
                    g2.setFont(rainFont.deriveFont(r.fontSize));
                    String[] ss = r.getRainChars();
                    int x = r.rainX;
                    int y = r.rainY - ss.length * 15;
                    for (String s : ss) {
                        g2.drawString(s, x, y);
                        y += 15;
                    }
                }
                g.drawImage(img, 0, 0, this);
            }
        }
    }

    private final class Rain implements Runnable {
        private final String[] rainChars;
        private int rainSpeed;
        private int rainX, rainY;
        private float fontSize;

        public Rain(int length, int x, int y, int speed, float size) {
            rainChars = new String[length + 1];
            for (int i = 0; i < length; i++)
                rainChars[i] = getRandomChar();
            rainChars[length] = " ";
            this.rainX = x;
            this.rainY = y;
            this.rainSpeed = speed;
            this.fontSize = size;
        }

        @Override
        public void run() {
            while (isRaining() && rainY < size.height + (rainChars.length + 1) * 15) {
                if (rainSpeed <= 0)
                    break;
                try {
                    Thread.sleep(rainSpeed);
                } catch (InterruptedException ex) {
                }
                rainY += 2;
            }
            rainSpeed = -1;
        }

        public String[] getRainChars() {
            return rainChars;
        }

        public boolean isEnd() {
            return rainSpeed <= 0;
        }
    }

}