package fcatools.conexpng.gui;

import fcatools.conexpng.OS;
import fcatools.conexpng.ProgramState;

import javax.swing.*;
import java.awt.*;
import java.beans.PropertyChangeListener;

public abstract class View extends JPanel implements PropertyChangeListener {

    protected static final long serialVersionUID = -873702052790459127L;

    protected ProgramState state;

    protected JToolBar toolbar = new JToolBar(JToolBar.VERTICAL);

    protected JComponent view, settings;

    protected JPanel panel;

    protected JSplitPane splitPane;

    View(ProgramState state) {
        this.state = state;
        state.addPropertyChangeListener(this);
    }

    protected void init() {
        setLayout(new BorderLayout());
        panel = new JPanel();
        panel.setLayout(new BorderLayout());

        toolbar.setFloatable(false);

        panel.add(toolbar, BorderLayout.WEST);
        panel.add(view, BorderLayout.CENTER);

        // Important to make split pane divider properly visible on osx
        if (OS.isMacOsX) {
            settings.setBorder(BorderFactory.createLoweredSoftBevelBorder());
            panel.setBorder(BorderFactory.createLoweredSoftBevelBorder());
        }

        splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, settings, panel);
        splitPane.setOneTouchExpandable(true);
        splitPane.setBorder(null);
        add(splitPane);
    }

}
