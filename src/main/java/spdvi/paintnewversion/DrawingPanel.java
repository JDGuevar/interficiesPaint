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

import org.opencv.core.CvType;
import org.opencv.core.MatOfPoint;
import org.opencv.imgproc.Imgproc;

import org.opencv.core.CvType;
import org.opencv.core.MatOfPoint;
import org.opencv.imgproc.Imgproc;

import org.opencv.core.CvType;

import org.opencv.core.CvType;
import org.opencv.core.MatOfPoint;
import org.opencv.imgproc.Imgproc;
import org.opencv.core.Mat;
import org.opencv.core.CvType;
import org.opencv.core.Scalar;
import org.opencv.core.MatOfPoint;
import org.opencv.imgproc.Imgproc;

class DrawingPanel extends JPanel {

    private Mat image;
    private BufferedImage bufferedImage;
    private Color brushColor = Color.BLACK;
    private int brushWidth = 1;
    private Point lastPoint;
    private ArrayList<BufferedImage> undoStack = new ArrayList<>();
    private ArrayList<BufferedImage> redoStack = new ArrayList<>();
    private String shapeToDraw = "NONE";

    public DrawingPanel() {
        File dll = new File("src\\main\\java\\spdvi\\paintnewversion\\funciones\\opencv_java490.dll");
        System.load(dll.getAbsolutePath());
        setPreferredSize(new Dimension(600, 400));
        createEmptyCanvas();

        addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                lastPoint = e.getPoint();
                if(!shapeToDraw.equals("NONE")) {
                    drawShape(e.getX(), e.getY());
                }
            }
        });

        addMouseMotionListener(new MouseMotionAdapter() {
            public void mouseDragged(MouseEvent e) {
                if (lastPoint != null && shapeToDraw.equals("NONE")) {
                    Graphics2D g2 = bufferedImage.createGraphics();
                    g2.setColor(brushColor);
                    g2.setStroke(new BasicStroke(brushWidth));
                    g2.drawLine(lastPoint.x, lastPoint.y, e.getX(), e.getY());
                    g2.dispose();
                    lastPoint = e.getPoint();
                    repaint();
                }
            }
        });
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                saveToUndoStack();
                lastPoint = e.getPoint();
            }

        });

    }

    private void createEmptyCanvas() {
        bufferedImage = new BufferedImage(600, 400, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = bufferedImage.createGraphics();
        g2.setColor(Color.WHITE);
        g2.fillRect(0, 0, 600, 400);
        g2.dispose();
    }

    public void setBrushColor(Color color) {
        this.brushColor = color;
    }

    public void setShapeToDraw(String shape) {
        this.shapeToDraw = shape;
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
        bufferedImage = img;
        repaint();
    }

    public void saveImage(String path) {
        try {
            ImageIO.write(bufferedImage, "PNG", new File(path));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void saveToUndoStack() {
        if (undoStack.size() > 10) { // Limitamos el historial a 10 estados para no consumir mucha memoria
            undoStack.remove(0);
        }
        redoStack.clear(); // Limpiamos la pila de rehacer al guardar un nuevo estado
        undoStack.add(copyImage(bufferedImage));
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
            redoStack.add(copyImage(bufferedImage)); // Guardamos la imagen actual en redoStack
            bufferedImage = undoStack.remove(undoStack.size() - 1); // Restauramos la última imagen guardada
            repaint();
        }
    }

    public void redo() {
        if (!redoStack.isEmpty()) {
            undoStack.add(copyImage(bufferedImage)); // Guardamos la imagen actual en undoStack
            bufferedImage = redoStack.remove(redoStack.size() - 1); // Restauramos la imagen desde redoStack
            repaint();
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawImage(bufferedImage, 0, 0, this);
    }

    private BufferedImage matToBufferedImage(Mat mat) {
        int width = mat.width();
        int height = mat.height();
        
        Mat matrgb = new Mat();
        
        Imgproc.cvtColor(mat, matrgb, Imgproc.COLOR_BGR2RGB);
        
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
        byte[] data = new byte[width * height * (int) mat.elemSize()];
        matrgb.get(0, 0, data);
        image.getRaster().setDataElements(0, 0, width, height, data);
        return image;
    }

    private void drawShape(int x, int y) {
        if (image == null) {
            image = new Mat(getHeight(), getWidth(), CvType.CV_8UC3, new Scalar(255, 255, 255));
        }
        switch (shapeToDraw) {
            case "CIRCLE":
                Imgproc.circle(image, new org.opencv.core.Point(x, y), 25, new Scalar(brushColor.getRed(), brushColor.getGreen(), brushColor.getBlue()), -1);
                break;
            case "RECTANGLE":
                Imgproc.rectangle(image, new org.opencv.core.Point(x - 25, y - 25), new org.opencv.core.Point(x + 25, y + 25), new Scalar(brushColor.getRed(), brushColor.getGreen(), brushColor.getBlue()), -1);
                break;
            case "ARROW":
                drawArrow(x, y);
                break;
        }
        bufferedImage = matToBufferedImage(image);
        repaint();
    }

    private void drawArrow(int x, int y) {
        int arrowWidth = 30;
        int arrowHeight = 20;
        int arrowBackWidth = 60;
        int arrowBackHeight = 3;

        org.opencv.core.Point[] points = new org.opencv.core.Point[7];
        points[0] = new org.opencv.core.Point(x, y);
        points[1] = new org.opencv.core.Point(x - arrowWidth, y - arrowHeight);
        points[2] = new org.opencv.core.Point(x - arrowWidth , y - arrowHeight / arrowBackHeight);
        points[3] = new org.opencv.core.Point(x - (arrowWidth + arrowBackWidth) , y - arrowHeight / arrowBackHeight);
        points[4] = new org.opencv.core.Point(x - (arrowWidth + arrowBackWidth) , y + arrowHeight / arrowBackHeight);
        points[5] = new org.opencv.core.Point(x - arrowWidth , y + arrowHeight / arrowBackHeight);
        points[6] = new org.opencv.core.Point(x - arrowWidth , y + arrowHeight);

        MatOfPoint matOfPoint = new MatOfPoint(points);
        Imgproc.fillPoly(image, java.util.Collections.singletonList(matOfPoint), new Scalar(brushColor.getRed(), brushColor.getGreen(), brushColor.getBlue()));
    }
}
