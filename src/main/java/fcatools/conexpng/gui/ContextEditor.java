package fcatools.conexpng.gui;

import de.tudresden.inf.tcs.fcaapi.exception.IllegalObjectException;
import de.tudresden.inf.tcs.fcalib.FullObject;
import fcatools.conexpng.ProgramState;
import fcatools.conexpng.model.FormalContext;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.event.TableModelEvent;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.Map;

import static fcatools.conexpng.gui.Util.*;
import static javax.swing.KeyStroke.getKeyStroke;

/**
 * The class responsible for displaying and interacting with ConExpNG's context editor.
 * The main component of this view is a customised JTable, that is more akin to a spreadsheet
 * editor, serving as our context editor. To this end, there are several additional classes
 * in this file.
 *
 * Notes:
 *
 * Generally, the code between ContextEditor and ContextMatrix is divided as per the following guidelines:
 *
 * - More general code is in ContextMatrix.
 * - Code that also pertains to other parts of the context editor
 *   (e.g. toolbar) other than the matrix is in ContextEditor.
 * - Code that needs to know about the MatrixModel specifically and not only
 *   about AbstractTableModel is in ContextEditor as ContextMatrix should not
 *   be coupled with a concrete model in order to have a seperation between
 *   model and view.
 *
 * E.g. PopupMenu code is in ContextEditor (and not in ContextMatrix) as for
 * a different Model one would probably use different PopupMenus, that means
 * the PopupMenus are coupled with MatrixModel.
 */
@SuppressWarnings("serial")
public class ContextEditor extends View {

    private static final long serialVersionUID = 1660117627650529212L;

    // Choose correct modifier key (STRG or CMD) based on platform
    @SuppressWarnings("unused")
	private static final int MASK = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();

    // Our JTable customisation and its respective data model
    private final ContextMatrix matrix;
    private final ContextMatrixModel matrixModel;
    // Context menus
    final JPopupMenu cellPopupMenu;
    final JPopupMenu objectCellPopupMenu;
    final JPopupMenu attributeCellPopupMenu;

    // For remembering which header cell has been right-clicked
    // For movement inside the matrix
    // Due to unfortunate implications of our JTable customisation we need to rely on this "hack"
    int lastActiveRowIndex;
    int lastActiveColumnIndex;

    public ContextEditor(final ProgramState state) {
        super(state);

        // Initialize various components
        panel = new JPanel();
        panel.setLayout(new BorderLayout());
        matrixModel = new ContextMatrixModel(state);
        matrix = new ContextMatrix(matrixModel, panel.getBackground(), state.columnWidths);
        Border margin = new EmptyBorder(1, 3, 1, 4);
        Border border = BorderFactory.createMatteBorder(1, 1, 0, 0, new Color(220,220,220));
        JScrollPane scrollPane = ContextMatrix.createStripedJScrollPane(matrix, panel.getBackground());
        scrollPane.setBorder(border);
        toolbar.setFloatable(false);
        toolbar.setBorder(margin);
        panel.add(toolbar, BorderLayout.WEST);
        panel.add(scrollPane, BorderLayout.CENTER);
        setLayout(new BorderLayout());
        add(panel);
        cellPopupMenu = new JPopupMenu();
        objectCellPopupMenu = new JPopupMenu();
        attributeCellPopupMenu = new JPopupMenu();

        // Add actions
        registerActions();
        createMouseActions();
        createKeyActions();
        createButtonActions();
        createContextMenuActions();

        // Force an update of the table to display it correctly
        matrixModel.fireTableStructureChanged();
    }

    // If context is not changed through the context editor (e.g. by exploration) be sure
    // to reflect these changes inside the matrix
    @Override
    public void propertyChange(PropertyChangeEvent e) {
        if (e.getPropertyName().equals("ContextChanged")) {
            matrixModel.fireTableStructureChanged();
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Behaviour Initialization
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private void registerActions() {
        ActionMap am = matrix.getActionMap();
        am.put("up", new MoveAction(0, -1));
        am.put("down", new MoveAction(0, +1));
        am.put("left", new MoveAction(-1, 0));
        am.put("right", new MoveAction(+1, 0));
        am.put("upCarry", new MoveWithCarryAction(0, -1));
        am.put("downCarry", new MoveWithCarryAction(0, +1));
        am.put("leftCarry", new MoveWithCarryAction(-1, 0));
        am.put("rightCarry", new MoveWithCarryAction(+1, 0));
        am.put("toggle", new ToggleActiveAction());
        am.put("removeObject", new RemoveActiveObjectAction());
        am.put("removeAttribute", new RemoveActiveAttributeAction());
    }

    private void createKeyActions() {
        InputMap im = matrix.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        im.put(getKeyStroke(KeyEvent.VK_UP, 0), "up");
        im.put(getKeyStroke(KeyEvent.VK_DOWN, 0), "down");
        im.put(getKeyStroke(KeyEvent.VK_LEFT, 0), "left");
        im.put(getKeyStroke(KeyEvent.VK_RIGHT, 0), "right");
        im.put(getKeyStroke(KeyEvent.VK_K, 0), "upCarry");
        im.put(getKeyStroke(KeyEvent.VK_J, 0), "downCarry");
        im.put(getKeyStroke(KeyEvent.VK_H, 0), "leftCarry");
        im.put(getKeyStroke(KeyEvent.VK_L, 0), "rightCarry");
        im.put(getKeyStroke(KeyEvent.VK_ENTER, 0), "toggle");
        im.put(getKeyStroke(KeyEvent.VK_T, 0), "toggle");
        im.put(getKeyStroke(KeyEvent.VK_R, 0), "removeObject");
        im.put(getKeyStroke(KeyEvent.VK_R, KeyEvent.SHIFT_MASK), "removeAttribute");
        im.put(getKeyStroke(KeyEvent.VK_O, 0), "addObjectBelow");
        im.put(getKeyStroke(KeyEvent.VK_O, KeyEvent.SHIFT_MASK), "addObjectAbove");
        im.put(getKeyStroke(KeyEvent.VK_A, 0), "addAttributeBelow");
        im.put(getKeyStroke(KeyEvent.VK_A, KeyEvent.SHIFT_MASK), "addAttributeAbove");
    }

    private void createMouseActions() {
        MouseAdapter mouseAdapter = new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                int i = matrix.rowAtPoint(e.getPoint());
                int j = matrix.columnAtPoint(e.getPoint());
                int clicks = e.getClickCount();
                if (clicks >= 2 && clicks % 2 == 0 && SwingUtilities.isLeftMouseButton(e)) { // Double Click
                    if (i > 0 && j > 0) {
                        invokeAction(new ToggleAction(i, j));
                    }
                }
            }
            public void mousePressed(MouseEvent e) { maybeShowPopup(e); }
            public void mouseReleased(MouseEvent e) { maybeShowPopup(e); }
        };
        matrix.addMouseListener(mouseAdapter);
        matrix.addMouseMotionListener(mouseAdapter);
    }

