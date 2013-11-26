package fcatools.conexpng.gui.workers;

import java.awt.Cursor;
import java.lang.reflect.InvocationTargetException;

import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

import fcatools.conexpng.gui.StatusBar;

/**
 * Abstract worker for every SwingWorker to inherit from. Basic operations
 * necessary for progress bar usage etc. are already implemented.
 * 
 * @author Torsten Casselt
 */
public abstract class AbstractWorker extends SwingWorker<Void, Void> {

	protected StatusBar statusBar;
	protected Long progressBarId;

	public AbstractWorker(Long progressBarId) {
		this.progressBarId = progressBarId;
	}

	/**
	 * Sets wait cursor and status bar visible.
	 */
	protected void init() {
		try {
			// need to be invoke and wait to ensure that it gets
			// executed before the worker starts. Else it is
			// possible that the worker finishes faster than
			// the EDT runs this thread resulting in wait
			// cursor being applied after finishing the worker
			// and the status bar staying visible.
			SwingUtilities.invokeAndWait(new Runnable() {
				@Override
				public void run() {
					setProgress(0);
					statusBar.setVisible(true);
					statusBar.getParent().setCursor(
							Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
				}
			});
		} catch (InvocationTargetException | InterruptedException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Sets the progress bar message in status bar in EDT.
	 * 
	 * @param message
	 *            message to set
	 */
	protected void setProgressBarMessage(final String message) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				statusBar.setProgressBarMessage(progressBarId, message);
			}
		});
	}

	/*
	 * executed in EDT so no computations here.
	 */
	@Override
	protected void done() {
		// remove progress bar from status bar
		statusBar.endCalculation(progressBarId);
		// check if all calculations are done. If so, set back
		// mouse cursor to default and hide status bar. Else
		// just leave it.
		if (statusBar.allCalculationsDone()) {
			statusBar.setVisible(false);
			statusBar.getParent().setCursor(
					Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
		}
		super.done();
	}
}
