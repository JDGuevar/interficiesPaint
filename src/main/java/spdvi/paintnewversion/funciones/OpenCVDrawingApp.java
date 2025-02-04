package spdvi.paintnewversion.funciones;

import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.Serializable;

public class OpenCVDrawingApp extends JPanel implements Serializable{
    private Mat image;
    private BufferedImage bufferedImage;
    private java.awt.Point lastPoint;
    private int ticknes=1;
    private String imagePath="images/moon.jpg";
    
    public void setTicknes(int t){
        ticknes=t;
    }

    public OpenCVDrawingApp() {
        setSize(400, 400);
        setVisible(true);
        File dll = new File("data/opencv_java490.dll");
        System.load(dll.getAbsolutePath());
        image = Imgcodecs.imread(imagePath);
        bufferedImage = matToBufferedImage(image);

        addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                lastPoint = e.getPoint();
            }
        });

        addMouseMotionListener(new MouseMotionAdapter() {
            public void mouseDragged(MouseEvent e) {
                if (lastPoint != null) {
                    Imgproc.line(image, new org.opencv.core.Point(lastPoint.x, lastPoint.y),
                            new org.opencv.core.Point(e.getX(), e.getY()), new Scalar(0, 0, 255), ticknes);
                    lastPoint = e.getPoint();
                    bufferedImage = matToBufferedImage(image);
                    repaint();
                }
            }
        });
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
}
