package fcatools.conexpng.gui.dependencies;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

public class DependencySettings extends JPanel {

    private PropertyChangeSupport propertyChangeSupport;

    private static final long serialVersionUID = -3692280021161777005L;

    JLabel minSupLabel = new JLabel("Minimal Support");
    JTextField supField = new JTextField("0.1");
    JSlider minSupSlider = new JSlider(0, 100, 10);

    JLabel confLabel = new JLabel("Confidence");
    JTextField confField = new JTextField("0.5");
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
            Color gray = new Color(225, 225, 225);
            g.setPaint(gray);
            g.fillArc(0, 5, 140, 140, 0, 360);
            g.setPaint(gray.darker().darker());
            g.drawString("#Association Rules = " + all, 0, 165);

            Color green = new Color(150, 220, 150);
            g.setColor(green);
            g.fillArc(0, 5, 140, 140, 90, degree);
            g.setColor(green.darker().darker());
            g.drawString("#With minSup = " + current, 0, 180);

//            g.setColor(Color.BLACK);
//            Shape circ = new Ellipse2D.Double(0, 5, 140, 140);
//            g.draw(circ);

        }

    };

    public DependencySettings() {
        propertyChangeSupport = new PropertyChangeSupport(this);
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        minSupSlider.setPreferredSize(new Dimension(150, 25));
        confSlider.setPreferredSize(new Dimension(150, 25));
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(10, 5, 0, 0);
        gbc.gridx = 0;
        gbc.gridy = 0;
        add(minSupLabel, gbc);
        gbc.gridx = 1;
        // TODO: @mac os user: size okay?
        supField.setPreferredSize(new Dimension(30, 20));
        add(supField, gbc);
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 2;
        add(minSupSlider, gbc);
        gbc.gridwidth = 1;
        gbc.gridy = 2;
        add(confLabel, gbc);
        gbc.gridx = 1;
        confField.setPreferredSize(new Dimension(30, 20));
        add(confField, gbc);
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        add(confSlider, gbc);
        gbc.gridy = 4;
        piechart.setPreferredSize(new Dimension(150, 200));
        add(piechart, gbc);
        gbc.gridy = 5;
        add(new JLabel("Sorting by:"), gbc);
        Action sortAction = new SortAction();
        JRadioButton lexical = new JRadioButton();
        lexical.setSelected(true);
        lexical.setAction(sortAction);
        lexical.setText("Lexical order");
        lexical.setMnemonic(KeyEvent.VK_L);
        lexical.setActionCommand("LexicalOrder");

        JRadioButton support = new JRadioButton();
        support.setAction(sortAction);
        support.setText("Support");
        support.setMnemonic(KeyEvent.VK_S);
        support.setActionCommand("Support");

        // Group the radio buttons.
        ButtonGroup group = new ButtonGroup();
        group.add(lexical);
        group.add(support);
        gbc.gridy = 6;
        add(lexical, gbc);
        gbc.gridy = 7;
        add(support, gbc);

        confSlider.addChangeListener(new SliderListener(false));
        minSupSlider.addChangeListener(new SliderListener(true));
        confField.addKeyListener(new TextFieldAction(false));
        supField.addKeyListener(new TextFieldAction(true));
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

    @SuppressWarnings("serial")
    private class SortAction extends AbstractAction {

        boolean lex = true;

        @Override
        public void actionPerformed(ActionEvent arg0) {
            if (arg0.getActionCommand().equals("LexicalOrder")) {
                if (lex)
                    return;
                else {
                    lex = true;
                    myFirePropertyChange("ToggleSortingOrder", null, null);
                }
            } else {
                if (!lex)
                    return;
                else {
                    lex = false;
                    myFirePropertyChange("ToggleSortingOrder", null, null);
                }
            }
        }

    }

    private class TextFieldAction implements KeyListener {

        private boolean minSup;

        public TextFieldAction(boolean minSup) {
            this.minSup = minSup;
        }

        @Override
        public void keyPressed(KeyEvent e) {
            // nothing todo

        }

        @Override
        public void keyReleased(KeyEvent e) {
            // nothing todo

        }

        @Override
        public void keyTyped(KeyEvent e) {

            if (e.getKeyChar() == KeyEvent.VK_ENTER) {
                if (minSup) {
                    double value = Double.parseDouble(supField.getText());
                    minSupSlider.setValue((int) (value * 100));
                } else {
                    double value = Double.parseDouble(confField.getText());
                    confSlider.setValue((int) (value * 100));
                }
            } else if (!(e.getKeyChar() == '.' && ((JTextComponent) e
                    .getSource()).getText().indexOf('.') < 0)
                    && !(e.getKeyChar() >= '0' && e.getKeyChar() <= '9')) {
                e.consume();
            }

        }
    }

    private class SliderListener implements ChangeListener {

        private boolean minSup;

        public SliderListener(boolean minSup) {
            this.minSup = minSup;
        }

        public void stateChanged(ChangeEvent e) {
            JSlider slider = (JSlider) e.getSource();
            double value = slider.getValue() / 100.0;
            if (minSup) {
                supField.setText("" + value);
                DependencySettings.this.myFirePropertyChange(
                        "MinimalSupportChanged", 0, value);
            } else {
                confField.setText("" + value);
                DependencySettings.this.myFirePropertyChange(
                        "ConfidenceChanged", 0, value);
            }
        }
    }
}
