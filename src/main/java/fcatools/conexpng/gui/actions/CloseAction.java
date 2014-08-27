package fcatools.conexpng.gui.actions;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import fcatools.conexpng.Conf;
import fcatools.conexpng.gui.MainFrame;
import fcatools.conexpng.gui.MainFrame.StillCalculatingDialog;
import fcatools.conexpng.gui.MainFrame.UnsavedChangesDialog;

/**
 * Action that triggers saving files on application exit.
 * 
 * @author Torsten Casselt
 */
public class CloseAction extends AbstractAction {

    /**
     * 
     */
    private static final long serialVersionUID = -5036339316801331386L;
    private MainFrame mainFrame;
    private Conf state;
    private boolean canceled;

    /**
     * Creates an object for an open/save dialog.
     * 
     * @param mainFrame
     *            to attach dialogs to
     * @param state
     *            to fetch save state from
     */
    public CloseAction(MainFrame mainFrame, Conf state) {
        this.mainFrame = mainFrame;
        this.state = state;
    }

    @Override
    public void actionPerformed(ActionEvent arg0) {
        if (!state.canBeSaved()) {
            StillCalculatingDialog scd = mainFrame.new StillCalculatingDialog();
            if (scd.isYes())
                return;
        }
        if (state.unsavedChanges) {
            UnsavedChangesDialog usd = mainFrame.new UnsavedChangesDialog();
            if (usd.isYes()) {
                new OpenSaveExportAction(mainFrame, state, false, false).actionPerformed(arg0);
            } else if (usd.isCancel()) {
                canceled = true;
            }
        }
    }

    public boolean canceled() {
        return canceled;
    }
}
