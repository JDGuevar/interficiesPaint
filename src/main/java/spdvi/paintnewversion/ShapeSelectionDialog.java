package spdvi.paintnewversion;

import java.awt.Component;
import java.awt.Dimension;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;

/**
 * Diálogo para seleccionar la forma a dibujar en el panel de dibujo.
 */
public class ShapeSelectionDialog extends JDialog {

    /**
     * Constructor del diálogo de selección de forma.
     *
     * @param parent El marco padre del diálogo.
     * @param drawingPanel El panel de dibujo donde se dibujarán las formas.
     */
    public ShapeSelectionDialog(JFrame parent, DrawingPanel drawingPanel) {
        super(parent, "Seleccionar Forma", true);
        setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));
        setSize(200, 200);
        setLocationRelativeTo(parent);

        // Botón para seleccionar el pincel
        JButton pincelButton = new JButton("Pincel");
        pincelButton.addActionListener(e -> {
            drawingPanel.setShapeToDraw("NONE");
            dispose();
        });
        add(pincelButton);

        // Botón para dibujar un círculo
        JButton circleButton = new JButton("Dibujar Círculo");
        circleButton.addActionListener(e -> {
            drawingPanel.setShapeToDraw("CIRCLE");
            dispose();
        });
        add(circleButton);

        // Botón para dibujar un rectángulo
        JButton rectangleButton = new JButton("Dibujar Rectángulo");
        rectangleButton.addActionListener(e -> {
            drawingPanel.setShapeToDraw("RECTANGLE");
            dispose();
        });
        add(rectangleButton);

        // Botón para dibujar una flecha
        JButton arrowButton = new JButton("Dibujar Flecha");
        arrowButton.addActionListener(e -> {
            drawingPanel.setShapeToDraw("ARROW");
            dispose();
        });
        add(arrowButton);

        // Botón para dibujar una estrella
        JButton starButton = new JButton("Dibujar Estrella");
        starButton.addActionListener(e -> {
            drawingPanel.setShapeToDraw("STAR");
            dispose();
        });
        add(starButton);

        // Botón para dibujar un perro
        JButton dogButton = new JButton("Dibujar Perro");
        dogButton.addActionListener(e -> {
            drawingPanel.setShapeToDraw("DOG");
            dispose();
        });
        add(dogButton);

        // Botón para dibujar un gato
        JButton catButton = new JButton("Dibujar Gato");
        catButton.addActionListener(e -> {
            drawingPanel.setShapeToDraw("CAT");
            dispose();
        });
        add(catButton);


        // Ajustar la alineación y el tamaño de los botones
        for (Component c : getContentPane().getComponents()) {
            if (c instanceof JButton) {
                ((JButton) c).setAlignmentX(Component.CENTER_ALIGNMENT);
                ((JButton) c).setMaximumSize(new Dimension(150, 40)); // Ajustar tamaño máximo
            }
        }
    }
}