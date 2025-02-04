/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package spdvi.paintnewversion;

import static java.awt.AWTEventMulticaster.add;
import java.awt.BorderLayout;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.videoio.VideoCapture;

class WebcamCaptureDialog extends JDialog {

    private JLabel imageLabel;
    private VideoCapture capture;
    private Mat frame;
    private boolean capturing = true;
    private DrawingPanel drawingPanel;

    public WebcamCaptureDialog(JFrame parent, DrawingPanel drawingPanel) {
        super(parent, "Captura desde Webcam", true);
        this.drawingPanel = drawingPanel;
        setSize(640, 480);
        setLayout(new BorderLayout());
        setLocationRelativeTo(parent);

        imageLabel = new JLabel();
        add(imageLabel, BorderLayout.CENTER);

        JButton captureButton = new JButton("Capturar");
        captureButton.addActionListener(e -> captureImage());
        add(captureButton, BorderLayout.SOUTH);

        startCamera();
    }

    private void startCamera() {
        capture = new VideoCapture(0);
        frame = new Mat();

        if (!capture.isOpened()) {
            JOptionPane.showMessageDialog(this, "No se pudo abrir la webcam.");
            dispose();
            return;
        }

        new Thread(() -> {
            while (capturing) {
                capture.read(frame);
                if (!frame.empty()) {
                    BufferedImage img = matToBufferedImage(frame);
                    SwingUtilities.invokeLater(() -> imageLabel.setIcon(new ImageIcon(img)));
                }
            }
        }).start();
    }

    private void captureImage() {
        if (!frame.empty()) {
            BufferedImage img = matToBufferedImage(frame);
            drawingPanel.loadImage(img);
            capturing = false;
            capture.release();
            dispose();
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
