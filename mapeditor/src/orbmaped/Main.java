package orbmaped;

import java.awt.Canvas;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.BufferStrategy;
import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.filechooser.FileFilter;

public class Main extends Canvas implements ActionListener, MouseMotionListener {

    JFrame frame;
    Image imgTiles;
    Image imgOrb;
    BufferedImage tiles[];
    BufferStrategy bufStrategy;
    JMenu fileMenu, helpMenu, coordViewer;
    JMenuItem menuItems[];
    MouseHandler mouseHandler;
    boolean leftBtnHeld = false, middleBtnHeld = false, rightBtnHeld = false;
    boolean shiftKeyHeld = false;
    int dragX = 0, dragY = 0;
    int curX = 0, curY = 0;
    int tileX = 0, tileY = 0;
    int offX = 0, offY = 0;
    byte choosenBlock = 0;
    //int scrollX = 0, scrollY = 0;
    boolean paletteOpen = false;
    boolean changeSpawnpoint = false;
    // Map
    Map map = new Map();
    final JFileChooser fc = new JFileChooser();

    public Main() {
//        try {
//            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
//        } catch (Exception e) {
//            System.out.println("Error setting native Look'n'Feel: " + e);
//        }

        frame = new JFrame();
        frame.setTitle("The Orb Map Editor 0.1");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setBounds(0, 0, 800, 576);
        frame.getContentPane().add(this);
        frame.setJMenuBar(createMenu());
        frame.setResizable(false);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        fc.setFileFilter(new FileFilter() {

            @Override
            public boolean accept(File f) {
                if (f.isDirectory()) {
                    return true;
                } else if (f.getAbsolutePath().substring(f.getAbsolutePath().length() - 6, f.getAbsolutePath().length()).equals(".tomap")) {
                    return true;
                } else {
                    return false;
                }
            }

            @Override
            public String getDescription() {
                return "TOMap files";
            }
        });

        setBackground(new Color(0x000044));
        createBufferStrategy(2);
        setIgnoreRepaint(true);
        bufStrategy = getBufferStrategy();

        ActionListener painter = new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                drawStuff();
                if (!middleBtnHeld) {
                    coordViewer.setText(tileX + ", " + tileY);
                }
                //coordViewer.setText("cX=" + curX + ", cY=" + curY + ", dX=" + dragX + ", dY=" + dragY + ", oX=" + offX + ", oY=" + offY);
            }
        };
        new Timer(10, painter).start();

        mouseHandler = new MouseHandler();
        addMouseListener(mouseHandler);
        addMouseMotionListener(this);
        addKeyListener(new KeyHandler());

        resetMap();

        imgTiles = new ImageIcon(getClass().getResource("/res/tiles.png")).getImage();
        tiles = new BufferedImage[77];
        for (int i = 0; i < 77; i++) {
            tiles[i] = Utils.getTile(imgTiles, i * 32, 0, 32, 32);
        }
        imgOrb = new ImageIcon(getClass().getResource("/res/orb.png")).getImage();

        requestFocus();
    }

    private JMenuBar createMenu() {
        JMenuBar menuBar = new JMenuBar();

        fileMenu = new JMenu("File");
        helpMenu = new JMenu("Help");
        coordViewer = new JMenu("0, 0");
        menuBar.add(fileMenu);
        menuBar.add(helpMenu);
        menuBar.add(Box.createHorizontalGlue());
        coordViewer.setEnabled(false);
        menuBar.add(coordViewer);

        menuItems = new JMenuItem[9];
        String labels[] = {"New", "Open", "Save", "Save as...", "Change theme of the map", "Change position of spawnpoint", "Quit", "Instructions", "About"};
        for (int i = 0; i < 9; i++) {
            menuItems[i] = new JMenuItem(labels[i]);
            menuItems[i].addActionListener(this);
            if (i == 4 || i == 6) {
                fileMenu.addSeparator();
            }
            if (i < 7) {
                fileMenu.add(menuItems[i]);
            } else {
                helpMenu.add(menuItems[i]);
            }
        }

        return menuBar;
    }

    private void resetMap() {
        offX = 0;
        offY = 0;
        map = null;
        map = new Map();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == menuItems[0]) { // New
            resetMap();
            fc.setSelectedFile(null);
        } else if (e.getSource() == menuItems[1]) { // Open
            int returnVal = fc.showOpenDialog(this);

            if (returnVal == JFileChooser.APPROVE_OPTION) {
                try {
                    resetMap();
                    map.load(fc.getSelectedFile().getAbsolutePath());
                } catch (IOException ex) {
                    Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
                } catch (ParseException ex) {
                    Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        } else if (e.getSource() == menuItems[2]) { // Save
            if (fc.getSelectedFile() == null) {
                e.setSource(menuItems[3]);
                actionPerformed(e);
            } else {
                try {
                    map.save(fc.getSelectedFile().getAbsolutePath());
                } catch (IOException ex) {
                    Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        } else if (e.getSource() == menuItems[3]) { // Save as
            int returnVal = fc.showSaveDialog(this);

            if (returnVal == JFileChooser.APPROVE_OPTION) {
                try {
                    map.save(fc.getSelectedFile().getAbsolutePath());
                } catch (IOException ex) {
                    Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
                }

            }
        } else if (e.getSource() == menuItems[4]) { // Change theme
            Object[] possibilities = {"0", "1", "2", "3", "4", "5", "6", "7"};
            String s = (String) JOptionPane.showInputDialog(
                    frame,
                    "Choose the theme from following list:",
                    "Change theme",
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    possibilities,
                    Byte.toString(map.getTheme()));

            if ((s != null) && (s.length() > 0)) {
                map.setTheme(Byte.parseByte(s));
            }
        } else if (e.getSource() == menuItems[5]) { // Change position of spawnpoint
            changeSpawnpoint = !changeSpawnpoint;
        } else if (e.getSource() == menuItems[6]) { // Quit
            System.exit(0);
        } else if (e.getSource() == menuItems[7]) { // Help
            JOptionPane.showMessageDialog(frame, "Hit [spacebar] to view available tiles. Pick one using the left mouse button.\nRight mouse button selects the currently hovered tile.\nMove the cursor around while holding the middle mouse button to move the view.\nIt will move at double speed if you hold the [shift] key down.\nYou can use Esc to quit the editor.", "The Orb Map Editor - Help", JOptionPane.QUESTION_MESSAGE);
        } else if (e.getSource() == menuItems[8]) { // About
            JOptionPane.showMessageDialog(frame, "The Orb Map Editor 0.5\nCreated by rhino and m4tx", "The Orb Map Editor - About", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    class MouseHandler extends MouseAdapter {

        @Override
        public void mousePressed(MouseEvent e) {
            if (e.getButton() == MouseEvent.BUTTON1) {
                leftBtnHeld = true;
            } else if (e.getButton() == MouseEvent.BUTTON2) {
                middleBtnHeld = true;
            } else if (e.getButton() == MouseEvent.BUTTON3) {
                rightBtnHeld = true;
            }

            if (!paletteOpen && !changeSpawnpoint) {
                if (leftBtnHeld) {
                    map.setBlock((short) tileX, (short) tileY, (byte) choosenBlock);
                }
                if (rightBtnHeld) {
                    map.setBlock((short) tileX, (short) tileY, (byte) -1);
                }
                if (middleBtnHeld) {
                    dragX = e.getX() - offX;
                    dragY = e.getY() - offY;
                }
            }
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            if (e.getButton() == MouseEvent.BUTTON1) {
                leftBtnHeld = false;
                if (paletteOpen && tileY * 25 + tileX < tiles.length && tiles[tileY * 25 + tileX] != null) {
                    choosenBlock = (byte) (tileY * 25 + tileX);
                    paletteOpen = false;
                } else if (changeSpawnpoint) {
                    map.setSpawnpoint((short)tileX, (short)tileY);
                    changeSpawnpoint = false;
                }
            } else if (e.getButton() == MouseEvent.BUTTON2) {
                middleBtnHeld = false;
            } else if (e.getButton() == MouseEvent.BUTTON3) {
                rightBtnHeld = false;
            }
        }
    }

    class KeyHandler extends KeyAdapter {

        @Override
        public void keyPressed(KeyEvent e) {
            switch (e.getKeyCode()) {
                case KeyEvent.VK_SHIFT:
                    shiftKeyHeld = true;
                    break;
                case KeyEvent.VK_SPACE:
                    paletteOpen = !paletteOpen;
                    break;
                case KeyEvent.VK_ESCAPE:
                    System.exit(0);
                    break;
            }
        }

        @Override
        public void keyReleased(KeyEvent e) {
            switch (e.getKeyCode()) {
                case KeyEvent.VK_SHIFT:
                    shiftKeyHeld = false;
                    break;
            }
        }
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        if (e.getX() >= 0 && e.getX() < 800) {
            curX = e.getX();
        }
        if (e.getY() >= 0 && e.getY() < 576) {
            curY = e.getY();
        }

        if (paletteOpen) {
            tileX = (curX - (curX % 32)) / 32;
            tileY = (curY - (curY % 32)) / 32;
        } else {
            tileX = (curX - offX - (curX - offX) % 32) / 32;
            tileY = (curY - offY - (curY - offY) % 32) / 32;
        }
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        mouseMoved(e);
        if (middleBtnHeld) {
            int mul = shiftKeyHeld ? 2 : 1;
            offX = mul * (e.getX() - dragX);
            offY = mul * (e.getY() - dragY);
            if (offX > 0) {
                dragX += offX;
                offX = 0;
            }
            if (offY > 0) {
                dragY += offY;
                offY = 0;
            }
        } else {
            mouseHandler.mousePressed(e);
        }
    }

    private void drawStuff() {
        Graphics2D g = (Graphics2D) bufStrategy.getDrawGraphics();

        g.setColor(new Color(0x000044));
        g.fillRect(0, 0, 800, 600);

        if (paletteOpen) {
            for (int y = 0; y < 17; y++) {
                for (int x = 0; x < 25; x++) {
                    if (y * 25 + x < tiles.length && tiles[y * 24 + x] != null) {
                        g.drawImage(tiles[y * 25 + x], x * 32, y * 32, null);
                    }
                }
            }
        } else {
            g.translate(offX, offY);
            for (short x = 0; x < 256; x++) {
                for (short y = 0; y < 256; y++) {
                    if (map.getBlock(x, y) != -1) {
                        g.drawImage(tiles[map.getBlock(x, y)], x * 32, y * 32, null);
                    }
                }
            }
            if (!changeSpawnpoint)
            g.drawImage(imgOrb, map.getSpawnpoint().x * 32, map.getSpawnpoint().y * 32, null);
        }

        if (!middleBtnHeld) {
            g.setColor(Color.WHITE);
            if (paletteOpen) {
                g.drawRect(curX - curX % 32, curY - curY % 32, 32, 32);
            } else {
                g.translate(-offX, -offY);
                g.drawRect(curX - (curX - offX) % 32, curY - (curY - offY) % 32, 32, 32);
            }
        }

        g.dispose();
        bufStrategy.show();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                new Main();
            }
        });
    }
}
