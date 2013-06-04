package com.eugenkiss.conexp2;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import com.eugenkiss.conexp2.model.FormalContext;

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
    public FormalContext context;
    public boolean unsavedChanges = false;

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

    private void firePropertyChange(String propertyName, Object oldValue,
            Object newValue) {
        propertyChangeSupport.firePropertyChange(propertyName, oldValue,
                newValue);
    }

    public void contextChanged() {
        firePropertyChange("ContextChanged", null, context);
    }

    public void attributeNameChanged(String oldName, String newName) {
        firePropertyChange("AttributeNameChanged", oldName, newName);
    }

    
}
