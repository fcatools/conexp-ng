package fcatools.conexpng.gui.dependencies;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.alee.laf.label.WebLabel;
import com.alee.laf.panel.WebPanel;
import com.alee.laf.radiobutton.WebRadioButton;
import com.alee.laf.slider.WebSlider;
import com.alee.laf.text.WebTextField;

import fcatools.conexpng.GUIConf;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

public class DependencySettings extends JPanel {

    private PropertyChangeSupport propertyChangeSupport;

    private static final long serialVersionUID = -3692280021161777005L;

    WebLabel minSupLabel = new WebLabel("Support");
    WebTextField supField = new WebTextField("");
    WebSlider minSupSlider = new WebSlider(0, 100, 0);

    WebLabel confLabel = new WebLabel("Confidence");
    WebTextField confField = new WebTextField("");
    WebSlider confSlider = new WebSlider(0, 100, 0);

    // Only for testing
    private int current = 0, all = 0;

    @SuppressWarnings("serial")
    WebPanel piechart = new WebPanel() {

        @Override
        public void paint(Graphics g2) {

            super.paint(g2);
            Graphics2D g = (Graphics2D) g2;
            g.addRenderingHints(new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON));
            g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

            int degree = (int) ((current * 360.0) / all);
            g.setColor(getBackground());
            g.fillRect(0, 0, getWidth(), getHeight());
            Color gray = new Color(225, 225, 225);
            g.setPaint(gray);
            g.fillArc(0, 5, 140, 140, 0, 360);
            g.setPaint(gray.darker().darker());
            g.drawString("#Association rules = " + all, 0, 165);

            Color green = new Color(150, 220, 150);
            g.setColor(green);
            g.fillArc(0, 5, 140, 140, 90, degree);
            g.setColor(green.darker().darker());
            g.drawString("#With support = " + current, 0, 180);
        }
    };

    private GUIConf state;

    public void setGuiConf(GUIConf guiConf) {
        state = guiConf;
    }

    public DependencySettings(GUIConf state) {
        this.state = state;
        supField.setText("" + state.support);
        minSupSlider.setValue((int) (state.support * 100));
        confField.setText("" + state.confidence);
        confSlider.setValue((int) (state.confidence * 100));
        propertyChangeSupport = new PropertyChangeSupport(this);
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        minSupSlider.setPreferredSize(new Dimension(150, 25));
        confSlider.setPreferredSize(new Dimension(150, 25));
        gbc.insets = new Insets(10, 5, 0, 0);
        gbc.gridx = 0;
        gbc.gridy = 0;
        WebLabel title = new WebLabel("Associations Settings");
        add(title, gbc);
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.WEST;
        add(minSupLabel, gbc);
        gbc.gridx = 1;
        supField.setPreferredSize(new Dimension(40, 20));
        add(supField, gbc);
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        add(minSupSlider, gbc);
        gbc.gridwidth = 1;
        gbc.gridy = 3;
        add(confLabel, gbc);
        gbc.gridx = 1;
        confField.setPreferredSize(new Dimension(40, 20));
        add(confField, gbc);
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        add(confSlider, gbc);
        gbc.gridy = 5;
        piechart.setPreferredSize(new Dimension(150, 200));
        add(piechart, gbc);
        gbc.gridy = 6;
        add(new WebLabel("Together with Implications"), gbc);
        gbc.gridy = 7;
        add(new WebLabel("Sorting by:"), gbc);
        Action sortAction = new SortAction();
        WebRadioButton lexicalSorting = new WebRadioButton();
        lexicalSorting.setAction(sortAction);
        lexicalSorting.setText("Lexical order");
        lexicalSorting.setMnemonic(KeyEvent.VK_L);
        lexicalSorting.setActionCommand("LexicalOrder");

        WebRadioButton supportSorting = new WebRadioButton();
        supportSorting.setAction(sortAction);
        supportSorting.setText("Support");
        supportSorting.setMnemonic(KeyEvent.VK_S);
        supportSorting.setActionCommand("Support");
        if (state.lexsorting)
            lexicalSorting.setSelected(true);
        else
            supportSorting.setSelected(true);
        // Group the radio buttons.
        ButtonGroup group = new ButtonGroup();
        group.add(lexicalSorting);
        group.add(supportSorting);
        gbc.gridy = 8;
        add(lexicalSorting, gbc);
        gbc.gridy = 9;
        add(supportSorting, gbc);
        confSlider.addChangeListener(new SliderListener(false));
        minSupSlider.addChangeListener(new SliderListener(true));
        confField.addKeyListener(new TextFieldAction(false));
        supField.addKeyListener(new TextFieldAction(true));
    }

    public void update(int numberOfCurrentAssocitaionrules, int numberOfAllCurrentAssocitaionrules) {
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

    private void myFirePropertyChange(String propertyName, Object oldValue, Object newValue) {
        propertyChangeSupport.firePropertyChange(propertyName, oldValue, newValue);
    }

    // ////////////////////////////////////////////////////////////////////////////////////////

    @SuppressWarnings("serial")
    private class SortAction extends AbstractAction {

        @Override
        public void actionPerformed(ActionEvent arg0) {
            if (arg0.getActionCommand().equals("LexicalOrder")) {
                if (state.lexsorting)
                    return;
                else {
                    state.lexsorting = !state.lexsorting;
                    myFirePropertyChange("ToggleSortingOrder", null, null);
                }
            } else {
                if (!state.lexsorting)
                    return;
                else {
                    state.lexsorting = !state.lexsorting;
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
            } else if (!(e.getKeyChar() == '.' && ((WebTextField) e.getSource()).getText().indexOf('.') < 0)
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
            WebSlider slider = (WebSlider) e.getSource();
            double value = slider.getValue() / 100.0;
            if (minSup) {
                supField.setText("" + value);
                if (!slider.getValueIsAdjusting()) {
                    state.support = value;
                    DependencySettings.this.myFirePropertyChange("MinimalSupportChanged", 0, value);
                }
            } else {
                confField.setText("" + value);
                state.confidence = value;
                DependencySettings.this.myFirePropertyChange("ConfidenceChanged", 0, value);
            }
        }
    }
}
