package spdvi.paintnewversion.ventanas;

import spdvi.paintnewversion.DrawingPanel;
import spdvi.paintnewversion.ShapeSelectionDialog;
import spdvi.paintnewversion.WebcamCaptureDialog;

import javax.imageio.ImageIO;
import javax.swing.*;

import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Clase MainFrame que muestra una interaz gráfica con las diversas herramientas
 * de una aplicación de dibujo.
 */
public class MainFrame extends javax.swing.JFrame {

    private JTabbedPane tabbedPane;

    /**
     * Constructor de la ventana principal de la aplicación.
     */
    public MainFrame() {
        initComponents();
        this.setExtendedState(MAXIMIZED_BOTH);
        this.setVisible(true);
        nuevoDibujo();
    }

    /**
     * Inicializa los componentes de la interfaz.
     */
    @SuppressWarnings("unchecked")
    private void initComponents() {

        tabbedPane = new JTabbedPane();
        jMenuBar1 = new JMenuBar();
        archivoMenu = new JMenu();
        nuevoDibujoButton = new JMenuItem();
        cargarImagenButton = new JMenuItem();
        guardarButton = new JMenuItem();
        pincelMenu = new JMenu();
        grosorButton = new JMenuItem();
        borrarButton = new JMenu();
        formas = new JMenu();
        colorButton = new JButton();
        gomaButton = new JButton();
        cuentaGotasButton = new JButton();
        deshacerButton = new JButton();
        rehacerButton = new JButton();
        webcamButton = new JButton();
        detectTextButton = new JButton();

        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        archivoMenu.setText("Archivo");

        nuevoDibujoButton.setText("Nuevo Dibujo");
        nuevoDibujoButton.addActionListener(e -> nuevoDibujo());
        archivoMenu.add(nuevoDibujoButton);

        cargarImagenButton.setText("Cargar Imagen");
        cargarImagenButton.addActionListener(e -> loadImage());
        archivoMenu.add(cargarImagenButton);

        guardarButton.setText("Guardar");
        guardarButton.addActionListener(e -> saveImage());
        archivoMenu.add(guardarButton);

        jMenuBar1.add(archivoMenu);

        pincelMenu.setText("Pincel");

        grosorButton.setText("Grosor");
        grosorButton.addActionListener(e -> cambiarGrosor());
        pincelMenu.add(grosorButton);

        jMenuBar1.add(pincelMenu);

        borrarButton.setText("Borrar");
        borrarButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                getCurrentDrawingPanel().clear();
            }
        });
        jMenuBar1.add(borrarButton);

        formas.setText("Formas");
        formas.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                ShapeSelectionDialog dialog = new ShapeSelectionDialog(MainFrame.this, getCurrentDrawingPanel());
                dialog.setVisible(true);
            }
        });
        jMenuBar1.add(formas);

        setJMenuBar(jMenuBar1);

        // Tamaño fijo para los botones
        Dimension buttonSize = new Dimension(50, 50);

        // Cargar y redimensionar iconos
        colorButton.setIcon(resizeIcon(new ImageIcon("images/circulo-de-color.png"), buttonSize.width, buttonSize.height));
        gomaButton.setIcon(resizeIcon(new ImageIcon("images/borrador-de-pizarra.png"), buttonSize.width, buttonSize.height));
        cuentaGotasButton.setIcon(resizeIcon(new ImageIcon("images/cuentagotas.png"), buttonSize.width, buttonSize.height));
        deshacerButton.setIcon(resizeIcon(new ImageIcon("images/volver.png"), buttonSize.width, buttonSize.height));
        rehacerButton.setIcon(resizeIcon(new ImageIcon("images/delantero.png"), buttonSize.width, buttonSize.height));
        webcamButton.setIcon(resizeIcon(new ImageIcon("images/webcam.png"), buttonSize.width, buttonSize.height));
        detectTextButton.setIcon(resizeIcon(new ImageIcon("images/ocr.png"), buttonSize.width, buttonSize.height));

        // Establecer tamaño fijo para los botones
        colorButton.setPreferredSize(buttonSize);
        gomaButton.setPreferredSize(buttonSize);
        cuentaGotasButton.setPreferredSize(buttonSize);
        deshacerButton.setPreferredSize(buttonSize);
        rehacerButton.setPreferredSize(buttonSize);
        webcamButton.setPreferredSize(buttonSize);
        detectTextButton.setPreferredSize(buttonSize);

        //colorButton.setText("Color");
        colorButton.addActionListener(e -> changeColor());

        //gomaButton.setText("Goma");
        gomaButton.addActionListener(e -> activaGoma());

        //cuentaGotasButton.setText("Cuenta Gotas");
        cuentaGotasButton.addActionListener(e -> activaCuentaGotas());

        //deshacerButton.setText("Deshacer");
        deshacerButton.addActionListener(e -> getCurrentDrawingPanel().undo());

        //rehacerButton.setText("Rehacer");
        rehacerButton.addActionListener(e -> getCurrentDrawingPanel().redo());

        webcamButton.addActionListener(e -> captureImage());

        detectTextButton.addActionListener(e -> detectTextFromImage());

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
        buttonPanel.add(colorButton);
        buttonPanel.add(gomaButton);
        buttonPanel.add(cuentaGotasButton);
        buttonPanel.add(deshacerButton);
        buttonPanel.add(rehacerButton);
        buttonPanel.add(webcamButton);
        buttonPanel.add(detectTextButton);

        getContentPane().add(buttonPanel, BorderLayout.WEST);
        getContentPane().add(tabbedPane, BorderLayout.CENTER);
        pack();
    }

    /**
     * Crea un nuevo panel de dibujo según unas dimensiones elegidas.
     */
    private void nuevoDibujo() {
        // Solicitar el tamaño del dibujo al usuario
        String widthStr = JOptionPane.showInputDialog(this, "Ingrese el ancho del dibujo:", "Nuevo Dibujo", JOptionPane.PLAIN_MESSAGE);
        String heightStr = JOptionPane.showInputDialog(this, "Ingrese el alto del dibujo:", "Nuevo Dibujo", JOptionPane.PLAIN_MESSAGE);

        // Convertir las entradas a enteros
        int width = Integer.parseInt(widthStr);
        int height = Integer.parseInt(heightStr);

        // Crear el panel de dibujo con el tamaño especificado
        DrawingPanel drawingPanel = new DrawingPanel(width, height);

        // Crear el JScrollPane y añadir el DrawingPanel
        JScrollPane scrollPane = new JScrollPane(drawingPanel);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

        // Añadir el JScrollPane al JTabbedPane
        tabbedPane.addTab("Dibujo " + (tabbedPane.getTabCount() + 1), scrollPane);
        tabbedPane.setSelectedComponent(scrollPane);

        // Asegurarse de que el JScrollPane respete el tamaño preferido del DrawingPanel
        scrollPane.revalidate();
        scrollPane.repaint();
        SwingUtilities.invokeLater(() -> drawingPanel.requestFocusInWindow());
    }

    /**
     * Obtiene el panel de la pestaña activa del JTabbedPane.
     *
     * @return el panel activo de dibujo.
     */
    private DrawingPanel getCurrentDrawingPanel() {
        JScrollPane scrollPane = (JScrollPane) tabbedPane.getSelectedComponent();
        return (DrawingPanel) scrollPane.getViewport().getView();
    }

    /**
     * Carga una imagen desde un archivo y lo muestra.
     */
    private void loadImage() {
        JFileChooser fileChooser = new JFileChooser();
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            if (file.exists() && file.canRead()) {
                try {
                    BufferedImage img = ImageIO.read(file);
                    DrawingPanel currentPanel = getCurrentDrawingPanel();
                    currentPanel.loadImage(img);
                    currentPanel.scrollRectToVisible(new Rectangle((currentPanel.getWidth() - img.getWidth()) / 2,
                            (currentPanel.getHeight() - img.getHeight()) / 2, img.getWidth(), img.getHeight()));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Guarda la imagen actual del panel en un archivo.
     */
    private void saveImage() {
        JFileChooser fileChooser = new JFileChooser();
        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            getCurrentDrawingPanel().saveImage(file.getAbsolutePath() + ".jpg");
            JOptionPane.showMessageDialog(this, "Imagen guardada correctamente.");
        }
    }

    /**
     * Cambia el grosor del pincel
     */
    private void cambiarGrosor() {
        JSlider slider = new JSlider(JSlider.HORIZONTAL, 1, 20, 3);
        slider.setMajorTickSpacing(5);
        slider.setMinorTickSpacing(1);
        slider.setPaintTicks(true);
        slider.setPaintLabels(true);

        int option = JOptionPane.showConfirmDialog(this, slider, "Seleccionar Grosor del Pincel",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (option == JOptionPane.OK_OPTION) {
            int selectedSize = slider.getValue();
            getCurrentDrawingPanel().setBrushWidth(selectedSize);
        }
    }

    /**
     * Abre el dialogo de la webcam y captura la imagen de la "foto".
     */
    private void captureImage() {
        SwingUtilities.invokeLater(() -> {
            WebcamCaptureDialog captureDialog = new WebcamCaptureDialog(this, getCurrentDrawingPanel());
            captureDialog.setVisible(true);
            BufferedImage capturedImage = captureDialog.getCapturedImage();
            if (capturedImage != null) {
                getCurrentDrawingPanel().loadImage(capturedImage);
            }
        });
    }

    /**
     * Establece el color blanco y un grosor de 5 para simular una goma de
     * borrar.
     */
    private void activaGoma() {
        getCurrentDrawingPanel().setBrushColor(Color.white);
        getCurrentDrawingPanel().setBrushWidth(5);
    }

    /**
     * Permite al usuario seleccionar un color para el pincel.
     */
    private void changeColor() {
        Color newColor = JColorChooser.showDialog(this, "Selecciona un color", getCurrentDrawingPanel().getBrushColor());
        if (newColor != null) {
            getCurrentDrawingPanel().setBrushColor(newColor);
        }
    }

    /**
     * Activa el modo cuenta gotas para seleccionar un color de la imagen del
     * panel.
     */
    private void activaCuentaGotas() {
        getCurrentDrawingPanel().setCuentagotasMode(true);
    }

    /**
     * Detecta el texto en la imagen actual del panel y permite guardarlo en un
     * .txt
     */
    private void detectTextFromImage() {
        BufferedImage image = getCurrentDrawingPanel().getLoadedImage(); // Método para obtener la imagen cargada en el panel
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

    /**
     * Extrae el texto usando Tesseract OCR
     *
     * @param image Imagen actual de la cual extraer el texto
     * @return Texto extraído de la imagen
     */
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

    /**
     * Guarda el texto en un archivo nuevo .txt
     *
     * @param text Texto a guardar
     */
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

    /**
     * Redimensiona un icono para ajustarlo al tamaño específico
     *
     * @param icon Icono original
     * @param width Ancho deseado
     * @param height Alto deseado
     * @return Icono redimensionado
     */
    private Icon resizeIcon(ImageIcon icon, int width, int height) {
        Image img = icon.getImage();
        Image resizedImage = img.getScaledInstance(width, height, java.awt.Image.SCALE_SMOOTH);
        return new ImageIcon(resizedImage);
    }

    /**
     * Método principal que inicia la aplicación
     *
     * @param args argumentos de la linea de comandos
     */
    public static void main(String[] args) {
        File dll = new File("src\\main\\java\\spdvi\\paintnewversion\\funciones\\opencv_java490.dll");
        System.load(dll.getAbsolutePath());

        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new MainFrame();
            }
        });
    }

    private JMenu archivoMenu;
    private JMenu borrarButton;
    private JMenuItem cargarImagenButton;
    private JButton colorButton;
    private JButton cuentaGotasButton;
    private JButton deshacerButton;
    private JMenu formas;
    private JButton gomaButton;
    private JMenuItem grosorButton;
    private JMenuItem guardarButton;
    private JMenuBar jMenuBar1;
    private JMenu pincelMenu;
    private JMenuItem nuevoDibujoButton;
    private JButton rehacerButton;
    private JButton webcamButton;
    private JButton detectTextButton;
}