    private void maybeShowPopup(MouseEvent e) {
        int i = matrix.rowAtPoint(e.getPoint());
        int j = matrix.columnAtPoint(e.getPoint());
        lastActiveRowIndex = i;
        lastActiveColumnIndex = j;
        if (e.isPopupTrigger()) {
            if (i == 0 && j == 0) {
                // Don't show a context menu in the matrix corner
            } else if (i > 0 && j > 0) {
                if (matrix.getSelectedColumn() <= 0 || matrix.getSelectedRow() <= 0) {
                    matrix.selectCell(i, j);
                }
                cellPopupMenu.show(e.getComponent(), e.getX(), e.getY());
            } else if (j == 0) {
                objectCellPopupMenu.show(e.getComponent(), e.getX(), e.getY());
            } else {
                attributeCellPopupMenu.show(e.getComponent(), e.getX(), e.getY());
            }
        }
    }

    private void createButtonActions() {
        addToolbarButton("addObject", "Add Object", "conexp/addObj.gif", new AddObjectAtEndAction());
        addToolbarButton("clarifyObjects", "Clarify Objects", "conexp/clarifyObj.gif", null); // TODO
        addToolbarButton("reduceObjects", "Reduce Objects", "conexp/reduceObj.gif", null); // TODO
        toolbar.addSeparator();
        addToolbarButton("addAttribute", "Add Attribute", "conexp/addAttr.gif", new AddAttributeAtEndAction());
        addToolbarButton("clarifyAttributes", "Clarify Attributes", "conexp/clarifyAttr.gif", null); // TODO
        addToolbarButton("reduceAttributes", "Reduce Attributes", "conexp/reduceAttr.gif", null); // TODO
        toolbar.addSeparator();
        addToolbarButton("reduceContext", "Reduce Context", "conexp/reduceCxt.gif", null); // TODO
        addToolbarButton("transposeContext", "Transpose Context", "conexp/transpose.gif", new TransposeAction());
        toolbar.addSeparator();
        addToolbarToggleButton("compactMatrix", "Compact Matrix", "conexp/alignToGrid.gif", new CompactAction()); // TODO
        addToolbarToggleButton("showArrowRelations", "Show Arrow Relations", "conexp/associationRule.gif", null); // TODO
    }

