package com.eugenkiss.conexp2.gui;

import java.awt.BorderLayout;
import java.beans.PropertyChangeListener;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JToolBar;

import com.eugenkiss.conexp2.OS;
import com.eugenkiss.conexp2.ProgramState;

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
