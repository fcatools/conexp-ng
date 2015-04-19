package fcatools.conexpng.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;

import com.alee.laf.panel.WebPanel;
import com.alee.laf.progressbar.WebProgressBar;

import fcatools.conexpng.gui.workers.AbstractWorker;
import fcatools.conexpng.io.locale.LocaleHandler;

/**
 * Status bar of this program. Displays progress information on calculations. Designed as a singleton.
 * 
 * @author Torsten Casselt
 */
@SuppressWarnings("serial")
public class StatusBar extends WebPanel {

    private Map<Long, AbstractWorker> calculationMap = new HashMap<Long, AbstractWorker>();
    private Map<Long, WebProgressBar> progressBarMap = new HashMap<Long, WebProgressBar>();
    private Long id = 0L;
    private WebPanel panel;
    private static StatusBar statusBar;

    /**
     * Creates the status bar.
     */
    private StatusBar() {
        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(100, 20));
        panel = new WebPanel();
        BoxLayout boxLayout = new BoxLayout(panel, BoxLayout.LINE_AXIS);
        panel.setLayout(boxLayout);
        panel.add(Box.createHorizontalGlue());
        add(panel, BorderLayout.CENTER);
        setVisible(false);
    };

    /**
     * Creates the status bar if it does not exist already and returns it.
     */
    public static StatusBar getInstance() {
        if (statusBar == null) {
            statusBar = new StatusBar();
        }
        return statusBar;
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
     * Adds a calculation to the status bar to be able to cancel it with a
     * button.
     * 
     * @param id
     *            of progress bar
     * @param aw
     *            worker with calculation
     */
    public void addCalculation(Long id, AbstractWorker aw) {
        calculationMap.put(id, aw);
    }

    /**
     * Creates a progress bar and returns the id with which it is saved.
     * 
     * @return id of progress bar
     */
    public long startCalculation() {
        WebProgressBar pb = new WebProgressBar();
        pb.setStringPainted(true);
        pb.setLayout(new BoxLayout(pb, BoxLayout.LINE_AXIS));
        pb.add(Box.createHorizontalGlue());
        JButton cancelButton = new JButton("x");
        // save current id as final value for action listener to use; else it
        // would take the current value at runtime
        final Long currId = id;
        cancelButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent arg0) {
                if (calculationMap.get(currId) != null) {
                    calculationMap.get(currId).cancel(true);
                }
            }
        });
        cancelButton.setToolTipText(LocaleHandler.getString("StatusBar.startCalculation.cancelButton.toolTip"));
        pb.add(cancelButton);
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