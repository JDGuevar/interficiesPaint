package spdvi.paintnewversion.ventanas;

import spdvi.paintnewversion.DrawingPanel;
import spdvi.paintnewversion.ShapeSelectionDialog;
import spdvi.paintnewversion.WebcamCaptureDialog;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class MainFrame extends javax.swing.JFrame {

    private JTabbedPane tabbedPane;

    public MainFrame() {
        initComponents();
        this.setExtendedState(MAXIMIZED_BOTH);
    }

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
        webcamButton = new JButton("Capturar Webcam");

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

        colorButton.setText("Color");
        colorButton.addActionListener(e -> changeColor());

        gomaButton.setText("Goma");
        gomaButton.addActionListener(e -> activaGoma());

        cuentaGotasButton.setText("Cuenta Gotas");
        cuentaGotasButton.addActionListener(e -> activaCuentaGotas());

        deshacerButton.setText("Deshacer");
        deshacerButton.addActionListener(e -> getCurrentDrawingPanel().undo());

        rehacerButton.setText("Rehacer");
        rehacerButton.addActionListener(e -> getCurrentDrawingPanel().redo());

        webcamButton.addActionListener(e -> captureImage());

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
        buttonPanel.add(colorButton);
        buttonPanel.add(gomaButton);
        buttonPanel.add(cuentaGotasButton);
        buttonPanel.add(deshacerButton);
        buttonPanel.add(rehacerButton);
        buttonPanel.add(webcamButton);  

        getContentPane().add(buttonPanel, BorderLayout.WEST);
        getContentPane().add(tabbedPane, BorderLayout.CENTER);

        pack();
    }

    private void nuevoDibujo() {
        DrawingPanel drawingPanel = new DrawingPanel();
        tabbedPane.addTab("Dibujo " + (tabbedPane.getTabCount() + 1), new JScrollPane(drawingPanel));
        drawingPanel.setBounds(0, 0, tabbedPane.getWidth(), tabbedPane.getHeight());
        tabbedPane.setSelectedComponent(drawingPanel);
    }

    private DrawingPanel getCurrentDrawingPanel() {
        JScrollPane scrollPane = (JScrollPane) tabbedPane.getSelectedComponent();
        return (DrawingPanel) scrollPane.getViewport().getView();
    }

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

    private void saveImage() {
        JFileChooser fileChooser = new JFileChooser();
        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            getCurrentDrawingPanel().saveImage(file.getAbsolutePath() + ".jpg");
            JOptionPane.showMessageDialog(this, "Imagen guardada correctamente.");
        }
    }

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

    private void activaGoma() {
        getCurrentDrawingPanel().setBrushColor(Color.white);
        getCurrentDrawingPanel().setBrushWidth(5);
    }

    private void changeColor() {
        Color newColor = JColorChooser.showDialog(this, "Selecciona un color", getCurrentDrawingPanel().getBrushColor());
        if (newColor != null) {
            getCurrentDrawingPanel().setBrushColor(newColor);
        }
    }

    private void activaCuentaGotas() {
        getCurrentDrawingPanel().setCuentagotasMode(true);
    }

    public static void main(String[] args) {
        File dll = new File("src\\main\\java\\spdvi\\paintnewversion\\funciones\\opencv_java490.dll");
        System.load(dll.getAbsolutePath());

        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new MainFrame().setVisible(true);
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
}