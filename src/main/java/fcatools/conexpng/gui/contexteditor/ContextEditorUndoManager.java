package fcatools.conexpng.gui.contexteditor;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.UndoManager;
import javax.swing.undo.UndoableEdit;

import fcatools.conexpng.Conf;
import fcatools.conexpng.gui.MainToolbar;

/**
 * Undo manager for context editor.
 * 
 * @author Torsten Casselt
 */
public class ContextEditorUndoManager extends UndoManager {

    private static final long serialVersionUID = 8164716784804291609L;
    private Conf conf;
    private boolean undoRedoInProgress;

    /**
     * Creates the undo manager with given configuration.
     * 
     * @param conf
     */
    public ContextEditorUndoManager(Conf conf) {
        this.conf = conf;
    }

    /**
     * Method to define the steps necessary for undo and redo and register the
     * UndoableEdit.
     */
    public void makeRedoable() {
        final Conf curConf = conf.copy(conf);
        final Conf lastConf = conf.copy(conf.lastConf);
        if (!undoRedoInProgress) {
            UndoableEdit undoableEdit = new AbstractUndoableEdit() {
                private static final long serialVersionUID = -4461145596327911434L;

                public void redo() throws javax.swing.undo.CannotRedoException {
                    super.redo();
                    undoRedoInProgress = true;
                    conf.newContext(curConf.context);
                    undoRedoInProgress = false;
                    // change undo/redo button state
                    MainToolbar.getRedoButton().setEnabled(canRedo());
                    MainToolbar.getUndoButton().setEnabled(canUndo());
                }

                public void undo() throws javax.swing.undo.CannotUndoException {
                    super.undo();
                    undoRedoInProgress = true;
                    conf.newContext(lastConf.context);
                    undoRedoInProgress = false;
                    // change undo/redo button state
                    MainToolbar.getRedoButton().setEnabled(canRedo());
                    MainToolbar.getUndoButton().setEnabled(canUndo());
                }
            };

            // Add this undoable edit to the undo manager
            addEdit(undoableEdit);
            // change undo/redo button state
            MainToolbar.getRedoButton().setEnabled(canRedo());
            MainToolbar.getUndoButton().setEnabled(canUndo());
        }
    }
}