package fcatools.conexpng.gui.contexteditor;

import de.tudresden.inf.tcs.fcaapi.exception.IllegalObjectException;
import de.tudresden.inf.tcs.fcalib.FullObject;
import fcatools.conexpng.Conf;
import fcatools.conexpng.model.FormalContext;

import javax.swing.table.AbstractTableModel;

import static fcatools.conexpng.Util.clamp;

/**
 * ContextMatrixModel allows the separation between the data and its presentation in the JTable.
 * Whenever the context is changed the changes are reflected (automatically) in the corresponding
 * JTable. In particular, if the user changes the context through the context editor what really
 * happens is that the context is changed (not the JTable per se) and the JTable is redrawn based
 * on the updated context.
 */
public class ContextMatrixModel extends AbstractTableModel implements Reorderable {

    private static final long serialVersionUID = -1509387655329719071L;

    private FormalContext context;
    // Only needed for 'contextChanged' method when renaming s.th.
    private Conf state;

    public ContextMatrixModel(Conf state) {
        loadNewContext(state);
    }

    public void loadNewContext(Conf state) {
        this.state = state;
        this.context = state.context;
    }

    @Override
    public boolean isCellEditable(int i, int j) {
        return (i+j > 0) && (i == 0 || j == 0);
    }

    @Override
    public int getRowCount() {
        return context.getObjectCount() + 1;
    }

    @Override
    public int getColumnCount() {
        return context.getAttributeCount() + 1;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        if (columnIndex == 0 && rowIndex == 0) {
            return "";
        }
        else if (columnIndex == 0) {
            return String.format("%s", context.getObjectAtIndex(rowIndex - 1).getIdentifier());
        }
        else if (rowIndex == 0) {
            return String.format("%s", context.getAttributeAtIndex(columnIndex - 1));
        }
        return context.objectHasAttribute(
                context.getObjectAtIndex(rowIndex - 1),
                context.getAttributeAtIndex(columnIndex - 1)) ? "X" : "";
    }

    @Override
    public void setValueAt(Object value, int i, int j) {

    }

    public String getAttributeNameAt(int i) {
        return context.getAttributeAtIndex(i);
    }

    public String getObjectNameAt(int i) {
        return context.getObjectAtIndex(i).getIdentifier();
    }

    public boolean renameAttribute(String oldName, String newName) {
        if (context.existsAttributeAlready(newName)) {
            return false;
        } else {
            state.saveConf();
            context.renameAttribute(oldName, newName);
            state.contextChanged();
            state.makeRedoable();
            return true;
        }
    }

    public boolean renameObject(String oldName, String newName) {
        if (context.existsObjectAlready(newName)) {
            return false;
        } else {
            state.saveConf();
            context.renameObject(oldName, newName);
            state.makeRedoable();
            state.contextChanged();
            return true;
        }
    }

    public void reorderRows(int from, int to) {
        if (context.getObjectCount() < 2) return;
        if (from < 1 || to < 1) return;
        from -= 1; to -= 1;
        from = clamp(from, 0, context.getObjectCount() - 1);
        to = clamp(to, 0, context.getObjectCount() - 1);
        FullObject<String, String> o = context.getObjectAtIndex(from);
        state.saveConf();
        try {
            context.removeObject(o.getIdentifier());
        } catch (IllegalObjectException e) {
            e.printStackTrace();
        }
        context.addObjectAt(o, to);
        state.makeRedoable();
    }

    public void reorderColumns(int from, int to) {
        if (context.getAttributeCount() < 2) return;
        if (from < 1 || to < 1) return;
        from -= 1; to -= 1;
        from = clamp(from, 0, context.getAttributeCount() - 1);
        to = clamp(to, 0, context.getAttributeCount() - 1);
        String a = context.getAttributeAtIndex(from);
        state.saveConf();
        context.removeAttributeInternal(a);
        context.addAttributeAt(a, to);
        state.makeRedoable();
    }

}
