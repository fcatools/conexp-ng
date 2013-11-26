package fcatools.conexpng.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import com.alee.laf.label.WebLabel;
import com.alee.laf.panel.WebPanel;
import com.alee.laf.progressbar.WebProgressBar;

/**
 * Main status bar of this program. Displays progress information
 * on calculations.
 * 
 * @author Torsten Casselt
 */
@SuppressWarnings("serial")
public class MainStatusBar extends WebPanel implements PropertyChangeListener {

	private WebLabel label;
	private WebProgressBar progressBar;
	
	/**
	 * Creates the status bar with progress bar.
	 */
    public MainStatusBar() {
        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(100, 20));
        WebPanel panel = new WebPanel(new BorderLayout());
        label = new WebLabel("");
        panel.add(label, BorderLayout.CENTER);
        progressBar = new WebProgressBar();
        progressBar.setValue(0);
        panel.add(progressBar, BorderLayout.EAST);
        add(panel, BorderLayout.EAST);
        setVisible(false);
    }
    
    /**
     * Sets the progress bar message.
     * 
     * @param message message to set
     */
    public void setProgressBarMessage(String message) {
    	label.setText(message);
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
    	if ("progress".equals(evt.getPropertyName())) {
            progressBar.setValue((Integer)evt.getNewValue());
        }
    }
}