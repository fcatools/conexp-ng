package fcatools.conexpng.gui;

import com.alee.laf.panel.WebPanel;
import com.alee.laf.splitpane.WebSplitPane;
import com.alee.laf.toolbar.WebToolBar;
import fcatools.conexpng.ProgramState;

import javax.swing.*;
import java.awt.*;
import java.beans.PropertyChangeListener;

import static javax.swing.JSplitPane.HORIZONTAL_SPLIT;

public abstract class View extends JPanel implements PropertyChangeListener {

    protected static final long serialVersionUID = -873702052790459127L;

    protected ProgramState state;

    protected WebToolBar toolbar;

    protected JComponent view, settings;

    protected WebPanel panel;

    protected WebSplitPane splitPane;

    public View(ProgramState state) {
        this.state = state;
        toolbar = new WebToolBar(WebToolBar.VERTICAL);
        state.addPropertyChangeListener(this);
    }

    protected void init() {
        setLayout(new BorderLayout());
        panel = new WebPanel();
        panel.setLayout(new BorderLayout());
        if (toolbar != null) {
            toolbar.setFloatable(false);
            panel.add(toolbar, BorderLayout.WEST);
        }
        panel.add(view, BorderLayout.CENTER);

        splitPane = new WebSplitPane(HORIZONTAL_SPLIT, settings, panel);
        splitPane.setOneTouchExpandable(true);
        splitPane.setDividerLocation(170);
        splitPane.setContinuousLayout(true);
        add(splitPane);
    }

}
