package spdvi.paintnewversion.funciones;

import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.File;

public class OpenCVDrawingApp extends JPanel {
    private Mat image;
    private BufferedImage bufferedImage;
    private java.awt.Point lastPoint;
    private Color currentColor = Color.BLACK;

    public OpenCVDrawingApp() {
        File dll = new File("src\\main\\java\\spdvi\\paintnewversion\\funciones\\opencv_java490.dll");
        System.load(dll.getAbsolutePath());
        loadImage("images/maquinote.jpg");
        addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                lastPoint = e.getPoint();
            }
        });

        addMouseMotionListener(new MouseMotionAdapter() {
            public void mouseDragged(MouseEvent e) {
                if (lastPoint != null) {
                    Imgproc.line(image, new org.opencv.core.Point(lastPoint.x, lastPoint.y),
                            new org.opencv.core.Point(e.getX(), e.getY()), new Scalar(currentColor.getRed(), currentColor.getGreen(), currentColor.getBlue()), 2);
                    lastPoint = e.getPoint();
                    bufferedImage = matToBufferedImage(image);
                    repaint();
                }
            }
        });
    }

    public void setCurrentColor(Color color) {
        this.currentColor = color;
    }

    public void clear() {
        image.setTo(new Scalar(255, 255, 255));
        bufferedImage = matToBufferedImage(image);
        repaint();
    }

    public void loadImage(String imagePath) {
        image = Imgcodecs.imread(imagePath);
        bufferedImage = matToBufferedImage(image);
        repaint();
    }

    public void setImage(BufferedImage img) {
        this.bufferedImage = img;
        this.image = bufferedImageToMat(img);
        repaint();
    }

    public void saveImage(String filePath) {
        Imgcodecs.imwrite(filePath, image);
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

    private Mat bufferedImageToMat(BufferedImage bi) {
        Mat mat = new Mat(bi.getHeight(), bi.getWidth(), CvType.CV_8UC3);
        byte[] data = ((DataBufferByte) bi.getRaster().getDataBuffer()).getData();
        mat.put(0, 0, data);
        return mat;
    }
}