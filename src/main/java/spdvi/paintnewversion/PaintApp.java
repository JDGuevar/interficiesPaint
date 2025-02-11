// src/main/java/spdvi/paintnewversion/PaintApp.java
package spdvi.paintnewversion;


import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

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

        //Botón para la función cuentagotas
        JButton cuentagotasButton = new JButton("Cuentagotas");
        cuentagotasButton.addActionListener(e -> activaCuentagotas());
        buttonPanel.add(cuentagotasButton);

        // Botón para cambiar de grosor
        JButton grosorButton = new JButton("Grosor");
        grosorButton.addActionListener(e -> cambiarGrosor());
        buttonPanel.add(grosorButton);

        // Botón para cargar imagen
        JButton loadImageButton = new JButton("Cargar Imagen");
        loadImageButton.addActionListener(e -> loadImage());
        buttonPanel.add(loadImageButton);

        //boton para detectar texto
        JButton detectTextButton = new JButton("Detectar Texto");
        detectTextButton.addActionListener(e -> detectTextFromImage());
        buttonPanel.add(detectTextButton);
        // Goma
        JButton gomaButton = new JButton("Goma");
        gomaButton.addActionListener(e -> activaGoma());
        buttonPanel.add(gomaButton);

        
        //Boton para borrar todo
        JButton clearButton = new JButton("Borrar");
        clearButton.addActionListener(e -> drawingPanel.clear());
        buttonPanel.add(clearButton);

        // HACER y DESHACER
        JButton undoButton = new JButton("Deshacer");
        undoButton.addActionListener(e -> drawingPanel.undo());
        buttonPanel.add(undoButton);

        JButton redoButton = new JButton("Rehacer");
        redoButton.addActionListener(e -> drawingPanel.redo());
        buttonPanel.add(redoButton);

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
            BufferedImage capturedImage = captureDialog.getCapturedImage();
            if (capturedImage != null) {
                drawingPanel.loadImage(capturedImage);
            }
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

    private void detectTextFromImage() {
        BufferedImage image = drawingPanel.getLoadedImage(); // Método para obtener la imagen cargada en el panel
        if (image != null) {
            String extractedText = extractTextFromImage(image);
            if (extractedText != null && !extractedText.isEmpty()) {
                int option = JOptionPane.showConfirmDialog(this, "Texto detectado:\n" + extractedText + "\n\n¿Deseas guardar este texto en un archivo .txt?", "Guardar Texto", JOptionPane.YES_NO_OPTION);
                if (option == JOptionPane.YES_OPTION) {
                    saveTextToFile(extractedText);
                }
            } else {
                JOptionPane.showMessageDialog(this, "No se detectó texto en la imagen.");
            }
        } else {
            JOptionPane.showMessageDialog(this, "No hay ninguna imagen cargada.");
        }
    }

    private String extractTextFromImage(BufferedImage image) {
        Tesseract tesseract = new Tesseract();
        tesseract.setDatapath("tessdata"); // Ruta a los datos de Tesseract
        tesseract.setLanguage("spa"); // Configurar el idioma a español = "spa", ingles ="eng"
        try {
            return tesseract.doOCR(image);
        } catch (TesseractException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void saveTextToFile(String text) {
        if (text != null && !text.isEmpty()) {
            JFileChooser fileChooser = new JFileChooser();
            if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                File file = fileChooser.getSelectedFile();
                try (FileWriter writer = new FileWriter(file.getAbsolutePath() + ".txt")) {
                    writer.write(text);
                    JOptionPane.showMessageDialog(this, "Texto guardado correctamente.");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } else {
            JOptionPane.showMessageDialog(this, "No se detectó texto.");
        }
    }

    // Método para cambiar el color del pincel
    private void changeColor() {
        Color newColor = JColorChooser.showDialog(this, "Selecciona un color", drawingPanel.getBrushColor());
        if (newColor != null) {
            drawingPanel.setBrushColor(newColor);
        }
    }

    private void activaCuentagotas() {
        drawingPanel.setCuentagotasMode(true);
    }
    private void loadImage() {
        JFileChooser fileChooser = new JFileChooser();
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            System.out.println("Archivo seleccionado: " + file.getAbsolutePath());
            if (file.exists() && file.canRead()) {
                try {
                    BufferedImage img = ImageIO.read(file);
                    drawingPanel.loadImage(img);
                    System.out.println("Imagen cargada correctamente.");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                System.out.println("No se puede leer el archivo: " + file.getAbsolutePath());
            }
        } else {
            System.out.println("No se seleccionó ningún archivo.");
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