    private void createContextMenuActions() {
        // ------------------------
        // Inner cells context menu
        // ------------------------
        // See issue #42
        /*
        addMenuItem(cellPopupMenu, "Cut", new CutAction());
        addMenuItem(cellPopupMenu, "Copy", new CopyAction());
        addMenuItem(cellPopupMenu, "Paste", new PasteAction());
        */
        addMenuItem(cellPopupMenu, "Select all", new SelectAllAction());
        //--------
        cellPopupMenu.add(new JPopupMenu.Separator());
        //--------
        addMenuItem(cellPopupMenu, "Fill", new FillAction());
        addMenuItem(cellPopupMenu, "Clear", new ClearAction());
        addMenuItem(cellPopupMenu, "Invert", new InvertAction());
        //--------
        cellPopupMenu.add(new JPopupMenu.Separator());
        //--------
        addMenuItem(cellPopupMenu, "Remove attribute(s)", new RemoveSelectedAttributesAction());
        addMenuItem(cellPopupMenu, "Remove object(s)", new RemoveSelectedObjectsAction());

        // ------------------------
        // Object cell context menu
        // ------------------------
        addMenuItem(objectCellPopupMenu, "Rename", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                renameObject(lastActiveRowIndex - 1);
            }
        });
        addMenuItem(objectCellPopupMenu, "Remove", new RemoveActiveObjectAction());
        addMenuItem(objectCellPopupMenu, "Add above", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                addObjectAt(lastActiveRowIndex - 1);
            }
        });
        addMenuItem(objectCellPopupMenu, "Add below", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                addObjectAt(lastActiveRowIndex);
            }
        });

        // ---------------------------
        // Attribute cell context menu
        // ---------------------------
        addMenuItem(attributeCellPopupMenu, "Rename", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                renameAttribute(lastActiveColumnIndex - 1);
            }
        });
        addMenuItem(attributeCellPopupMenu, "Remove", new RemoveActiveAttributeAction());
        addMenuItem(attributeCellPopupMenu, "Add left", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                addAttributeAt(lastActiveColumnIndex - 1);
            }
        });
        addMenuItem(attributeCellPopupMenu, "Add right", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                addAttributeAt(lastActiveColumnIndex);
            }
        });
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Actions
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @SuppressWarnings("UnusedDeclaration")
    class CombineActions extends AbstractAction {
        Action first, second;
        CombineActions(Action first, Action second) {
            this.first = first;
            this.second = second;
        }
        public void actionPerformed(ActionEvent e) {
            invokeAction(first, e);
            invokeAction(second, e);
        }
    }

    class MoveAction extends AbstractAction {
        int horizontal, vertical;
        MoveAction(int horizontal, int vertical) {
            this.horizontal = horizontal;
            this.vertical = vertical;
        }
        public void actionPerformed(ActionEvent e) {
            lastActiveRowIndex = clamp(lastActiveRowIndex + vertical, 1, state.context.getObjectCount());
            lastActiveColumnIndex = clamp(lastActiveColumnIndex + horizontal, 1, state.context.getAttributeCount());
            matrix.selectCell(lastActiveRowIndex, lastActiveColumnIndex);
        }
    }

	class MoveWithCarryAction extends AbstractAction {
        int horizontal, vertical;
        MoveWithCarryAction(int horizontal, int vertical) {
            this.horizontal = horizontal;
            this.vertical = vertical;
        }
        public void actionPerformed(ActionEvent e) {
            if (state.context.getObjectCount() == 0 || state.context.getAttributeCount() == 0) return;
            int i = lastActiveRowIndex + vertical - 1;
            int j = lastActiveColumnIndex + horizontal - 1;
            //noinspection LoopStatementThatDoesntLoop
            while (true) {
                if (i < 0) {
                    j -= 1;
                    i = state.context.getObjectCount() - 1;
                    break;
                }
                if (j < 0) {
                    i -= 1;
                    j = state.context.getAttributeCount() - 1;
                }
                if (i >= state.context.getObjectCount()) {
                    j += 1;
                    i = 0;
                    break;
                }
                if (j >= state.context.getAttributeCount()) {
                    i += 1;
                    j = 0;
                }
                break;
            }
            i = mod(i, state.context.getObjectCount());
            j = mod(j, state.context.getAttributeCount());
            lastActiveRowIndex = i + 1;
            lastActiveColumnIndex = j + 1;
            matrix.selectCell(lastActiveRowIndex, lastActiveColumnIndex);
        }
    }
    class ToggleAction extends AbstractAction {
        int i, j;
        ToggleAction(int i, int j) {
            this.i = i;
            this.j = j;
        }
        public void actionPerformed(ActionEvent e) {
            if (i <= 0 || j <= 0) return;
            int i = clamp(this.i, 1, state.context.getObjectCount()) - 1;
            int j = clamp(this.j, 1, state.context.getAttributeCount()) - 1;
            state.context.toggleAttributeForObject(
                    state.context.getAttributeAtIndex(j),
                    state.context.getObjectAtIndex(i).getIdentifier());
            matrix.saveSelectedInterval();
            matrixModel.fireTableDataChanged();
            matrix.restoreSelectedInterval();
            state.contextChanged();
        }
    }

    class ToggleActiveAction extends AbstractAction {
        public void actionPerformed(ActionEvent e) {
            invokeAction(new ToggleAction(lastActiveRowIndex, lastActiveColumnIndex));
        }
    }

    class SelectAllAction extends AbstractAction {
        public void actionPerformed(ActionEvent e) {
            matrix.selectAll();
        }
    }

    abstract class AbstractFillClearInvertAction extends AbstractAction {
        public void actionPerformed(ActionEvent e) {
            int i1 = matrix.getSelectedRow() - 1;
            int i2 = i1 + matrix.getSelectedRowCount();
            int j1 = matrix.getSelectedColumn() - 1;
            int j2 = j1 + matrix.getSelectedColumnCount();
            matrix.saveSelectedInterval();
            execute(i1, i2, j1, j2);
            matrixModel.fireTableDataChanged();
            matrix.restoreSelectedInterval();
            state.contextChanged();
        }
        abstract void execute(int i1, int i2, int j1, int j2);
    }

    class FillAction extends AbstractFillClearInvertAction {
        void execute(int i1, int i2, int j1, int j2) {
            state.context.fill(i1, i2, j1, j2);
        }
    }

    class ClearAction extends AbstractFillClearInvertAction {
        void execute(int i1, int i2, int j1, int j2) {
            state.context.clear(i1, i2, j1, j2);
        }
    }

    class InvertAction extends AbstractFillClearInvertAction {
        void execute(int i1, int i2, int j1, int j2) {
            state.context.invert(i1, i2, j1, j2);
        }
    }

    class TransposeAction extends AbstractAction {
        public void actionPerformed(ActionEvent e) {
            state.context.transpose();
            matrixModel.fireTableStructureChanged();
            matrix.clearSelection();
            matrix.saveSelectedInterval();
            state.contextChanged();
        }
    }

    class AddAttributeAtAction extends AbstractAction {
        int index;
        AddAttributeAtAction(int index) {
            this.index = index;
        }
        public void actionPerformed(ActionEvent e) {
            matrix.saveSelectedInterval();
            addAttributeAt(index);
            matrix.restoreSelectedInterval();
            state.contextChanged();
        }
    }

    class AddObjectAtAction extends AbstractAction {
        int index;
        AddObjectAtAction(int index) {
            this.index = index;
        }
        public void actionPerformed(ActionEvent e) {
            matrix.saveSelectedInterval();
            addObjectAt(index);
            matrix.restoreSelectedInterval();
            state.contextChanged();
        }
    }

    class AddAttributeAtEndAction extends AbstractAction {
        public void actionPerformed(ActionEvent e) {
            invokeAction(new AddAttributeAtAction(state.context.getAttributeCount()));
        }
    }

    class AddObjectAtEndAction extends AbstractAction {
        public void actionPerformed(ActionEvent e) {
            invokeAction(new AddObjectAtAction(state.context.getObjectCount()));
        }
    }

    class RemoveActiveObjectAction extends AbstractAction {
        public void actionPerformed(ActionEvent e) {
            if (state.context.getObjectCount() == 0) return;
            matrix.saveSelectedInterval();
            try {
                state.context.removeObject(state.context.getObjectAtIndex(lastActiveRowIndex -1).getIdentifier());
                if (lastActiveRowIndex - 1 >= state.context.getObjectCount()) lastActiveRowIndex--;
            } catch (IllegalObjectException e1) {
                e1.printStackTrace();
            }
            matrixModel.fireTableStructureChanged();
            matrix.invalidate();
            matrix.repaint();
            matrix.restoreSelectedInterval();
            state.contextChanged();
        }
    }

    class RemoveActiveAttributeAction extends AbstractAction {
        public void actionPerformed(ActionEvent e) {
            if (state.context.getAttributeCount() == 0) return;
            matrix.saveSelectedInterval();
            state.context.removeAttribute(state.context.getAttributeAtIndex(lastActiveColumnIndex -1));
            matrix.updateColumnWidths(lastActiveColumnIndex);
            if (lastActiveColumnIndex - 1 >= state.context.getAttributeCount()) lastActiveColumnIndex--;
            matrixModel.fireTableStructureChanged();
            matrix.invalidate();
            matrix.repaint();
            matrix.restoreSelectedInterval();
            state.contextChanged();
        }
    }

    class RemoveSelectedObjectsAction extends AbstractAction {
        public void actionPerformed(ActionEvent e) {
            if (state.context.getAttributeCount() == 0) return;
            matrix.saveSelectedInterval();
            int i = Math.min(matrix.lastSelectedRowsStartIndex, matrix.lastSelectedRowsEndIndex) - 1;
            int d = Math.abs(matrix.lastSelectedRowsStartIndex - matrix.lastSelectedRowsEndIndex) + 1;
            for (int unused = 0; unused < d; unused++) {
                try {
                    state.context.removeObject(state.context.getObjectAtIndex(i).getIdentifier());
                } catch (IllegalObjectException e1) {
                    e1.printStackTrace();
                }
            }
            matrixModel.fireTableStructureChanged();
            matrix.invalidate();
            matrix.repaint();
            state.contextChanged();
        }
    }

    class RemoveSelectedAttributesAction extends AbstractAction {
        public void actionPerformed(ActionEvent e) {
            if (state.context.getAttributeCount() == 0) return;
            matrix.saveSelectedInterval();
            int i = Math.min(matrix.lastSelectedColumnsStartIndex, matrix.lastSelectedColumnsEndIndex) - 1;
            int d = Math.abs(matrix.lastSelectedColumnsStartIndex - matrix.lastSelectedColumnsEndIndex) + 1;
            for (int unused = 0; unused < d; unused++) {
                state.context.removeAttribute(state.context.getAttributeAtIndex(i));
                matrix.updateColumnWidths(i + 1);
            }
            matrixModel.fireTableStructureChanged();
            matrix.invalidate();
            matrix.repaint();
            state.contextChanged();
        }
    }

    class CompactAction implements ItemListener {
        public void itemStateChanged(ItemEvent e) {
            if(e.getStateChange() == ItemEvent.SELECTED) {
                matrix.compact();
            }
            else {
                matrix.uncompact();
            }
        }
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Helper functions
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private static void addMenuItem(JPopupMenu menu, String name, ActionListener action) {
        JMenuItem item = new JMenuItem(name);
        menu.add(item);
        item.addActionListener(action);
    }

    private void addToolbarButton(String name, String tooltip, String iconPath, Action action) {
        JButton b = createButton(tooltip, name, iconPath);
        toolbar.add(b);
        b.addActionListener(action);
    }

    private void addToolbarToggleButton(String name, String tooltip, String iconPath, ItemListener itemListener) {
        JToggleButton b = createToggleButton(tooltip, name, iconPath);
        toolbar.add(b);
        b.addItemListener(itemListener);
    }

    private void addAttributeAt(int i) {
        String collisionFreeName = "attr" + i;
        while (true) {
            if (!state.context.existsAttributeAlready(collisionFreeName)) break;
            collisionFreeName = collisionFreeName + "'";
        }
        state.context.addAttributeAt(collisionFreeName, i);
        matrixModel.fireTableStructureChanged();
        renameAttribute(i);
    }

    private void addObjectAt(int i) {
        String collisionFreeName = "obj" + i;
        while (true) {
            if (!state.context.existsObjectAlready(collisionFreeName)) break;
            collisionFreeName = collisionFreeName + "'";
        }
        FullObject<String,String> newObject = new FullObject<>(collisionFreeName);
        state.context.addObjectAt(newObject, i);
        matrixModel.fireTableStructureChanged();
        renameObject(i);
    }

    private void renameAttribute(int i) {
        matrix.editCellAt(0, i + 1);
        matrix.requestFocus();
        ContextMatrix.ContextCellEditor ed = (ContextMatrix.ContextCellEditor) matrix.editor;
        ed.getTextField().requestFocus();
        ed.getTextField().selectAll();
    }

    private void renameObject(int i) {
        matrix.editCellAt(i + 1, 0);
        matrix.requestFocus();
        ContextMatrix.ContextCellEditor ed = (ContextMatrix.ContextCellEditor) matrix.editor;
        ed.getTextField().requestFocus();
        ed.getTextField().selectAll();
    }

    private void invokeAction(Action action, ActionEvent e) {
        action.actionPerformed(e);
    }

    private void invokeAction(Action action) {
        invokeAction(action, new ActionEvent(this, 0, ""));
    }

    @SuppressWarnings("unused")
    private void invokeAction(String name) {
        matrix.getActionMap().get(name).actionPerformed(new ActionEvent(this, 0, ""));
    }

}


