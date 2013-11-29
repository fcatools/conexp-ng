package fcatools.conexpng.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.HashMap;
import java.util.Map;

import javax.swing.Box;
import javax.swing.BoxLayout;

import com.alee.laf.panel.WebPanel;
import com.alee.laf.progressbar.WebProgressBar;

/**
 * Status bar of this program. Displays progress information on calculations.
 * 
 * @author Torsten Casselt
 */
@SuppressWarnings("serial")
public class StatusBar extends WebPanel {

    private Map<Long, WebProgressBar> progressBarMap = new HashMap<Long, WebProgressBar>();
    private Long id = 0L;
    private WebPanel panel;

    /**
     * Creates the status bar.
     */
    public StatusBar() {
        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(100, 20));
        panel = new WebPanel();
        BoxLayout boxLayout = new BoxLayout(panel, BoxLayout.LINE_AXIS);
        panel.setLayout(boxLayout);
        panel.add(Box.createHorizontalGlue());
        add(panel, BorderLayout.CENTER);
        setVisible(false);
    }

    /**
     * Returns true if no calculations are running at the moment.
     * 
     * @return true if no calculations are running at the moment
     */
    public boolean allCalculationsDone() {
        return progressBarMap.isEmpty();
    }

    /**
     * Sets the progress bar with given id indeterminate.
     * 
     * @param id
     *            id of progress bar
     * @param indeterminate
     *            true if indeterminate, false if not
     */
    public void setIndeterminate(Long id, boolean indeterminate) {
        if (progressBarMap.containsKey(id)) {
            progressBarMap.get(id).setIndeterminate(indeterminate);
        }
    }

    /**
     * Sets the progress bar message.
     * 
     * @param id
     *            id of progress bar
     * @param message
     *            message to set
     */
    public void setProgressBarMessage(Long id, String message) {
        if (progressBarMap.containsKey(id)) {
            progressBarMap.get(id).setString(message);
        }
    }

    /**
     * Sets the progress bar's value.
     * 
     * @param id
     *            id of progress bar to set value
     * @param value
     *            value to set
     */
    public void setProgressBarValue(Long id, int value) {
        if (progressBarMap.containsKey(id)) {
            progressBarMap.get(id).setValue(value);
        }
    }

    /**
     * Creates a progress bar and returns the id with which it is saved.
     * 
     * @return id of progress bar
     */
    public long startCalculation() {
        WebProgressBar pb = new WebProgressBar();
        pb.setStringPainted(true);
        panel.add(pb);
        progressBarMap.put(id, pb);
        return id++;
    }

    /**
     * Removes the progress bar with given id.
     * 
     * @param id
     *            id of progress bar to remove
     */
    public void endCalculation(Long id) {
        panel.remove(progressBarMap.remove(id));
        revalidate();
    }
}