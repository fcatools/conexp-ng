package fcatools.conexpng.gui.lattice;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.UndoManager;
import javax.swing.undo.UndoableEdit;

import fcatools.conexpng.gui.MainToolbar;

/**
 * Undo manager for lattice view.
 * 
 * @author Torsten Casselt
 */
public class LatticeViewUndoManager extends UndoManager {

    private static final long serialVersionUID = 4418115079346777659L;

    private boolean undoRedoInProgress;

    /**
     * Method to define the steps necessary for undo and redo and register the
     * UndoableEdit.
     * 
     * @param e
     *            element to undo movement of
     * @param originalElementPosX
     *            original x position of element
     * @param originalElementPosY
     *            original y position of element
     */
    public void makeRedoable(final LatticeGraphElement e, final int originalElementPosX, final int originalElementPosY) {
        if (!undoRedoInProgress) {
            UndoableEdit undoableEdit = new AbstractUndoableEdit() {
                private static final long serialVersionUID = -4461145596327911434L;
                final int x = e.getX();
                final int y = e.getY();

                public void redo() throws javax.swing.undo.CannotRedoException {
                    super.redo();
                    undoRedoInProgress = true;
                    e.update(x, y);
                    undoRedoInProgress = false;
                    // change undo/redo button state
                    MainToolbar.getRedoButton().setEnabled(canRedo());
                    MainToolbar.getUndoButton().setEnabled(canUndo());
                }

                public void undo() throws javax.swing.undo.CannotUndoException {
                    super.undo();
                    undoRedoInProgress = true;
                    e.update(originalElementPosX, originalElementPosY);
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