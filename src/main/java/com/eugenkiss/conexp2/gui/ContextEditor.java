package com.eugenkiss.conexp2.gui;

import com.eugenkiss.conexp2.ProgramState;
import com.eugenkiss.conexp2.model.FormalContext;
import de.tudresden.inf.tcs.fcaapi.exception.IllegalObjectException;
import de.tudresden.inf.tcs.fcalib.FullObject;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.event.TableModelEvent;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import static com.eugenkiss.conexp2.gui.Util.*;
import static javax.swing.KeyStroke.getKeyStroke;

/**
 * The class responsible for displaying and interacting with ConExpNG's context editor.
 * The main component of this view is a customised JTable, that is more akin to a spreadsheet
 * editor, serving as our context editor. To this end, there are several additional classes
 * in this file.
 */
public class ContextEditor extends View {

    private static final long serialVersionUID = 1660117627650529212L;

    // Choose correct modifier key (STRG or CMD) based on platform
    @SuppressWarnings("UnusedDeclaration")
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
        matrixModel = new ContextMatrixModel(state.context);
        matrix = new ContextMatrix(matrixModel, panel.getBackground());
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
        matrix.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                int i = matrix.rowAtPoint(e.getPoint());
                int j = matrix.columnAtPoint(e.getPoint());
                int clicks = e.getClickCount();
                if (clicks >= 2 && clicks % 2 == 0 && !SwingUtilities.isRightMouseButton(e)) { // Double Click
                    if (i > 0 && j > 0) {
                        invokeAction(new ToggleAction(i, j));
                    }
                }
            }

            public void mousePressed(MouseEvent e) {
                int i = matrix.rowAtPoint(e.getPoint());
                int j = matrix.columnAtPoint(e.getPoint());
                lastActiveRowIndex = i;
                lastActiveColumnIndex = j;
                if (e.isPopupTrigger()) {
                    if (i == 0 && j == 0) {
                        // Don't show a context menu in the matrix corner
                    } else if (i > 0 && j > 0) {
                        if (matrix.getSelectedColumn() <= 0 || matrix.getSelectedRow() <= 0) {
                            matrix.setSelectedCell(i, j);
                        }
                        cellPopupMenu.show(e.getComponent(), e.getX(), e.getY());
                    } else if (j == 0) {
                        objectCellPopupMenu.show(e.getComponent(), e.getX(), e.getY());
                    } else {
                        attributeCellPopupMenu.show(e.getComponent(), e.getX(), e.getY());
                    }
                }
            }

            public void mouseReleased(MouseEvent e) {
                mousePressed(e);
            }
        });
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
    }

    private void createContextMenuActions() {
        // ------------------------
        // Inner cells context menu
        // ------------------------
        addMenuItem(cellPopupMenu, "Cut", new CutAction());
        addMenuItem(cellPopupMenu, "Copy", new CopyAction());
        addMenuItem(cellPopupMenu, "Paste", new PasteAction());
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
        addMenuItem(cellPopupMenu, "Remove attribute(s)", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                // TODO
            }
        });
        addMenuItem(cellPopupMenu, "Remove object(s)", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                // TODO
            }
        });

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
        addMenuItem(attributeCellPopupMenu, "Add horizontal", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                addAttributeAt(lastActiveColumnIndex - 1);
            }
        });
        addMenuItem(attributeCellPopupMenu, "Add vertical", new ActionListener() {
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
            matrix.setSelectedCell(lastActiveRowIndex, lastActiveColumnIndex);
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
            matrix.setSelectedCell(lastActiveRowIndex, lastActiveColumnIndex);
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

    class CopyAction extends AbstractAction {
        public void actionPerformed(ActionEvent e) {
            // TODO
        }
    }

    class CutAction extends AbstractAction {
        public void actionPerformed(ActionEvent e) {
            // TODO
        }
    }

    class PasteAction extends AbstractAction {
        public void actionPerformed(ActionEvent e) {
            // TODO
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
            state.contextChanged();
        }
    }

    class AddAttributeAtAction extends AbstractAction {
        int index;
        AddAttributeAtAction(int index) {
            this.index = index;
        }
        public void actionPerformed(ActionEvent e) {
            addAttributeAt(index);
        }
    }

    class AddObjectAtAction extends AbstractAction {
        int index;
        AddObjectAtAction(int index) {
            this.index = index;
        }
        public void actionPerformed(ActionEvent e) {
            addObjectAt(index);
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
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    matrixModel.fireTableStructureChanged();
                    matrix.updateUI();
                    matrix.restoreSelectedInterval();
                }
            });
        }
    }

    class RemoveActiveAttributeAction extends AbstractAction {
        public void actionPerformed(ActionEvent e) {
            if (state.context.getAttributeCount() == 0) return;
            matrix.saveSelectedInterval();
            state.context.removeAttribute(state.context.getAttributeAtIndex(lastActiveColumnIndex -1));
            if (lastActiveColumnIndex - 1 >= state.context.getAttributeCount()) lastActiveColumnIndex--;
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    matrixModel.fireTableStructureChanged();
                    matrix.updateUI();
                    matrix.restoreSelectedInterval();
                }
            });
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

    @SuppressWarnings("UnusedDeclaration")
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
class ContextMatrixModel extends AbstractTableModel {

