/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package spdvi.paintnewversion;

import javax.swing.*;
import java.awt.*;
import java.io.File;

public class PaintApp extends JFrame {

    private DrawingPanel drawingPanel;

    public PaintApp() {
        setTitle("Paint con OpenCV");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        drawingPanel = new DrawingPanel();
        add(drawingPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();

        // Botón para cambiar de color
        JButton colorButton = new JButton("Color");
        colorButton.addActionListener(e -> changeColor());
        buttonPanel.add(colorButton);

        // Botón para capturar imagen desde la webcam
        JButton webcamButton = new JButton("Capturar Webcam");
        webcamButton.addActionListener(e -> captureImage());
        buttonPanel.add(webcamButton);

        // Botón para guardar la imagen editada
        JButton saveButton = new JButton("Guardar");
        saveButton.addActionListener(e -> saveImage());
        buttonPanel.add(saveButton);

        add(buttonPanel, BorderLayout.SOUTH);
    }

    // Método para abrir una ventana emergente de la webcam y capturar imagen
    private void captureImage() {
        SwingUtilities.invokeLater(() -> {
            WebcamCaptureDialog captureDialog = new WebcamCaptureDialog(this, drawingPanel);
            captureDialog.setVisible(true);
        });
    }

    // Método para cambiar el color del pincel
    private void changeColor() {
        Color newColor = JColorChooser.showDialog(this, "Selecciona un color", drawingPanel.getBrushColor());
        if (newColor != null) {
            drawingPanel.setBrushColor(newColor);
        }
    }

    // Método para guardar la imagen
    private void saveImage() {
        JFileChooser fileChooser = new JFileChooser();
        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            drawingPanel.saveImage(file.getAbsolutePath());
            JOptionPane.showMessageDialog(this, "Imagen guardada correctamente.");
        }
    }

    public static void main(String[] args) {
        // Cargar OpenCV
        File dll = new File("src\\main\\java\\spdvi\\paintnewversion\\funciones\\opencv_java490.dll");
        System.load(dll.getAbsolutePath());

        SwingUtilities.invokeLater(() -> {
            PaintApp app = new PaintApp();
            app.setVisible(true);
        });
    }
}
