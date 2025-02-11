package spdvi.paintnewversion;

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
import java.util.ArrayList;
import javax.imageio.ImageIO;
import javax.swing.JPanel;

class DrawingPanel extends JPanel {

    private BufferedImage image;
    private Color brushColor = Color.BLACK;
    private int brushWidth = 1;
    private Point lastPoint;
    private ArrayList<BufferedImage> undoStack = new ArrayList<>();
    private ArrayList<BufferedImage> redoStack = new ArrayList<>();
    private boolean cuentagotasMode = false;

    public DrawingPanel() {
        setPreferredSize(new Dimension(600, 400));
        createEmptyCanvas();

        addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                if (cuentagotasMode) {
                    Point point = e.getPoint();
                    Color color = getColorAtPoint(point);
                    setBrushColor(color);
                    cuentagotasMode = false; // Desactivar modo cuentagotas después de seleccionar el color
                } else {
                    saveToUndoStack();
                    lastPoint = e.getPoint();
                }
            }
        });

        addMouseMotionListener(new MouseMotionAdapter() {
            public void mouseDragged(MouseEvent e) {
                if (lastPoint != null && !cuentagotasMode) {
                    Graphics2D g2 = image.createGraphics();
                    g2.setColor(brushColor);
                    g2.setStroke(new BasicStroke(brushWidth));
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

    public int getBrushWidth() {
        return brushWidth;
    }

    public void setBrushWidth(int brushWidth) {
        this.brushWidth = brushWidth;
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

    private void saveToUndoStack() {
        if (undoStack.size() > 10) { // Limitamos el historial a 10 estados para no consumir mucha memoria
            undoStack.remove(0);
        }
        redoStack.clear(); // Limpiamos la pila de rehacer al guardar un nuevo estado
        undoStack.add(copyImage(image));
    }

    public void clear() {
        createEmptyCanvas();
        repaint();
    }

    private BufferedImage copyImage(BufferedImage img) {
        BufferedImage copy = new BufferedImage(img.getWidth(), img.getHeight(), img.getType());
        Graphics2D g = copy.createGraphics();
        g.drawImage(img, 0, 0, null);
        g.dispose();
        return copy;
    }

    public void undo() {
        if (!undoStack.isEmpty()) {
            redoStack.add(copyImage(image)); // Guardamos la imagen actual en redoStack
            image = undoStack.remove(undoStack.size() - 1); // Restauramos la última imagen guardada
            repaint();
        }
    }

    public void redo() {
        if (!redoStack.isEmpty()) {
            undoStack.add(copyImage(image)); // Guardamos la imagen actual en undoStack
            image = redoStack.remove(redoStack.size() - 1); // Restauramos la imagen desde redoStack
            repaint();
        }
    }

    public void setCuentagotasMode(boolean mode) {
        this.cuentagotasMode = mode;
    }

    private Color getColorAtPoint(Point point) {
        BufferedImage image = getBufferedImage();
        if (image != null) {
            int rgb = image.getRGB(point.x, point.y);
            return new Color(rgb);
        }
        return Color.BLACK; // Color por defecto si no hay imagen
    }

    private BufferedImage getBufferedImage() {
        // Método para obtener la imagen actual del panel
        BufferedImage image = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = image.createGraphics();
        paint(g2d);
        g2d.dispose();
        return image;
    }

    // Método para obtener la imagen cargada
    public BufferedImage getLoadedImage() {
        return image;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawImage(image, 0, 0, this);
    }
}