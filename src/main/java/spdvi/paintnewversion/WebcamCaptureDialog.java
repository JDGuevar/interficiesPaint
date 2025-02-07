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

public class WebcamCaptureDialog extends JDialog {
    private DrawingPanel drawingPanel;
    private VideoCapture capture;
    private Mat frame;
    private boolean capturing = false;
    private BufferedImage capturedImage;
    private JLabel imageLabel;
    private CascadeClassifier faceCascade;

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

    private void stopCapturing() {
        capturing = false;
        if (capture != null) {
            capture.release();
        }
    }

    private void captureImage() {
        if (!frame.empty()) {
            capturedImage = matToBufferedImage(frame);
            stopCapturing();
            dispose();
        }
    }

    public BufferedImage getCapturedImage() {
        return capturedImage;
    }

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