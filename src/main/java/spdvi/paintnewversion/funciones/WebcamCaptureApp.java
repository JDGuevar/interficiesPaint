/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package spdvi.paintnewversion.funciones;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.videoio.VideoCapture;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

public class WebcamCaptureApp extends JFrame {
    private JLabel imageLabel;
    private VideoCapture capture;
    private Mat frame;
    private boolean capturing = false;

    public WebcamCaptureApp() {
        setTitle("Captura d'Imatges de la Webcam");
        setSize(640, 480);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        imageLabel = new JLabel();
        add(imageLabel, BorderLayout.CENTER);

        JButton captureButton = new JButton("Capturar");
        captureButton.addActionListener(e -> captureImage());
        add(captureButton, BorderLayout.SOUTH);
    }

    private void captureImage() {
        String fileName = JOptionPane.showInputDialog(this, "Introdueix el nom del fitxer:");
        if (fileName != null && !fileName.trim().isEmpty()) {
            File outputFile = new File("images/"+ fileName + ".jpg");
            Imgcodecs.imwrite(outputFile.getAbsolutePath(), frame);
            JOptionPane.showMessageDialog(this, "Imatge desada com: " + outputFile.getAbsolutePath());
        }
    }

    public void start() {
        File dll = new File("src\\main\\java\\spdvi\\paintnewversion\\funciones\\opencv_java490.dll");
        System.load(dll.getAbsolutePath());
        capture = new VideoCapture(0);
        frame = new Mat();

        if (!capture.isOpened()) {
            JOptionPane.showMessageDialog(this, "No s'ha pogut obrir la webcam.");
            return;
        }

        capturing = true;
        new Thread(() -> {
            while (capturing) {
                capture.read(frame);
                if (!frame.empty()) {
                    BufferedImage img = matToBufferedImage(frame);
                    ImageIcon icon = new ImageIcon(img);
                    imageLabel.setIcon(icon);
                    imageLabel.repaint();
                }
            }
        }).start();
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

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> { // què és això?
            WebcamCaptureApp app = new WebcamCaptureApp();
            app.setVisible(true);
            app.start();
        });
    }
}

