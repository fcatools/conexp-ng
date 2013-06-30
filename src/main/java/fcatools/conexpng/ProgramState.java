package fcatools.conexpng;

import fcatools.conexpng.gui.lattice.LatticeGraph;
import fcatools.conexpng.model.AssociationRule;
import fcatools.conexpng.model.FormalContext;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import de.tudresden.inf.tcs.fcaapi.Concept;
import de.tudresden.inf.tcs.fcalib.FullObject;
import de.tudresden.inf.tcs.fcalib.utils.ListSet;

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
    public String lastOpened;
    public FormalContext context;
    public Set<AssociationRule> associations;
    public boolean unsavedChanges = false;
    public Map<Integer, Integer> columnWidths = new HashMap<>();
    public LatticeGraph lattice;
    public boolean showObjectLabel = false;
    public boolean showAttributLabel = false;
    public boolean showEdges = true;
    public boolean idealHighlighting = false;

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

    private void firePropertyChange(ContextChangeEvents cce, Object oldValue,
            Object newValue) {
        propertyChangeSupport.firePropertyChange(new ContextChangeEvent(this,
                cce, oldValue, newValue));
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
        firePropertyChange(ContextChangeEvents.ATTRIBUTENAMECHANGED, oldName,
                newName);
    }

    public void showLabelsChanged(){
        firePropertyChange(ContextChangeEvents.LABELSCHANGED, null, null);
    }
    
    public void temporaryContextChanged(){
    	firePropertyChange(ContextChangeEvents.TEMPORARYCONTEXTCHANGED, null, null);
    }

    public void startCalculation(String source){
        firePropertyChange(ContextChangeEvents.STARTCALCULATION, null, source);
    }

    public void endCalculation(){
        firePropertyChange(ContextChangeEvents.ENDCALCULATION, null, null);
    }

    @SuppressWarnings("serial")
    public class ContextChangeEvent extends PropertyChangeEvent {

        private ContextChangeEvents cce;

        public ContextChangeEvent(Object source, String propertyName,
                Object oldValue, Object newValue) {
            super(source, propertyName, oldValue, newValue);
        }

        public ContextChangeEvent(Object source, ContextChangeEvents cce,
                Object oldValue, Object newValue) {
            super(source, cce.toString(), oldValue, newValue);
            this.cce = cce;
        }

        public ContextChangeEvents getName() {
            return cce;
        }
    }
}
