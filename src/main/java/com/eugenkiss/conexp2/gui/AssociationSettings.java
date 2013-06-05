package com.eugenkiss.conexp2.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class AssociationSettings extends JPanel {

    private PropertyChangeSupport propertyChangeSupport;

    private static final long serialVersionUID = -3692280021161777005L;

    JLabel minSupLabel = new JLabel("Minimal Support 0.1");
    JSlider minSupSlider = new JSlider(0, 100, 10);

    JLabel confLabel = new JLabel("Confidence 0.5");
    JSlider confSlider = new JSlider(0, 100, 50);

    // Only for testing
    private int current = 0, all = 0;

    @SuppressWarnings("serial")
    JPanel piechart = new JPanel() {

        @Override
        public void paint(Graphics g2) {

            super.paint(g2);
            Graphics2D g = (Graphics2D) g2;
            g.addRenderingHints(new RenderingHints(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON));
            g.setRenderingHint(RenderingHints.KEY_RENDERING,
                    RenderingHints.VALUE_RENDER_QUALITY);

            int degree = (int) ((current * 360.0) / all);
            g.setColor(getBackground());
            g.fillRect(0, 0, getWidth(), getHeight());
            g.setPaint(Color.BLUE);
            g.fillArc(25, 5, 140, 140, 0, 360);
            g.drawString("#Association Rules = " + all, 10, 165);

            g.setColor(Color.RED);
            g.fillArc(25, 5, 140, 140, 90, degree);
            g.drawString("#Assoc. Rules with minSup = " + current, 10, 180);
        }

    };

    public AssociationSettings() {
        propertyChangeSupport = new PropertyChangeSupport(this);
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(0, 0, 10, 0);
        gbc.gridx = 0;
        gbc.gridy = 0;
        add(minSupLabel, gbc);
        gbc.gridy = 1;
        add(minSupSlider, gbc);
        gbc.gridy = 2;
        add(confLabel, gbc);
        gbc.gridy = 3;
        add(confSlider, gbc);
        gbc.gridy = 4;
        gbc.gridheight = GridBagConstraints.REMAINDER;
        piechart.setPreferredSize(new Dimension(200, 200));

        // Element redCircle = piechart.getSVGDocument().createElementNS(
        // SVGDOMImplementation.SVG_NAMESPACE_URI,
        // SVGConstants.SVG_CIRCLE_TAG);
        // redCircle.setAttributeNS(null, SVGConstants.SVG_CX_ATTRIBUTE, "100");
        // redCircle.setAttributeNS(null, SVGConstants.SVG_CY_ATTRIBUTE, "80");
        // redCircle.setAttributeNS(null, "r", "70");
        // redCircle.setAttributeNS(null, "fill", "red");
        // piechart.getSVGDocument().getDocumentElement().appendChild(redCircle);
        //
        // Element blueCircle = piechart.getSVGDocument().createElementNS(
        // SVGDOMImplementation.SVG_NAMESPACE_URI,
        // SVGConstants.SVG_CIRCLE_TAG);
        // blueCircle.setAttributeNS(null, SVGConstants.SVG_CX_ATTRIBUTE,
        // "100");
        // blueCircle.setAttributeNS(null, SVGConstants.SVG_CY_ATTRIBUTE, "80");
        // blueCircle.setAttributeNS(null, "r", "36");
        // blueCircle.setAttributeNS(null, "stroke", "blue");
        // blueCircle.setAttributeNS(null, "stroke-width", "71");
        // int degree = (int) ((current * 360.0) / all);
        // blueCircle.setAttributeNS(null, "stroke-dasharray", degree * 0.70 +
        // " "
        // + ((360 - degree) * 0.70));
        // blueCircle.setAttributeNS(null, "fill", "red");
        //
        // piechart.getSVGDocument().getDocumentElement().appendChild(blueCircle);

        add(piechart, gbc);

        confSlider.addChangeListener(new SliderListener(false));
        minSupSlider.addChangeListener(new SliderListener(true));

    }

    public void update(int numberOfCurrentAssocitaionrules,
            int numberOfAllCurrentAssocitaionrules) {
        current = numberOfCurrentAssocitaionrules;
        all = numberOfAllCurrentAssocitaionrules;
        piechart.repaint();
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        super.addPropertyChangeListener(listener);
        propertyChangeSupport.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        super.removePropertyChangeListener(listener);
        propertyChangeSupport.removePropertyChangeListener(listener);
    }

    private void myFirePropertyChange(String propertyName, Object oldValue,
            Object newValue) {
        propertyChangeSupport.firePropertyChange(propertyName, oldValue,
                newValue);
    }

    // ////////////////////////////////////////////////////////////////////////////////////////

    private class SliderListener implements ChangeListener {

        private boolean minSup;

        public SliderListener(boolean minSup) {
            this.minSup = minSup;
        }

        public void stateChanged(ChangeEvent e) {
            JSlider slider = (JSlider) e.getSource();
            double value = slider.getValue() / 100.0;
            if (minSup) {
                minSupLabel.setText("Minimal Support " + value);

                AssociationSettings.this.myFirePropertyChange(
                        "MinimalSupportChanged", 0, value);
            } else {
                confLabel.setText("Confidence " + value);
                AssociationSettings.this.myFirePropertyChange(
                        "ConfidenceChanged", 0, value);
            }
        }
    }
}
