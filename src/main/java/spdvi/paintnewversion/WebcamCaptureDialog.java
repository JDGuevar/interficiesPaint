package spdvi.paintnewversion;

import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.objdetect.Objdetect;
import org.opencv.videoio.VideoCapture;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import javax.imageio.ImageIO;

/**
 * Clase WebcamCaptureDialog que permite capturar imágenes desde una webcam utilizando OpenCV.
 * Además, detecta caras en la imagen capturada y dibuja rectángulos alrededor de ellas.
 */
public class WebcamCaptureDialog extends JDialog {
    private DrawingPanel drawingPanel;
    private VideoCapture capture;
    private Mat frame;
    private boolean capturing = false;
    private BufferedImage capturedImage;
    private JLabel imageLabel;
    private CascadeClassifier faceCascade;

    /**
     * Constructor de la clase WebcamCaptureDialog.
     *
     * @param parent        Ventana padre del diálogo.
     * @param drawingPanel  Panel de dibujo donde se insertará la imagen capturada.
     */
    public WebcamCaptureDialog(JFrame parent, DrawingPanel drawingPanel) {
        super(parent, "Captura de Webcam", true);
        this.drawingPanel = drawingPanel;
        setSize(640, 480);
        setLayout(new BorderLayout());

        imageLabel = new JLabel();
        add(imageLabel, BorderLayout.CENTER);

        JButton captureButton = new JButton("Capturar");
        captureButton.addActionListener(e -> captureImage());
        add(captureButton, BorderLayout.SOUTH);

        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                stopCapturing();
            }
        });

        // Cargar el clasificador de caras
        faceCascade = new CascadeClassifier("data/haarcascade_frontalface_alt2.xml");

        startCapturing();
    }

    /**
     * Inicia la captura de video desde la webcam.
     */
    private void startCapturing() {
        capture = new VideoCapture(0);
        frame = new Mat();

        if (!capture.isOpened()) {
            JOptionPane.showMessageDialog(this, "No se ha podido abrir la webcam.");
            return;
        }

        capturing = true;
        new Thread(() -> {
            while (capturing) {
                capture.read(frame);
                if (!frame.empty()) {
                    detectAndDrawFaces(frame);
                    BufferedImage img = matToBufferedImage(frame);
                    imageLabel.setIcon(new ImageIcon(img));
                    imageLabel.repaint();
                }
            }
        }).start();
    }

    /**
     * Detiene la captura de video y libera los recursos.
     */
    private void stopCapturing() {
        capturing = false;
        if (capture != null) {
            capture.release();
        }
    }

    /**
     * Captura la imagen actual de la webcam y la almacena en una variable.
     */
    private void captureImage() {
        if (!frame.empty()) {
            capturedImage = matToBufferedImage(frame);
            stopCapturing();
            dispose();
        }
    }

    /**
     * Obtiene la imagen capturada.
     *
     * @return La imagen capturada como un objeto BufferedImage.
     */
    public BufferedImage getCapturedImage() {
        return capturedImage;
    }

    /**
     * Detecta caras en la imagen proporcionada y dibuja rectángulos alrededor de ellas.
     *
     * @param image Imagen en la que se buscarán caras.
     */
    private void detectAndDrawFaces(Mat image) {
        MatOfRect faces = new MatOfRect();
        Mat grayFrame = new Mat();
        Imgproc.cvtColor(image, grayFrame, Imgproc.COLOR_BGR2GRAY);
        Imgproc.equalizeHist(grayFrame, grayFrame);

        int height = grayFrame.height();
        int absoluteFaceSize = Math.round(height * 0.2f);

        faceCascade.detectMultiScale(grayFrame, faces, 1.1, 2, 0 | Objdetect.CASCADE_SCALE_IMAGE,
                new Size(absoluteFaceSize, absoluteFaceSize), new Size());

        Rect[] faceArray = faces.toArray();
        for (Rect face : faceArray) {
            Imgproc.rectangle(image, face, new Scalar(255, 123, 45), 3);
        }
    }

    /**
     * Convierte un objeto Mat de OpenCV a BufferedImage.
     *
     * @param mat Imagen en formato Mat.
     * @return Imagen en formato BufferedImage.
     */
    private BufferedImage matToBufferedImage(Mat mat) {
        MatOfByte matOfByte = new MatOfByte();
        Imgcodecs.imencode(".jpg", mat, matOfByte);
        byte[] byteArray = matOfByte.toArray();
        BufferedImage img = null;
        try {
            img = ImageIO.read(new ByteArrayInputStream(byteArray));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return img;
    }
}
