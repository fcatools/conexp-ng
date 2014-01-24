package fcatools.conexpng.gui.lattice;

import javax.swing.undo.UndoManager;

import fcatools.conexpng.Conf;

/**
 * Undo manager for lattice view.
 * 
 * @author Torsten Casselt
 */
public class LatticeViewUndoManager extends UndoManager {

    private static final long serialVersionUID = 4418115079346777659L;

    // private Conf conf;
    // private boolean undoRedoInProgress;

    /**
     * Creates the undo manager with given configuration.
     * 
     * @param conf
     */
    public LatticeViewUndoManager(Conf conf) {
        // this.conf = conf;
    }

    /**
     * Method to define the steps necessary for undo and redo and register the
     * UndoableEdit.
     */
    public void makeRedoable() {

    }
}