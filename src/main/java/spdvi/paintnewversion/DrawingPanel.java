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
import java.awt.image.DataBufferByte;
import java.io.File;
import java.util.ArrayList;

import javax.swing.JPanel;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Scalar;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

public class DrawingPanel extends JPanel {

    private Mat image;
    private BufferedImage bufferedImage;

    private Color brushColor = Color.BLACK;
    private int brushWidth = 1;
    private Point lastPoint;
    private ArrayList<BufferedImage> undoStack = new ArrayList<>();
    private ArrayList<BufferedImage> redoStack = new ArrayList<>();
    private String shapeToDraw = "NONE";
    private double zoomFactor = 1.0;
    private double zoomIncrement = 0.1;
    private int zoomCenterX = 0;
    private int zoomCenterY = 0;
    private boolean cuentagotasMode = false;

    public DrawingPanel() {
        File dll = new File("src\\main\\java\\spdvi\\paintnewversion\\funciones\\opencv_java490.dll");
        System.load(dll.getAbsolutePath());
        createEmptyCanvas();
        
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                saveToUndoStack();
                lastPoint = adjustPointForZoom(e.getPoint());
                Point adjustedPoint = adjustPointForZoom(e.getPoint());
                if (cuentagotasMode) {
                    Color color = getColorAtPoint(adjustedPoint);
                    setBrushColor(color);
                    cuentagotasMode = false; // Desactivar modo cuentagotas después de seleccionar el color
                }else{
                    if (!shapeToDraw.equals("NONE")) {
                    
                    drawShape(adjustedPoint.x, adjustedPoint.y);
                    }else{
                        if (e.getButton() == MouseEvent.BUTTON1) {// Left click
                            Scalar color = new Scalar(brushColor.getBlue(), brushColor.getGreen(), brushColor.getRed()); // BGR order
                            Imgproc.line(image, new org.opencv.core.Point(lastPoint.x, lastPoint.y),
                            new org.opencv.core.Point(adjustedPoint.x, adjustedPoint.y), color, brushWidth);
                        } else if (e.getButton() == MouseEvent.BUTTON3) { // Right click
                            erase(adjustedPoint.x, adjustedPoint.y);
                        }
                    }
                
                }
                bufferedImage = matToBufferedImage(image);
                repaint();
                
            }
        });

        addMouseMotionListener(new MouseMotionAdapter() {
            public void mouseDragged(MouseEvent e) {

                if (lastPoint != null && shapeToDraw.equals("NONE")) {

                    Point adjustedPoint = adjustPointForZoom(e.getPoint());
                    
                    if (e.getModifiersEx() == MouseEvent.BUTTON1_DOWN_MASK) { // Left button drag
                        Scalar color = new Scalar(brushColor.getBlue(), brushColor.getGreen(), brushColor.getRed()); // BGR order
                        Imgproc.line(image, new org.opencv.core.Point(lastPoint.x, lastPoint.y),
                                new org.opencv.core.Point(adjustedPoint.x,adjustedPoint.y), color, brushWidth);
                    } else if (e.getModifiersEx() == MouseEvent.BUTTON3_DOWN_MASK) { // Right button drag
                        erase(adjustedPoint.x, adjustedPoint.y);
                    }
                    lastPoint = adjustedPoint;
                    bufferedImage = matToBufferedImage(image);

                    repaint();
                }
            }
        });

        addMouseWheelListener(listener -> {
            int notches = listener.getWheelRotation();
            if (notches < 0) {
                zoomFactor += zoomIncrement;
            } else {
                zoomFactor = Math.max(zoomFactor - zoomIncrement, 0.1);
            }
            this.setSize((int) (bufferedImage.getWidth() * zoomFactor), (int) (bufferedImage.getHeight() * zoomFactor));
            repaint();
            revalidate();
        });
    }

    private Point adjustPointForZoom(Point point) {
        int adjustedX = (int) ((point.x - zoomCenterX) / zoomFactor);
        int adjustedY = (int) ((point.y - zoomCenterY) / zoomFactor);
        return new Point(adjustedX, adjustedY);

    }

    private void createEmptyCanvas() {
        image = new Mat(400, 600, CvType.CV_8UC3, new Scalar(255, 255, 255));
        bufferedImage = matToBufferedImage(image);
        repaint();

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

    public void loadImage(String imagePath) {
        image = Imgcodecs.imread(imagePath);
        if (image.empty()) {
            System.err.println("Error: No se pudo cargar la imagen desde " + imagePath);
            return;
        }
        bufferedImage = matToBufferedImage(image);
        repaint();
    }

    public void loadImage(BufferedImage img) {
        if (img == null) {
            System.err.println("Error: La imagen proporcionada es nula");
            return;
        }
        this.bufferedImage = img;
        this.image = bufferedImageToMat(img);
        repaint();
    }

    private Mat bufferedImageToMat(BufferedImage bi) {
        Mat mat = new Mat(bi.getHeight(), bi.getWidth(), CvType.CV_8UC3);
        byte[] data = ((DataBufferByte) bi.getRaster().getDataBuffer()).getData();
        mat.put(0, 0, data);
        return mat;
    }

    public void saveImage(String path) {
        Imgcodecs.imwrite(path, image);

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
            image = bufferedImageToMat(bufferedImage); // Actualizamos el Mat

            repaint();
        }
    }

    public void redo() {
        if (!redoStack.isEmpty()) {

            undoStack.add(copyImage(bufferedImage)); // Guardamos la imagen actual en undoStack
            bufferedImage = redoStack.remove(redoStack.size() - 1); // Restauramos la imagen desde redoStack
            image = bufferedImageToMat(bufferedImage); // Actualizamos el Mat

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
        BufferedImage image = this.image != null ? matToBufferedImage(this.image) : bufferedImage;
        return image;
    }
    
    public BufferedImage getLoadedImage() {
        return bufferedImage;
    }


    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create(); // Creamos una copia para evitar modificar el original

        // Aplicamos la transformación para el zoom
        g2.translate(zoomCenterX, zoomCenterY);
        g2.scale(zoomFactor, zoomFactor);

        // Dibujamos la imagen
        g2.drawImage(bufferedImage, 0, 0, this);

        g2.dispose(); // Liberamos los recursos de g2
    }

    private BufferedImage matToBufferedImage(Mat mat) {
        int width = mat.width();
        int height = mat.height();
        int channels = mat.channels();

        Mat matrgb = new Mat();
        Imgproc.cvtColor(mat, matrgb, Imgproc.COLOR_BGR2RGB);

        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
        byte[] data = new byte[width * height * channels];
        matrgb.get(0, 0, data);
        image.getRaster().setDataElements(0, 0, width, height, data);
        return image;
    }

    private void drawShape(int x, int y) {
        if (image == null) {
            image = new Mat(getHeight(), getWidth(), CvType.CV_8UC3, new Scalar(255, 255, 255));
        }
        Scalar color = new Scalar(brushColor.getBlue(), brushColor.getGreen(), brushColor.getRed()); // BGR order
        switch (shapeToDraw) {
            case "CIRCLE":
                Imgproc.circle(image, new org.opencv.core.Point(x, y), 25, color, -1);
                break;
            case "RECTANGLE":
                Imgproc.rectangle(image, new org.opencv.core.Point(x - 25, y - 25), new org.opencv.core.Point(x + 25, y + 25), color, -1);
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
        points[2] = new org.opencv.core.Point(x - arrowWidth, y - arrowHeight / arrowBackHeight);
        points[3] = new org.opencv.core.Point(x - (arrowWidth + arrowBackWidth), y - arrowHeight / arrowBackHeight);
        points[4] = new org.opencv.core.Point(x - (arrowWidth + arrowBackWidth), y + arrowHeight / arrowBackHeight);
        points[5] = new org.opencv.core.Point(x - arrowWidth, y + arrowHeight / arrowBackHeight);
        points[6] = new org.opencv.core.Point(x - arrowWidth, y + arrowHeight);

        MatOfPoint matOfPoint = new MatOfPoint(points);
        Scalar color = new Scalar(brushColor.getBlue(), brushColor.getGreen(), brushColor.getRed()); // BGR order
        Imgproc.fillPoly(image, java.util.Collections.singletonList(matOfPoint), color);
    }
    private void erase(int x, int y) {
        Scalar white = new Scalar(255, 255, 255); // White color
        Imgproc.line(image, new org.opencv.core.Point(lastPoint.x, lastPoint.y),
            new org.opencv.core.Point(x, y), white, brushWidth);
        bufferedImage = matToBufferedImage(image);
        repaint();
    }
}