////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

/**
 * ContextMatrixModel allows the separation between the data and its presentation in the JTable.
 * Whenever the context is changed the changes are reflected (automatically) in the corresponding
 * JTable. In particular, if the user changes the context through the context editor what really
 * happens is that the context is changed (not the JTable per se) and the JTable is redrawn based
 * on the updated context.
 */
class ContextMatrixModel extends AbstractTableModel implements Reorderable {

    private static final long serialVersionUID = -1509387655329719071L;

    private final FormalContext context;
    // Only needed for 'contextChanged' method when renaming s.th.
    private final ProgramState state;

    ContextMatrixModel(ProgramState state) {
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
            return String.format("<html><div style='margin:2px 4px'><b>%s</b></div></html>",
                    context.getObjectAtIndex(rowIndex - 1).getIdentifier());
        }
        else if (rowIndex == 0) {
            return String.format("<html><div style='margin:2px 4px'><b>%s</b></div></html>",
                    context.getAttributeAtIndex(columnIndex - 1));
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
            context.renameAttribute(oldName, newName);
            state.contextChanged();
            return true;
        }
    }

    public boolean renameObject(String oldName, String newName) {
        if (context.existsObjectAlready(newName)) {
            return false;
        } else {
            context.renameObject(oldName, newName);
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
        try {
            context.removeObject(o.getIdentifier());
        } catch (IllegalObjectException e) {
            e.printStackTrace();
        }
        context.addObjectAt(o, to);
    }

    public void reorderColumns(int from, int to) {
        if (context.getAttributeCount() < 2) return;
        if (from < 1 || to < 1) return;
        from -= 1; to -= 1;
        from = clamp(from, 0, context.getAttributeCount() - 1);
        to = clamp(to, 0, context.getAttributeCount() - 1);
        String a = context.getAttributeAtIndex(from);
        context.removeAttributeInternal(a);
        context.addAttributeAt(a, to);
    }

}


