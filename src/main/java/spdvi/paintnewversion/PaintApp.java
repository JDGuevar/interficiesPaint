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
        setLayout(new BorderLayout());
        
        drawingPanel = new DrawingPanel();
        add(drawingPanel, BorderLayout.CENTER);
        
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
        add(buttonPanel, BorderLayout.WEST);

        // Botón para cambiar de color
        JButton colorButton = new JButton("Color");
        colorButton.addActionListener(e -> changeColor());
        buttonPanel.add(colorButton);

        // Botón para cambiar de grosor
        JButton grosorButton = new JButton("Grosor");
        grosorButton.addActionListener(e -> cambiarGrosor());
        buttonPanel.add(grosorButton);

        // Goma
        JButton gomaButton = new JButton("Goma");
        gomaButton.addActionListener(e -> activaGoma());
        buttonPanel.add(gomaButton);

        // Botón para capturar imagen desde la webcam
        JButton webcamButton = new JButton("Capturar Webcam");
        webcamButton.addActionListener(e -> captureImage());
        buttonPanel.add(webcamButton);

        // Botón para guardar la imagen editada
        JButton saveButton = new JButton("Guardar");
        saveButton.addActionListener(e -> saveImage());
        buttonPanel.add(saveButton);
        
        for (Component c : buttonPanel.getComponents()) {
            if (c instanceof JButton) {
                ((JButton) c).setAlignmentX(Component.CENTER_ALIGNMENT);
                ((JButton) c).setMaximumSize(new Dimension(150, 40)); // Ajustar tamaño máximo
            }
        }
    }

    // Método para abrir una ventana emergente de la webcam y capturar imagen
    private void captureImage() {
        SwingUtilities.invokeLater(() -> {
            WebcamCaptureDialog captureDialog = new WebcamCaptureDialog(this, drawingPanel);
            captureDialog.setVisible(true);
        });
    }

    //Método para cambiar el grosor
    private void cambiarGrosor() {
        JSlider slider = new JSlider(JSlider.HORIZONTAL, 1, 20, 3); // Rango de 1 a 20, valor inicial 3
        slider.setMajorTickSpacing(5);
        slider.setMinorTickSpacing(1);
        slider.setPaintTicks(true);
        slider.setPaintLabels(true);
        
        int option = JOptionPane.showConfirmDialog(this, slider, "Seleccionar Grosor del Pincel",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        
        if (option == JOptionPane.OK_OPTION) {
            int selectedSize = slider.getValue();
            drawingPanel.setBrushWidth(selectedSize); // Método en DrawingPanel para cambiar el grosor
        }
    }

    // GOMA
    private void activaGoma() {
        drawingPanel.setBrushColor(Color.white);
        drawingPanel.setBrushWidth(5);
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
            drawingPanel.saveImage(file.getAbsolutePath() + ".jpg");
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
