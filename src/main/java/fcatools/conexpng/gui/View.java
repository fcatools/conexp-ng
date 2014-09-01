package fcatools.conexpng.gui;

import static javax.swing.JSplitPane.HORIZONTAL_SPLIT;

import java.awt.BorderLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JComponent;
import javax.swing.border.EmptyBorder;

import com.alee.laf.panel.WebPanel;
import com.alee.laf.splitpane.WebSplitPane;
import com.alee.laf.toolbar.WebToolBar;

import fcatools.conexpng.Conf;

public abstract class View extends WebPanel implements PropertyChangeListener {

    protected static final long serialVersionUID = -873702052790459127L;

    protected Conf state;

    protected WebToolBar toolbar;

    protected JComponent view, settingsPanel;

    protected WebPanel panel;

    protected WebSplitPane splitPane;

    public View(Conf state) {
        this.state = state;
        state.addPropertyChangeListener(this);
        toolbar = new WebToolBar(WebToolBar.VERTICAL);
        panel = new WebPanel();
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(3, 3, 3, 3));
    }

    protected void init() {
        panel.setLayout(new BorderLayout());
        if (toolbar != null) {
            toolbar.setFloatable(false);
            panel.add(toolbar, BorderLayout.WEST);
        }
        panel.add(view, BorderLayout.CENTER);

        splitPane = new WebSplitPane(HORIZONTAL_SPLIT, settingsPanel, panel);
        splitPane.setOneTouchExpandable(true);
        splitPane.setContinuousLayout(true);
        add(splitPane);
    }

    /**
     * Returns the state.
     * 
     * @return
     */
    public Conf getState() {
        return state;
    }

    /**
     * Returns the settings.
     * 
     * @return
     */
    public JComponent getSettings() {
        return settingsPanel;
    }

    @Override
    public void setVisible(boolean aFlag) {
        boolean old = isVisible();
        super.setVisible(aFlag);
        propertyChange(new PropertyChangeEvent(this, "visibilityChanged", old, aFlag));
    }
}