////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

/**
 * ContextMatrix is simply a customisation of JTable in order to make it look & behave more
 * like a spreadsheet editor resp. ConExp's context editor. The code is intricate, a bit
 * ugly and uses quite a few snippets from various sources from the internet (see below).
 * That is just because of the way JTable is designed - it is not meant to be too flexible.
 *
 * PRECONDITION: Expects a model that extends 'AbstractTableModel' and implements 'Reordarable'! TODO: Overwrite setTableModel to assert for that
 *
 * Resources:
 * http://explodingpixels.wordpress.com/2009/05/18/creating-a-better-jtable/
 * http://stackoverflow.com/questions/14416188/jtable-how-to-get-selected-cells
 * http://stackoverflow.com/questions/5044222/how-can-i-determine-which-cell-in-a-jtable-was-selected?rq=1
 * http://tonyobryan.com/index.php?article=57
 * http://www.jroller.com/santhosh/entry/make_jtable_resiable_better_than
*/
class ContextMatrix extends JTable {

    private static final long serialVersionUID = -7474568014425724962L;

    private static final Color HEADER_COLOR = new Color(245, 245, 250);
    private static final Color EVEN_ROW_COLOR = new Color(252, 252, 252);
    private static final Color ODD_ROW_COLOR = new Color(255, 255, 255);
    private static final Color TABLE_GRID_COLOR = new Color(0xd9d9d9);

    // Needed as otherwise there is a weird white area below the editor
    // We just paint the editor background in the background color of the containing element
    Color BACKGROUND_COLOR = Color.LIGHT_GRAY;
    // For enabling renaming of objects/attributes
    TableCellEditor editor;
    // For preventing a selection to disappear after an operation like "invert"
    public int lastSelectedRowsStartIndex;
    public int lastSelectedRowsEndIndex;
    public int lastSelectedColumnsStartIndex;
    public int lastSelectedColumnsEndIndex;

    public ContextMatrix(TableModel dm, Color bg, Map<Integer, Integer> columnWidths) {
        super(dm);
        BACKGROUND_COLOR = bg;
        this.columnWidths = columnWidths;
        setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        setTableHeader(null);
        setOpaque(false);
        setGridColor(TABLE_GRID_COLOR);
        setIntercellSpacing(new Dimension(0, 0));
        setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        setCellSelectionEnabled(true);
        setShowGrid(false);
        clearKeyBindings();
        // Create custom TableCellEditor
        editor = new ContextCellEditor(new JTextField());
        // For column resizing
        addMouseListener(columnResizeMouseAdapter);
        addMouseMotionListener(columnResizeMouseAdapter);
        createDraggingActions();
    }

    private void clearKeyBindings() {
        // After testings thoroughly it seems to be impossible to simply clear all keybindings
        // even if Swing's API suggests that it should be possible. So we need to rely on a hack
        // for removing keybindings
        int[] is = {JComponent.WHEN_IN_FOCUSED_WINDOW, JComponent.WHEN_FOCUSED, JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT};
        InputMap im = getInputMap();
        for (int i = 0; i <= is.length; i++) {
            im.put(getKeyStroke(KeyEvent.VK_ENTER, 0), "none");
            im.put(getKeyStroke(KeyEvent.VK_UP, 0), "none");
            im.put(getKeyStroke(KeyEvent.VK_LEFT, 0), "none");
            im.put(getKeyStroke(KeyEvent.VK_RIGHT, 0), "none");
            im.put(getKeyStroke(KeyEvent.VK_DOWN, 0), "none");
            im.put(getKeyStroke(KeyEvent.VK_UP, KeyEvent.SHIFT_MASK), "none");
            im.put(getKeyStroke(KeyEvent.VK_LEFT, KeyEvent.SHIFT_MASK), "none");
            im.put(getKeyStroke(KeyEvent.VK_RIGHT, KeyEvent.SHIFT_MASK), "none");
            im.put(getKeyStroke(KeyEvent.VK_DOWN, KeyEvent.SHIFT_MASK), "none");
            if (i == is.length) break;
            //noinspection MagicConstant
            im = getInputMap(is[i]);
        }
    }

    // For centering text inside cells
    private void alignCells() {
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        for (int i = 0; i < getColumnCount(); i++) {
            getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }
    }

    // For renaming of objects/attributes
    private void makeHeaderCellsEditable() {
        for (int i = 0; i < getColumnCount(); i++) {
            getColumnModel().getColumn(i).setCellEditor(editor);
        }
    }

    // For allowing a programmatical cell selection (i.e. not only through mouse/keyboard events)
    public void selectCell(int row, int column) {
        setRowSelectionInterval(row, row);
        setColumnSelectionInterval(column, column);
    }

    public void selectRow(int row) {
        setRowSelectionInterval(row, row);
        setColumnSelectionInterval(1, this.getColumnCount() - 1);
    }

    public void selectColumn(int column) {
        setColumnSelectionInterval(column, column);
        setRowSelectionInterval(1, this.getRowCount() - 1);
    }

    // For preventing a selection to disappear after an operation like "invert"
    public void saveSelectedInterval() {
        lastSelectedRowsStartIndex = getSelectedRow();
        lastSelectedRowsEndIndex = getSelectedRowCount()-1 + lastSelectedRowsStartIndex;
        lastSelectedColumnsStartIndex = getSelectedColumn();
        lastSelectedColumnsEndIndex = getSelectedColumnCount()-1 + lastSelectedColumnsStartIndex;
    }

    // For preventing a selection to disappear after an operation like "invert"
    public void restoreSelectedInterval() {
        if (getRowCount() <= 1 || getColumnCount() <= 1) return;
        if (  (lastSelectedColumnsEndIndex == 0 && lastSelectedColumnsStartIndex == 0)
           || (lastSelectedRowsEndIndex    == 0 && lastSelectedRowsStartIndex    == 0)) return;
        lastSelectedRowsStartIndex = clamp(lastSelectedRowsStartIndex, 1, getRowCount()-1);
        lastSelectedRowsEndIndex = clamp(lastSelectedRowsEndIndex, 1, getRowCount()-1);
        lastSelectedColumnsStartIndex = clamp(lastSelectedColumnsStartIndex, 1, getColumnCount() - 1);
        lastSelectedColumnsEndIndex = clamp(lastSelectedColumnsEndIndex, 1, getColumnCount()-1);
        setRowSelectionInterval(lastSelectedRowsStartIndex, lastSelectedRowsEndIndex);
        setColumnSelectionInterval(lastSelectedColumnsStartIndex, lastSelectedColumnsEndIndex);
    }

