package spdvi.paintnewversion;

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
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

/**
 * Panel de dibujo que permite dibujar diferentes formas y realizar operaciones de edición.
 */
public class DrawingPanel extends JPanel {

    private Mat image;
    private BufferedImage bufferedImage;

    private Color brushColor = Color.BLACK;
    private int brushWidth = 1;
    private double figureSize = (double)brushWidth/10;
    private Point lastPoint;
    private ArrayList<BufferedImage> undoStack = new ArrayList<>();
    private ArrayList<BufferedImage> redoStack = new ArrayList<>();
    private String shapeToDraw = "NONE";
    private double zoomFactor = 1.0;
    private double zoomIncrement = 0.1;
    private int zoomCenterX = 0;
    private int zoomCenterY = 0;
    private boolean cuentagotasMode = false;
    private int width;
    private int height;

    /**
     * Constructor del panel de dibujo.
     */
    public DrawingPanel(int width, int height) {
        this.width = width;
        this.height = height;
        setPreferredSize(new Dimension(width, height));
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
                } else {
                    if (!shapeToDraw.equals("NONE")) {
                        drawShape(adjustedPoint.x, adjustedPoint.y);
                    } else {
                        if (e.getButton() == MouseEvent.BUTTON1) { // Left click
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
                                new org.opencv.core.Point(adjustedPoint.x, adjustedPoint.y), color, brushWidth);
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
            setPreferredSize(new Dimension((int) (bufferedImage.getWidth() * zoomFactor), (int) (bufferedImage.getHeight() * zoomFactor)));
            revalidate();
            repaint();
        });
    }

    /**
     * Ajusta un punto según el factor de zoom actual.
     *
     * @param point El punto a ajustar.
     * @return El punto ajustado.
     */
    private Point adjustPointForZoom(Point point) {
        int adjustedX = (int) ((point.x - zoomCenterX) / zoomFactor);
        int adjustedY = (int) ((point.y - zoomCenterY) / zoomFactor);
        return new Point(adjustedX, adjustedY);
    }

    /**
     * Crea un lienzo vacío.
     */
    private void createEmptyCanvas() {
        image = new Mat(this.height, this.width, CvType.CV_8UC3, new Scalar(255, 255, 255));
        bufferedImage = matToBufferedImage(image);
        setPreferredSize(new Dimension(bufferedImage.getWidth(), bufferedImage.getHeight()));
        revalidate();
        repaint();
    }

    /**
     * Establece el color del pincel.
     *
     * @param color El nuevo color del pincel.
     */
    public void setBrushColor(Color color) {
        this.brushColor = color;
    }

    /**
     * Establece la forma a dibujar.
     *
     * @param shape La forma a dibujar.
     */
    public void setShapeToDraw(String shape) {
        this.shapeToDraw = shape;
    }

    /**
     * Obtiene el color del pincel.
     *
     * @return El color del pincel.
     */
    public Color getBrushColor() {
        return brushColor;
    }

    /**
     * Obtiene el ancho del pincel.
     *
     * @return El ancho del pincel.
     */
    public int getBrushWidth() {
        return brushWidth;
    }

    /**
     * Establece el ancho del pincel.
     *
     * @param brushWidth El nuevo ancho del pincel.
     */
    public void setBrushWidth(int brushWidth) {
        this.brushWidth = brushWidth;
        this.figureSize = (double)brushWidth/10;
    }

    /**
     * Carga una imagen desde una ruta de archivo.
     *
     * @param imagePath La ruta del archivo de imagen.
     */
    public void loadImage(String imagePath) {
        image = Imgcodecs.imread(imagePath);
        if (image.empty()) {
            System.err.println("Error: No se pudo cargar la imagen desde " + imagePath);
            return;
        }
        bufferedImage = matToBufferedImage(image);
        repaint();
    }

    /**
     * Carga una imagen desde un BufferedImage.
     *
     * @param img La imagen a cargar.
     */
    public void loadImage(BufferedImage img) {
        if (img == null) {
            System.err.println("Error: La imagen proporcionada es nula");
            return;
        }
        this.bufferedImage = img;
        this.image = bufferedImageToMat(img);
        repaint();
    }

    /**
     * Convierte un BufferedImage a un Mat.
     *
     * @param bi El BufferedImage a convertir.
     * @return El Mat resultante.
     */
    private Mat bufferedImageToMat(BufferedImage bi) {
        Mat mat = new Mat(bi.getHeight(), bi.getWidth(), CvType.CV_8UC3);
        byte[] data = ((DataBufferByte) bi.getRaster().getDataBuffer()).getData();
        mat.put(0, 0, data);
        return mat;
    }

    /**
     * Guarda la imagen actual en una ruta de archivo.
     *
     * @param path La ruta del archivo donde se guardará la imagen.
     */
    public void saveImage(String path) {
        Imgcodecs.imwrite(path, image);
    }

    /**
     * Guarda el estado actual en la pila de deshacer.
     */
    private void saveToUndoStack() {
        if (undoStack.size() > 10) { // Limitamos el historial a 10 estados para no consumir mucha memoria
            undoStack.remove(0);
        }
        redoStack.clear(); // Limpiamos la pila de rehacer al guardar un nuevo estado
        undoStack.add(copyImage(bufferedImage));
    }

    /**
     * Limpia el lienzo.
     */
    public void clear() {
        createEmptyCanvas();
        repaint();
    }

    /**
     * Crea una copia de una imagen.
     *
     * @param img La imagen a copiar.
     * @return La copia de la imagen.
     */
    private BufferedImage copyImage(BufferedImage img) {
        BufferedImage copy = new BufferedImage(img.getWidth(), img.getHeight(), img.getType());
        Graphics2D g = copy.createGraphics();
        g.drawImage(img, 0, 0, null);
        g.dispose();
        return copy;
    }

    /**
     * Deshace la última acción.
     */
    public void undo() {
        if (!undoStack.isEmpty()) {
            redoStack.add(copyImage(bufferedImage)); // Guardamos la imagen actual en redoStack
            bufferedImage = undoStack.remove(undoStack.size() - 1); // Restauramos la última imagen guardada
            image = bufferedImageToMat(bufferedImage); // Actualizamos el Mat
            repaint();
        }
    }

    /**
     * Rehace la última acción deshecha.
     */
    public void redo() {
        if (!redoStack.isEmpty()) {
            undoStack.add(copyImage(bufferedImage)); // Guardamos la imagen actual en undoStack
            bufferedImage = redoStack.remove(redoStack.size() - 1); // Restauramos la imagen desde redoStack
            image = bufferedImageToMat(bufferedImage); // Actualizamos el Mat
            repaint();
        }
    }

    /**
     * Establece el modo cuentagotas.
     *
     * @param mode El nuevo estado del modo cuentagotas.
     */
    public void setCuentagotasMode(boolean mode) {
        this.cuentagotasMode = mode;
    }

    /**
     * Obtiene el color en un punto específico.
     *
     * @param point El punto donde obtener el color.
     * @return El color en el punto especificado.
     */
    private Color getColorAtPoint(Point point) {
        BufferedImage image = getBufferedImage();
        if (image != null) {
            int rgb = image.getRGB(point.x, point.y);
            return new Color(rgb);
        }
        return Color.BLACK; // Color por defecto si no hay imagen
    }

    /**
     * Obtiene la imagen actual como BufferedImage.
     *
     * @return La imagen actual.
     */
    private BufferedImage getBufferedImage() {
        BufferedImage image = this.image != null ? matToBufferedImage(this.image) : bufferedImage;
        return image;
    }

    /**
     * Obtiene la imagen cargada.
     *
     * @return La imagen cargada.
     */
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

    /**
     * Convierte un Mat a un BufferedImage.
     *
     * @param mat El Mat a convertir.
     * @return El BufferedImage resultante.
     */
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

    /**
     * Dibuja una forma en la posición especificada.
     *
     * @param x La coordenada x donde se dibujará la forma.
     * @param y La coordenada y donde se dibujará la forma.
     */
    private void drawShape(int x, int y) {
        if (image == null) {
            image = new Mat(getHeight(), getWidth(), CvType.CV_8UC3, new Scalar(255, 255, 255));
        }
        Scalar color = new Scalar(brushColor.getBlue(), brushColor.getGreen(), brushColor.getRed()); // BGR order
        switch (shapeToDraw) {
            case "CIRCLE":
                Imgproc.circle(image, new org.opencv.core.Point(x, y), 5*brushWidth, color, -1);
                break;
            case "RECTANGLE":
                Imgproc.rectangle(image, new org.opencv.core.Point(x - 25*figureSize, y - 25*figureSize), new org.opencv.core.Point(x + 25, y + 25), color, -1);
                break;
            case "ARROW":
                drawArrow(x, y);
                break;
            case "STAR":
                drawStar(x, y);
                break;
            case "DOG":
                drawDog(x, y);
                break;

            case "CAT":
                drawCat(x, y);
                break;
        }
        bufferedImage = matToBufferedImage(image);
        repaint();
    }

    /**
     * Dibuja una flecha en la posición especificada.
     *
     * @param x La coordenada x donde se dibujará la flecha.
     * @param y La coordenada y donde se dibujará la flecha.
     */
    private void drawArrow(int x, int y) {
        int arrowWidth = 10*brushWidth/2;
        int arrowHeight = 5*brushWidth/2;
        int arrowBackWidth = 20*brushWidth/2;
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

    /**
     * Dibuja una estrella en la posición especificada.
     *
     * @param x La coordenada x donde se dibujará la estrella.
     * @param y La coordenada y donde se dibujará la estrella.
     */
    private void drawStar(int x, int y) {
        double outerRadius = 30 * figureSize; // Radio exterior de la estrella
        double innerRadius = 15 * figureSize; // Radio interior de la estrella
        int numPoints = 5; // Número de puntas de la estrella

        org.opencv.core.Point[] points = new org.opencv.core.Point[numPoints * 2];
        double angle = Math.PI / numPoints;

        for (int i = 0; i < numPoints * 2; i++) {
            double radius = (i % 2 == 0) ? outerRadius : innerRadius;
            double theta = i * angle - Math.PI / 2;
            points[i] = new org.opencv.core.Point(
                    x + Math.cos(theta) * radius,
                    y + Math.sin(theta) * radius
            );
        }

        MatOfPoint matOfPoint = new MatOfPoint(points);
        Scalar color = new Scalar(brushColor.getBlue(), brushColor.getGreen(), brushColor.getRed()); // BGR order
        Imgproc.fillPoly(image, java.util.Collections.singletonList(matOfPoint), color);
        bufferedImage = matToBufferedImage(image);
        repaint();
    }

    /**
     * Dibuja un perro en la posición especificada.
     *
     * @param x La coordenada x donde se dibujará el perro.
     * @param y La coordenada y donde se dibujará el perro.
     */
    private void drawDog(int x, int y) {
        // Dibujar la cabeza
        Scalar dogColor = new Scalar(19, 69, 139); // Marrón
        Imgproc.ellipse(image, new org.opencv.core.Point(x, y), new Size(50, 40), 0, 0, 360, dogColor, -1);
        
        // Dibujar las orejas
        Imgproc.ellipse(image, new org.opencv.core.Point(x - 35, y - 20), new Size(20, 50), -120, 0, 360, dogColor, -1);
        Imgproc.ellipse(image, new org.opencv.core.Point(x + 35, y - 20), new Size(20, 50), 120, 0, 360, dogColor, -1);
        
        // Dibujar el hocico
        Imgproc.ellipse(image, new org.opencv.core.Point(x, y + 30), new Size(20, 15), 0, 0, 360, dogColor, -1);
        
        // Dibujar la lengua
        Scalar tongueColor = new Scalar(0, 0, 255); // Rojo
        Imgproc.ellipse(image, new org.opencv.core.Point(x, y + 40), new Size(15, 10), 0, 0, 180, tongueColor, -1);
        
        // Dibujar los ojos
        Scalar eyeColor = new Scalar(0, 0, 0); // Negro
        Imgproc.circle(image, new org.opencv.core.Point(x - 15, y - 10), 5, eyeColor, -1);
        Imgproc.circle(image, new org.opencv.core.Point(x + 15, y - 10), 5, eyeColor, -1);

        // Dibujar la nariz
        Imgproc.ellipse(image, new org.opencv.core.Point(x, y + 20), new Size(10, 7), 0, 0, 360, eyeColor, -1);
                
        bufferedImage = matToBufferedImage(image);
        repaint();
    }

    /**
     * Dibuja un gato en la posición especificada.
     *
     * @param x La coordenada x donde se dibujará el gato.
     * @param y La coordenada y donde se dibujará el gato.
     */
    private void drawCat(int x, int y) {
        // Dibujar la cabeza
        Scalar catColor = new Scalar(0, 0, 0); // Negro
        Imgproc.ellipse(image, new org.opencv.core.Point(x, y), new Size(50, 40), 0, 0, 360, catColor, -1);
        
        // Dibujar las orejas puntiagudas
        org.opencv.core.Point[] leftEar = { new org.opencv.core.Point(x - 40, y - 50), new org.opencv.core.Point(x - 60, y - 20), new org.opencv.core.Point(x - 20, y - 20) };
        org.opencv.core.Point[] rightEar = { new org.opencv.core.Point(x + 40, y - 50), new org.opencv.core.Point(x + 60, y - 20), new org.opencv.core.Point(x + 20, y - 20) };
        
        // Ángulo de rotación (en grados)
        double angle = 35;

        // Definir los puntos centrales de las orejas
        org.opencv.core.Point leftEarCenter = new org.opencv.core.Point(x - 40, y - 35);
        org.opencv.core.Point rightEarCenter = new org.opencv.core.Point(x + 40, y - 35);

        // Rotar las orejas sobre sí mismas
        org.opencv.core.Point[] leftEarRotated = new org.opencv.core.Point[3];
        org.opencv.core.Point[] rightEarRotated = new org.opencv.core.Point[3];

        for (int i = 0; i < 3; i++) {
            leftEarRotated[i] = rotatePoint(leftEar[i], leftEarCenter, -angle);
            rightEarRotated[i] = rotatePoint(rightEar[i], rightEarCenter, angle);
        }

        // Dibujar las orejas rotadas
        Imgproc.fillConvexPoly(image, new MatOfPoint(leftEarRotated), catColor);
        Imgproc.fillConvexPoly(image, new MatOfPoint(rightEarRotated), catColor);
        
        // Dibujar los ojos
        Scalar eyeColor = new Scalar(255, 255, 255); // Blanco
        Imgproc.circle(image, new org.opencv.core.Point(x - 15, y - 10), 5, eyeColor, -1);
        Imgproc.circle(image, new org.opencv.core.Point(x + 15, y - 10), 5, eyeColor, -1);
        
        // Dibujar la nariz
        Scalar bigoteColor = new Scalar(100, 100, 100); // Gris
        Imgproc.ellipse(image, new org.opencv.core.Point(x, y + 20), new Size(6, 4), 0, 0, 360, bigoteColor, -1);
        
        // Dibujar los bigotes
        for (int i = -1; i <= 1; i++) {
            Imgproc.line(image, new org.opencv.core.Point(x - 10, y + 20 + (i * 5)), new org.opencv.core.Point(x - 60, y + 20 + (i * 10)), bigoteColor, 2);
            Imgproc.line(image, new org.opencv.core.Point(x + 10, y + 20 + (i * 5)), new org.opencv.core.Point(x + 60, y + 20 + (i * 10)), bigoteColor, 2);
        } 
        bufferedImage = matToBufferedImage(image);
        repaint();
    }

    // Función para rotar un punto alrededor de otro punto de referencia
    private org.opencv.core.Point rotatePoint(org.opencv.core.Point point, org.opencv.core.Point center, double angle) {
        double radians = Math.toRadians(angle);
        double cos = Math.cos(radians);
        double sin = Math.sin(radians);

        double dx = point.x - center.x;
        double dy = point.y - center.y;

        double newX = center.x + (dx * cos - dy * sin);
        double newY = center.y + (dx * sin + dy * cos);

        return new org.opencv.core.Point(newX, newY);
    }
    
    /**
     * Borra una línea en la posición especificada. 
     *
     * @param x La coordenada x donde se comenzará a borrar.
     * @param y La coordenada y donde se comenzará a borrar.
     */
    private void erase(int x, int y) {
        Scalar white = new Scalar(255, 255, 255); // White color
        Imgproc.line(image, new org.opencv.core.Point(lastPoint.x, lastPoint.y),
            new org.opencv.core.Point(x, y), white, brushWidth);
        bufferedImage = matToBufferedImage(image);
        repaint();
    }
}
