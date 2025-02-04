package spdvi.paintnewversion;

import static com.sun.java.accessibility.util.AWTEventMonitor.addMouseListener;
import static com.sun.java.accessibility.util.AWTEventMonitor.addMouseMotionListener;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.JPanel;

class DrawingPanel extends JPanel {

    private BufferedImage image;
    private Color brushColor = Color.BLACK;
    private Point lastPoint;

    public DrawingPanel() {
        setPreferredSize(new Dimension(600, 400));
        createEmptyCanvas();

        addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                lastPoint = e.getPoint();
            }
        });

        addMouseMotionListener(new MouseMotionAdapter() {
            public void mouseDragged(MouseEvent e) {
                if (lastPoint != null) {
                    Graphics2D g2 = image.createGraphics();
                    g2.setColor(brushColor);
                    g2.setStroke(new BasicStroke(3));
                    g2.drawLine(lastPoint.x, lastPoint.y, e.getX(), e.getY());
                    g2.dispose();
                    lastPoint = e.getPoint();
                    repaint();
                }
            }
        });
    }

    private void createEmptyCanvas() {
        image = new BufferedImage(600, 400, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = image.createGraphics();
        g2.setColor(Color.WHITE);
        g2.fillRect(0, 0, 600, 400);
        g2.dispose();
    }

    public void setBrushColor(Color color) {
        this.brushColor = color;
    }

    public Color getBrushColor() {
        return brushColor;
    }

    public void loadImage(BufferedImage img) {
        image = img;
        repaint();
    }

    public void saveImage(String path) {
        try {
            ImageIO.write(image, "PNG", new File(path));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawImage(image, 0, 0, this);
    }
}