    private static final long serialVersionUID = -1509387655329719071L;

    private final FormalContext context;

    ContextMatrixModel(FormalContext context) {
        this.context = context;
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
            return true;
        }
    }

    public boolean renameObject(String oldName, String newName) {
        if (context.existsObjectAlready(newName)) {
            return false;
        } else {
            context.renameObject(oldName, newName);
            return true;
        }
    }
}


////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

/**
 * ContextMatrix is simply a customisation of JTable in order to make it look & behave more
 * like a spreadsheet editor resp. ConExp's context editor. The code is intricate, a bit
 * ugly and uses quite a few snippets from various sources from the internet (see below).
 * That is just because of the way JTable is designed - it is not meant to be too flexible.
 *
 * Resources:
 * http://explodingpixels.wordpress.com/2009/05/18/creating-a-better-jtable/
 * http://stackoverflow.com/questions/14416188/jtable-how-to-get-selected-cells
 * http://stackoverflow.com/questions/5044222/how-can-i-determine-which-cell-in-a-jtable-was-selected?rq=1
 * http://tonyobryan.com/index.php?article=57
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
    int lastSelectedRowsStartIndex;
    int lastSelectedRowsEndIndex;
    int lastSelectedColumnsStartIndex;
    int lastSelectedColumnsEndIndex;

    public ContextMatrix(TableModel dm, Color bg) {
        super(dm);
        BACKGROUND_COLOR = bg;
        init();
    }

    private void init() {
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
    public void setSelectedCell(int row, int column) {
        setRowSelectionInterval(row, row);
        setColumnSelectionInterval(column, column);
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
        lastSelectedRowsStartIndex = clamp(lastSelectedRowsStartIndex, 1, getRowCount()-1);
        lastSelectedRowsEndIndex = clamp(lastSelectedRowsEndIndex, 1, getRowCount()-1);
        lastSelectedColumnsStartIndex = clamp(lastSelectedColumnsStartIndex, 1, getColumnCount()-1);
        lastSelectedColumnsEndIndex = clamp(lastSelectedColumnsEndIndex, 1, getColumnCount()-1);
        setRowSelectionInterval(lastSelectedRowsStartIndex, lastSelectedRowsEndIndex);
        setColumnSelectionInterval(lastSelectedColumnsStartIndex, lastSelectedColumnsEndIndex);
    }

    // Overrided as header cells should *not* be selected when selecting all cells
    @Override
    public void selectAll() {
        setRowSelectionInterval(1, getRowCount()-1);
        setColumnSelectionInterval(1, getColumnCount()-1);
    }

    // Overrided as header cells should *not* be selectable through mouse clicks / keyboard events
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
    }

    // For correct painting of table when selecting something
    @Override
    public Component prepareRenderer(TableCellRenderer renderer, int row,
                                     int column) {
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
    // Custom viewport that makes the table look nice
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
            for (int j = 0; j < fTable.getRowCount() + 1; j++) {
                g.setColor(new Color(255,255,255));
                g.drawLine(x, y + j * rowHeight - 1, x + firstColumnWidth - 1, y + j * rowHeight - 1);
            }
        }

        private void paintHorizontalHeaderBackground(Graphics g) {
            int tableWidth = fTable.getWidth();
            int firstRowHeight = fTable.getRowHeight();
            int columnWidth = fTable.getColumnModel().getColumn(0).getWidth();
            int offsetX = getViewPosition().x;
            int offsetY = getViewPosition().y;
            int x = -offsetX;
            int y = -offsetY;
            g.setColor(HEADER_COLOR);
            g.fillRect(x, y, tableWidth, firstRowHeight);

            g.setColor(new Color(255, 255, 255));
            g.drawLine(x + columnWidth, y + firstRowHeight - 1, x + tableWidth, y + firstRowHeight - 1);
            g.setColor(new Color(235, 235, 235));
            g.drawLine(x + columnWidth, y + firstRowHeight+1, x + tableWidth, y + firstRowHeight+1);
            for (int j = 1; j < fTable.getColumnCount() + 1; j++) {
                g.setColor(new Color(255,255,255));
                g.drawLine(x + j * columnWidth - 2, y, x + j * columnWidth - 2, y + firstRowHeight - 1);
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