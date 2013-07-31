package fcatools.conexpng;

import de.tudresden.inf.tcs.fcaapi.Concept;
import de.tudresden.inf.tcs.fcaapi.FCAImplication;
import de.tudresden.inf.tcs.fcaapi.exception.IllegalObjectException;
import de.tudresden.inf.tcs.fcalib.FullObject;
import fcatools.conexpng.gui.MainToolbar;
import fcatools.conexpng.gui.lattice.LatticeGraph;
import fcatools.conexpng.model.AssociationRule;
import fcatools.conexpng.model.FormalContext;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Set;
import java.util.Vector;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.UndoManager;
import javax.swing.undo.UndoableEdit;

/**
 * Contains context, lattice, implications, filePath, snapshots etc.
 * <p>
 * Why 'Conf'? "Dependency Injection", e.g. for testing purposes a component can
 * be passed a "MockConfiguration" very easily and it is better to have a
 * central place for the program's state as opposed to have it scattered
 * throughout different classes. If you want you can see this class as the
 * "Model" in an MVC context.
 *
 */
public class Conf extends UndoManager {
    private static final long serialVersionUID = 1L;
    public String filePath;
    public Vector<String> lastOpened = new Vector<>(5);
    public FormalContext context;
    public Set<AssociationRule> associations;
    public Set<FCAImplication<String>> implications;
    public boolean unsavedChanges = false;
    public LatticeGraph lattice;
    public Set<Concept<String, FullObject<String, String>>> concepts;
    public GUIConf guiConf;
    // TODO: @Jan this should be moved to the guiConf
    public boolean showObjectLabel = false;
    public boolean showAttributLabel = false;
    public boolean showEdges = true;
    public boolean idealHighlighting = false;

    private PropertyChangeSupport propertyChangeSupport;

    public Conf() {
        propertyChangeSupport = new PropertyChangeSupport(this);
        guiConf = new GUIConf();
    }

    public int getNumberOfConcepts() {
        return concepts.size();
    }

    private Conf lastConf;
    private boolean undoRedoInProgress;

    public void saveConf() {
        lastConf = copy(this);
    }

    public Conf copy(Conf conf){
        Conf copy=new Conf();
        copy.context = new FormalContext();
        copy.context.addAttributes(conf.context.getAttributes());
        try {
            copy.context.addObjects(conf.context.getObjects());
        } catch (IllegalObjectException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return copy;
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.removePropertyChangeListener(listener);
    }

    private void firePropertyChange(ContextChangeEvents cce, Object oldValue, Object newValue) {
        propertyChangeSupport.firePropertyChange(new ContextChangeEvent(this, cce, oldValue, newValue));
    }

    public static final int START = 1;

    public static final int STOP = 2;

    public enum StatusMessage {

        LOADINGFILE("Loading the context"), SAVINGFILE("Saving the context"), CALCULATINGASSOCIATIONS(
                "Calculating the associations"), CALCULATINGIMPLICATIONS("Calculating the implications"), CALCULATINGCONCEPTS(
                "Calculating the concepts"), CALCULATINGLATTICE("Calculating the lattice");

        private StatusMessage(String name) {
            this.name = name;
        }

        private final String name;

        public String toString() {
            return name;
        }
    }

    public class StatusBarMessage extends PropertyChangeEvent {

        private static final long serialVersionUID = 1L;

        public StatusBarMessage(Object source, String propertyName, Object oldValue, Object newValue) {
            super(source, propertyName, oldValue, newValue);
        }

        public StatusBarMessage(Object source, StatusMessage status, Object oldValue, Object newValue) {
            this(source, status.toString(), oldValue, newValue);
        }
    }

    private void fireStatusBarPropertyChange(StatusMessage status, int newValue) {
        propertyChangeSupport.firePropertyChange(new StatusBarMessage(this, status, 0, newValue));
    }

    public void contextChanged() {
        this.context.clearConsidered();
        firePropertyChange(ContextChangeEvents.CONTEXTCHANGED, null, context);
    }

    public void newContext(FormalContext context) {

        this.context = context;
        this.context.clearConsidered();
        firePropertyChange(ContextChangeEvents.NEWCONTEXT, null, context);
    }

    public void attributeNameChanged(String oldName, String newName) {
        firePropertyChange(ContextChangeEvents.ATTRIBUTENAMECHANGED, oldName, newName);
    }

    public void showLabelsChanged() {
        firePropertyChange(ContextChangeEvents.LABELSCHANGED, null, null);
    }

    public void temporaryContextChanged() {
        firePropertyChange(ContextChangeEvents.TEMPORARYCONTEXTCHANGED, null, null);
    }

    public void startCalculation(StatusMessage status) {
        fireStatusBarPropertyChange(status, START);
    }

    public void endCalculation(StatusMessage status) {
        fireStatusBarPropertyChange(status, STOP);
    }

    public void loadedFile() {
        firePropertyChange(ContextChangeEvents.LOADEDFILE, null, lattice);
    }

    @SuppressWarnings("serial")
    public class ContextChangeEvent extends PropertyChangeEvent {

        private ContextChangeEvents cce;

        public ContextChangeEvent(Object source, String propertyName, Object oldValue, Object newValue) {
            super(source, propertyName, oldValue, newValue);
        }

        public ContextChangeEvent(Object source, ContextChangeEvents cce, Object oldValue, Object newValue) {
            super(source, cce.toString(), oldValue, newValue);
            this.cce = cce;
        }

        public ContextChangeEvents getName() {
            return cce;
        }
    }

    public void setNewFile(String filepath) {
        lastOpened.remove(filepath);
        if (!filePath.equals(System.getProperty("user.home"))) {
            lastOpened.add(0, this.filePath);
            if (lastOpened.size() > 5)
                lastOpened.remove(5);
        }
        filePath = filepath;
    }

    @SuppressWarnings("serial")
    public void makeRedoable() {
        if (!undoRedoInProgress) {
            UndoableEdit undoableEdit = new AbstractUndoableEdit() {
                final Conf curConf = copy(Conf.this);
                final Conf lastConf = copy(Conf.this.lastConf);

                // Method that is called when we must redo the undone action
                public void redo() throws javax.swing.undo.CannotRedoException {
                    super.redo();
                    context = curConf.context;
                    undoRedoInProgress = true;
                    newContext(context);
                    undoRedoInProgress = false;
                    MainToolbar.getRedoButton().setEnabled(canRedo());
                    MainToolbar.getUndoButton().setEnabled(canUndo());
                }

                public void undo() throws javax.swing.undo.CannotUndoException {
                    super.undo();
                    context = lastConf.context;

                    undoRedoInProgress = true;
                    newContext(context);
                    undoRedoInProgress = false;
                    MainToolbar.getRedoButton().setEnabled(canRedo());
                    MainToolbar.getUndoButton().setEnabled(canUndo());
                }
            };

            // Add this undoable edit to the undo manager
            addEdit(undoableEdit);
            MainToolbar.getRedoButton().setEnabled(canRedo());
            MainToolbar.getUndoButton().setEnabled(canUndo());
        }
        lastConf=copy(this);

    }

    public static void init() {
        // TODO Auto-generated method stub

    }

}
