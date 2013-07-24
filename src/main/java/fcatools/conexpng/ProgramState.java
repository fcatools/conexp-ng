package fcatools.conexpng;

import de.tudresden.inf.tcs.fcaapi.Concept;
import de.tudresden.inf.tcs.fcalib.FullObject;
import de.tudresden.inf.tcs.fcalib.utils.ListSet;
import fcatools.conexpng.gui.lattice.LatticeGraph;
import fcatools.conexpng.model.AssociationRule;
import fcatools.conexpng.model.FormalContext;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

/**
 * Contains context, lattice, implications, filePath, snapshots etc.
 * <p>
 * Why 'ProgramState'? "Dependency Injection", e.g. for testing purposes a
 * component can be passed a "MockProgramState" very easily and it is better to
 * have a central place for the program's state as opposed to have it scattered
 * throughout different classes. If you want you can see this class as the
 * "Model" in an MVC context.
 *
 */
public class ProgramState {

    public String filePath;
    public Vector<String> lastOpened = new Vector<>(5);
    public FormalContext context;
    public Set<AssociationRule> associations;
    public boolean unsavedChanges = false;
    public Map<Integer, Integer> columnWidths = new HashMap<>();
    public LatticeGraph lattice;
    public ListSet<Concept<FullObject<String, String>, String>> concepts;
    public boolean showObjectLabel = false;
    public boolean showAttributLabel = false;
    public boolean showEdges = true;
    public boolean idealHighlighting = false;
    public int numberOfConcepts;
    private PropertyChangeSupport propertyChangeSupport;

    public ProgramState() {
        propertyChangeSupport = new PropertyChangeSupport(this);
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

    public void newLattice(LatticeGraph lattice2) {
        this.lattice = lattice2;
        firePropertyChange(ContextChangeEvents.NEWLATTICE, null, lattice2);
    }

    public void loadedFile(FormalContext context2, LatticeGraph lattice2) {
        this.context = context2;
        this.lattice = lattice2;
        firePropertyChange(ContextChangeEvents.LOADEDFILE, null, lattice2);
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

}
