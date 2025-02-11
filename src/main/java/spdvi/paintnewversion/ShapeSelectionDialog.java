package spdvi.paintnewversion;

import java.awt.Component;
import java.awt.Dimension;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;

public class ShapeSelectionDialog extends JDialog {
    public ShapeSelectionDialog(JFrame parent, DrawingPanel drawingPanel) {
        super(parent, "Seleccionar Forma", true);
        setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));
        setSize(200, 200);
        setLocationRelativeTo(parent);

        JButton pincelButton = new JButton("Pincel");
        pincelButton.addActionListener(e -> {
            drawingPanel.setShapeToDraw("NONE");
            dispose();
        });
        add(pincelButton);

        JButton circleButton = new JButton("Dibujar Círculo");
        circleButton.addActionListener(e -> {
            drawingPanel.setShapeToDraw("CIRCLE");
            dispose();
        });
        add(circleButton);

        JButton rectangleButton = new JButton("Dibujar Rectángulo");
        rectangleButton.addActionListener(e -> {
            drawingPanel.setShapeToDraw("RECTANGLE");
            dispose();
        });
        add(rectangleButton);

        JButton arrowButton = new JButton("Dibujar Flecha");
        arrowButton.addActionListener(e -> {
            drawingPanel.setShapeToDraw("ARROW");
            dispose();
        });
        add(arrowButton);

        JButton starButton = new JButton("Dibujar Estrella");
        starButton.addActionListener(e -> {
            drawingPanel.setShapeToDraw("STAR");
            dispose();
        });
        add(starButton);

        // Espaciado entre botones
        for (Component c : getContentPane().getComponents()) {
            if (c instanceof JButton) {
                ((JButton) c).setAlignmentX(Component.CENTER_ALIGNMENT);
                ((JButton) c).setMaximumSize(new Dimension(150, 40)); // Ajustar tamaño máximo
            }
        }
    }
}