    // Overridden as header cells should *not* be selected when selecting all cells
    @Override
    public void selectAll() {
        setRowSelectionInterval(1, getRowCount()-1);
        setColumnSelectionInterval(1, getColumnCount()-1);
    }

    // Overridden as header cells should *not* be selectable through mouse clicks / keyboard events
    @Override
    public boolean isCellSelected(int i, int j) {
        return i != 0 && j != 0 && super.isCellSelected(i, j);
    }

    // For correct rendering of table after data changes
    @Override
    public void tableChanged(TableModelEvent e) {
        super.tableChanged(e);
        alignCells();
        makeHeaderCellsEditable();
        restoreColumnWidths();
    }

    // For correct painting of table when selecting something
    @Override
    public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
        Component component = super.prepareRenderer(renderer, row, column);
        if (component instanceof JComponent) {
            ((JComponent)component).setOpaque(isCellSelected(row, column));
        }
        return component;
    }

    // Create our custom viewport into which our custom JTable will be inserted
    public static JScrollPane createStripedJScrollPane(JTable table, Color bg) {
        JScrollPane scrollPane =  new JScrollPane(table);
        scrollPane.setViewport(new StripedViewport(table, bg));
        scrollPane.getViewport().setView(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        return scrollPane;
    }

    // Custom cell editor. Needed for renaming of objects/attributes
    @SuppressWarnings("serial")
	public static class ContextCellEditor extends DefaultCellEditor {

        int lastRow = 0;
        int lastColumn = 0;
        String lastName;
        ContextMatrixModel model = null;
        JTextField textField = null;

        public ContextCellEditor(JTextField textField) {
            super(textField);
            this.textField = textField;
        }

        @Override
        public Component getTableCellEditorComponent(JTable table,
                Object value, boolean isSelected, int row, int column) {
            JTextField f = (JTextField) super.getTableCellEditorComponent(table, value, isSelected, row, column);
            model = (ContextMatrixModel) table.getModel();
            String text;
            if (column == 0) {
                text = model.getObjectNameAt(row-1);
            } else {
                text = model.getAttributeNameAt(column-1);
            }
            f.setText(text);
            lastName = text;
            lastColumn = column;
            lastRow = row;
            this.textField = f;
            return f;
        }

        @Override
        public Object getCellEditorValue() {
            String newName = super.getCellEditorValue().toString();
            if (lastColumn == 0) {
                boolean success = model.renameObject(lastName, newName);
                if (!success) {
                    // TODO: Show dialog that says name already taken
                }
            } else {
                boolean success = model.renameAttribute(lastName, newName);
                if (!success) {
                    // TODO: Show dialog that says name already taken
                }
            }
            return super.getCellEditorValue();
        }

        public JTextField getTextField() {
            return textField;
        }

    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Dragging
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    // For dragging logic
    boolean isDraggingRow = false;
    boolean isDraggingColumn = false;
    int lastDraggedRowIndex;
    int lastDraggedColumnIndex;

    private void createDraggingActions() {
        MouseAdapter mouseAdapter = new MouseAdapter() {

            public void mousePressed(MouseEvent e) {
                int i = rowAtPoint(e.getPoint());
                int j = columnAtPoint(e.getPoint());
                lastDraggedRowIndex = i;
                lastDraggedColumnIndex = j;
                // A hacky way to check if the user is currently resizing a column
                if (getCursor() != ContextMatrix.resizeCursor) {
                    if (SwingUtilities.isLeftMouseButton(e) && j == 0 && i > 0) {
                        isDraggingRow = true;
                        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                    }
                    if (SwingUtilities.isLeftMouseButton(e) && i == 0 && j > 0) {
                        isDraggingColumn = true;
                        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                    }
                }
            }

            public void mouseDragged(MouseEvent e) {
                Reorderable model = (Reorderable) getModel();
                int i = rowAtPoint(e.getPoint());
                int j = columnAtPoint(e.getPoint());
                if (i < 0 || j < 0) return;
                if (isDraggingRow) selectRow(Math.max(i, 1));
                if (isDraggingColumn) selectColumn(Math.max(lastDraggedColumnIndex, 1));
                // A reorder of rows occured
                if (isDraggingRow && i != lastDraggedRowIndex && i != 0) {
                    model.reorderRows(lastDraggedRowIndex, i);
                    ((AbstractTableModel)getModel()).fireTableDataChanged();
                    selectRow(i);
                    lastDraggedRowIndex = i;
                }
                // A reorder of columns occured
                if (isDraggingColumn && j != lastDraggedColumnIndex && j != 0) {
                    // to prevent a bug when reordering columns of different widths
                    Rectangle selected = getCellRect(0, lastDraggedColumnIndex, false);
                    Rectangle r = getCellRect(0, j, false);
                    Point p = r.getLocation();
                    if ((j <= lastDraggedColumnIndex || e.getX() > p.x + r.width - selected.width) &&
                            (j >  lastDraggedColumnIndex || e.getX() < p.x + selected.width)) {
                        model.reorderColumns(lastDraggedColumnIndex, j);
                        switchColumnWidths(lastDraggedColumnIndex, j);
                        ((AbstractTableModel) model).fireTableDataChanged();
                        selectColumn(j);
                        lastDraggedColumnIndex = j;
                    }
                }
            }

            public void mouseReleased(MouseEvent e) {
                // TODO: this should to select code
                // For selecting entire row/column when clicking on a header
                int i = rowAtPoint(e.getPoint());
                int j = columnAtPoint(e.getPoint());
                if (getCursor() != ContextMatrix.resizeCursor) {
                    if (SwingUtilities.isLeftMouseButton(e) && j == 0 && i > 0) {
                        selectRow(i);
                    }
                    if (SwingUtilities.isLeftMouseButton(e) && i == 0 && j > 0) {
                        selectColumn(j);
                    }
                }

                isDraggingRow = false;
                isDraggingColumn = false;
                if (getCursor() != ContextMatrix.resizeCursor) {
                    setCursor(Cursor.getDefaultCursor());
                }
            }
        };
        addMouseListener(mouseAdapter);
        addMouseMotionListener(mouseAdapter);
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Resizing
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    // Beware! Unelegant code ahead!
    public static final int DEFAULT_COLUMN_WIDTH = 80;
    public static final int COMPACTED_COLUMN_WIDTH = 15;
    public static Cursor resizeCursor = Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR);
    public Map<Integer,Integer> columnWidths;
    public Map<Integer,Integer> compactedColumnWidths = new HashMap<>();
    private int mouseXOffset;
    private Cursor otherCursor = resizeCursor;
    private TableColumn resizingColumn;
    private boolean isCompacted = false;

    public void compact() {
        isCompacted = true;
        for (int i = 1; i < getColumnModel().getColumnCount(); i++) {
            getColumnModel().getColumn(i).setPreferredWidth(COMPACTED_COLUMN_WIDTH);
        }
        compactedColumnWidths.clear();
    }

    public void uncompact() {
        isCompacted = false;
        restoreColumnWidths();
    }

    public void restoreColumnWidths() {
        if (columnWidths == null) return;
        if (!isCompacted) {
            for (int i = 0; i < getColumnCount(); i++) {
                Integer w = columnWidths.get(i);
                TableColumn t = getColumnModel().getColumn(i);
                if (w == null) {
                    t.setPreferredWidth(DEFAULT_COLUMN_WIDTH);
                } else {
                    t.setPreferredWidth(w);
                }
            }
        } else {
            for (int i = 1; i < getColumnCount(); i++) {
                Integer w = compactedColumnWidths.get(i);
                TableColumn t = getColumnModel().getColumn(i);
                if (w == null) {
                    t.setPreferredWidth(COMPACTED_COLUMN_WIDTH);
                } else {
                    t.setPreferredWidth(w);
                }
            }
        }
    }

    public void updateColumnWidths(int removedIndex) {
        if (!isCompacted) {
            columnWidths.remove(removedIndex);
            for (int i = removedIndex + 1; i < getColumnCount(); i++) {
                Integer oldWidth = columnWidths.remove(i);
                if (oldWidth == null) continue;
                if (i == 1) continue;
                columnWidths.put(i-1, oldWidth);
            }
        } else {
            compactedColumnWidths.remove(removedIndex);
            for (int i = removedIndex + 1; i < getColumnCount(); i++) {
                Integer oldWidth = compactedColumnWidths.remove(i);
                if (oldWidth == null) continue;
                if (i == 1) continue;
                compactedColumnWidths.put(i-1, oldWidth);
            }
        }
    }

    public void switchColumnWidths(int from, int to) {
        Integer fromVal, toVal;

        fromVal = columnWidths.get(from);
        toVal = columnWidths.get(to);
        if (fromVal == null) fromVal = ContextMatrix.DEFAULT_COLUMN_WIDTH;
        if (toVal == null) toVal = ContextMatrix.DEFAULT_COLUMN_WIDTH;
        columnWidths.put(to, fromVal);
        columnWidths.put(from, toVal);

        fromVal = compactedColumnWidths.get(from);
        toVal = compactedColumnWidths.get(to);
        if (fromVal == null) fromVal = ContextMatrix.COMPACTED_COLUMN_WIDTH;
        if (toVal == null) toVal = ContextMatrix.COMPACTED_COLUMN_WIDTH;
        compactedColumnWidths.put(to, fromVal);
        compactedColumnWidths.put(from, toVal);
    }

    @SuppressWarnings("FieldCanBeLocal")
    private MouseAdapter columnResizeMouseAdapter = new MouseAdapter() {

        public void mousePressed(MouseEvent e){
            ContextMatrix matrix = ContextMatrix.this;
            Point p = e.getPoint();
            // First find which header cell was hit
            int index = matrix.columnAtPoint(p);
            if(index == -1) return;
            // The last 3 pixels + 3 pixels of next column are for resizing
            TableColumn resizingColumn = getResizingColumn(p, index);
            if(resizingColumn == null) return;
            matrix.resizingColumn = resizingColumn;
            mouseXOffset = p.x - resizingColumn.getWidth();
            matrix.restoreSelectedInterval();
        }

        public void mouseMoved(MouseEvent e){
            ContextMatrix matrix = ContextMatrix.this;
            if((getResizingColumn(e.getPoint()) == null) == (matrix.getCursor() == resizeCursor)){
                swapCursor();
            }
        }

        public void mouseDragged(MouseEvent e){
            ContextMatrix matrix = ContextMatrix.this;
            int mouseX = e.getX();
            TableColumn resizingColumn = matrix.resizingColumn;
            if(resizingColumn != null){
                matrix.restoreSelectedInterval();
                int oldWidth = resizingColumn.getWidth();
                int newWidth = Math.max(mouseX - mouseXOffset, 20);
                resizingColumn.setWidth(newWidth);
                resizingColumn.setPreferredWidth(newWidth);
                if (!isCompacted) {
                    columnWidths.put(resizingColumn.getModelIndex(), newWidth);
                } else {
                    compactedColumnWidths.put(resizingColumn.getModelIndex(), newWidth);
                }

                Container container;
                if((matrix.getParent() == null)
                        || ((container = matrix.getParent().getParent()) == null)
                        || !(container instanceof JScrollPane)){
                    return;
                }

                JViewport viewport = ((JScrollPane)container).getViewport();
                int viewportWidth = viewport.getWidth();
                int diff = newWidth - oldWidth;
                int newHeaderWidth = matrix.getWidth() + diff;

                // Resize a table
                Dimension tableSize = matrix.getSize();
                tableSize.width += diff;
                matrix.setSize(tableSize);

                // If this table is in AUTO_RESIZE_OFF mode and has a horizontal
                // scrollbar, we need to update a view's position.
                if((newHeaderWidth >= viewportWidth)
                        && (matrix.getAutoResizeMode() == JTable.AUTO_RESIZE_OFF)){
                    Point p = viewport.getViewPosition();
                    p.x =
                            Math.max(0, Math.min(newHeaderWidth - viewportWidth, p.x + diff));
                    viewport.setViewPosition(p);

                    // Update the original X offset value.
                    mouseXOffset += diff;
                }
            }
        }

        public void mouseReleased(MouseEvent e){
            ContextMatrix matrix = ContextMatrix.this;
            matrix.resizingColumn = null;
            matrix.saveSelectedInterval();
        }

        private void swapCursor(){
            Cursor tmp = getCursor();
            setCursor(otherCursor);
            otherCursor = tmp;
        }

        private TableColumn getResizingColumn(Point p) {
            return getResizingColumn(p, columnAtPoint(p));
        }

        private TableColumn getResizingColumn(Point p, int column) {
            if (column == -1) return null;
            int row = rowAtPoint(p);
            if (row != 0) return null;
            Rectangle r = getCellRect(row, column, true);
            r.grow(-3, 0);
            if (r.contains(p)) return null;
            int midPoint = r.x + r.width / 2;
            int columnIndex = (p.x < midPoint) ? column - 1 : column;
            if(columnIndex == -1) return null;
            return getColumnModel().getColumn(columnIndex);
        }

    };


    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Drawing
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private static class StripedViewport extends JViewport {

        private static final long serialVersionUID = 171992496170114834L;

        // Needed as otherwise there is a weird white area below the editor
        // We just paint the editor background in the background color of the containing element
        private final Color BACKGROUND_COLOR;
        private final JTable fTable;

        public StripedViewport(JTable table, Color bg) {
            BACKGROUND_COLOR = bg;
            fTable = table;
            setBackground(BACKGROUND_COLOR);
            setOpaque(false);
            initListeners();
        }

        private void initListeners() {
            PropertyChangeListener listener = createTableColumnWidthListener();
            for (int i=0; i<fTable.getColumnModel().getColumnCount(); i++) {
                fTable.getColumnModel().getColumn(i).addPropertyChangeListener(listener);
            }
        }

        private PropertyChangeListener createTableColumnWidthListener() {
            return new PropertyChangeListener() {
                public void propertyChange(PropertyChangeEvent evt) {
                    repaint();
                }
            };
        }

        @Override
        public void setViewPosition(Point p) {
            super.setViewPosition(p);
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            paintBackground(g);
            paintStripedBackground(g);
            paintVerticalHeaderBackground(g);
            paintHorizontalHeaderBackground(g);
            paintGridLines(g);
            super.paintComponent(g);
        }

        private void paintBackground(Graphics g) {
            g.setColor(BACKGROUND_COLOR);
            g.fillRect(g.getClipBounds().x, g.getClipBounds().y,
                       g.getClipBounds().width, g.getClipBounds().height);
        }

        private void paintStripedBackground(Graphics g) {
            int rowHeight = fTable.getRowHeight();
            int tableWidth = fTable.getWidth();
            int offsetX = getViewPosition().x;
            int offsetY = getViewPosition().y;
            int x = -offsetX;
            int y = -offsetY;
            for (int j = 0; j < fTable.getRowCount(); j++) {
                g.setColor(j % 2 == 0 ? EVEN_ROW_COLOR : ODD_ROW_COLOR);
                g.fillRect(x, y + j * rowHeight, tableWidth, rowHeight);
            }
        }

        private void paintVerticalHeaderBackground(Graphics g) {
            int tableHeight = fTable.getHeight();
            int firstColumnWidth = fTable.getColumnModel().getColumn(0).getWidth();
            int rowHeight = fTable.getRowHeight();
            int offsetX = getViewPosition().x;
            int offsetY = getViewPosition().y;
            int x = -offsetX;
            int y = -offsetY;
            g.setColor(HEADER_COLOR);
            g.fillRect(x, y, firstColumnWidth, tableHeight);

            g.setColor(new Color(255,255,255));
            g.drawLine(x + firstColumnWidth - 2, y + rowHeight, x + firstColumnWidth - 2, y + tableHeight);
            g.setColor(new Color(235,235,235));
            g.drawLine(x + firstColumnWidth, y + rowHeight, x + firstColumnWidth, y + tableHeight);
            g.setColor(new Color(255, 255, 255));
            for (int j = 0; j < fTable.getRowCount() + 1; j++) {
                g.drawLine(x, y + j * rowHeight - 1, x + firstColumnWidth - 1, y + j * rowHeight - 1);
            }
        }

        private void paintHorizontalHeaderBackground(Graphics g) {
            int tableWidth = fTable.getWidth();
            int firstRowHeight = fTable.getRowHeight();
            int firstColumnWidth = fTable.getColumnModel().getColumn(0).getWidth();
            int offsetX = getViewPosition().x;
            int offsetY = getViewPosition().y;
            int x = -offsetX;
            int y = -offsetY;
            g.setColor(HEADER_COLOR);
            g.fillRect(x, y, tableWidth, firstRowHeight);

            g.setColor(new Color(255, 255, 255));
            g.drawLine(x + firstColumnWidth, y + firstRowHeight - 1, x + tableWidth, y + firstRowHeight - 1);
            g.setColor(new Color(235, 235, 235));
            g.drawLine(x + firstColumnWidth, y + firstRowHeight + 1, x + tableWidth, y + firstRowHeight + 1);
            g.setColor(new Color(255, 255, 255));
            int columnWidth = 0;
            for (int j = 1; j < fTable.getColumnCount() + 1; j++) {
                columnWidth += fTable.getColumnModel().getColumn(j-1).getWidth();
                g.drawLine(x + columnWidth - 2, y, x + columnWidth - 2, y + firstRowHeight - 1);
            }
        }

        private void paintGridLines(Graphics g) {
            int tableHeight = fTable.getHeight();
            int rowHeight = fTable.getRowHeight();
            int offsetX = getViewPosition().x;
            int offsetY = getViewPosition().y;
            int x = -offsetX;
            int y = -offsetY;
            g.setColor(TABLE_GRID_COLOR);
            for (int i = 0; i < fTable.getColumnCount(); i++) {
                TableColumn column = fTable.getColumnModel().getColumn(i);
                x += column.getWidth();
                g.drawLine(x - 1, y, x - 1, y + tableHeight);
            }
            for (int j = 1; j < fTable.getRowCount() + 1; j++) {
                g.drawLine(-offsetX, y + j * rowHeight, x - 1, y + j * rowHeight);
            }
        }

    }

}