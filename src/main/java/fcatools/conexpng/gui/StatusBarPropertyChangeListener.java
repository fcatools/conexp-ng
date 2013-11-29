package fcatools.conexpng.gui;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/**
 * Listener to address changes in progress of calculations.
 * 
 * @author Torsten Casselt
 */
public class StatusBarPropertyChangeListener implements PropertyChangeListener {

    private Long progressBarId;
    private StatusBar statusBar;

    /**
     * Creates a listener.
     * 
     * @param progressBarId
     *            id of progress bar to update on change
     * @param statusBar
     *            status bar to access progress bar
     */
    public StatusBarPropertyChangeListener(Long progressBarId, StatusBar statusBar) {
        this.progressBarId = progressBarId;
        this.statusBar = statusBar;
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if ("progress".equals(evt.getPropertyName())) {
            statusBar.setProgressBarValue(progressBarId, (Integer) evt.getNewValue());
        }
    }

}
