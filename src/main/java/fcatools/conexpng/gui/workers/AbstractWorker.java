package fcatools.conexpng.gui.workers;

import java.awt.Cursor;

import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

import fcatools.conexpng.gui.MainStatusBar;

public abstract class AbstractWorker extends SwingWorker<Void, Void> {

	protected MainStatusBar statusBar;
	
	/**
	 * Sets wait cursor and status bar visible.
	 */
	protected void init() {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				setProgress(0);
				statusBar.setVisible(true);
				statusBar.getParent().setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
			}
		});
	}
	
	/**
	 * Sets the progress bar message in status bar in EDT.
	 * 
	 * @param message message to set
	 */
	protected void setProgressBarMessage(final String message) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				statusBar.setProgressBarMessage(message);
			}
		});
	}
	
	/*
	 * executed in EDT so no computations here.
	 */
	@Override
	protected void done() {
		statusBar.setVisible(false);
		statusBar.getParent().setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
		super.done();
	}
}
