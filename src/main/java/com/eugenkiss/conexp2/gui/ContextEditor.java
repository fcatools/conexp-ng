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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import static com.eugenkiss.conexp2.gui.Util.createButton;

/**
 * The class responsible for displaying and interacting with ConExpNG's context editor.
 * The main component of this view is a customised JTable, that is more akin to a spreadsheet
 * editor, serving as our context editor. To this end, there are several additional classes
 * in this file.
 */
public class ContextEditor extends View {

    private static final long serialVersionUID = 1660117627650529212L;

    // Our JTable customisation and its respective data model
    private final ContextMatrix matrix;
    private final ContextMatrixModel matrixModel;

    // For remembering which header cell has been right-clicked
    // Due to unfortunate implications of our JTable customisation we need to rely on this "hack"
    int lastClickedRow;
    int lastClickedColumn;

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

        // Add actions
        createButtonActions();
        createContextMenuActions();

        // Force an update of the table to display it correctly
        matrixModel.fireTableStructureChanged();
    }

    private void createButtonActions() {
        // Add buttons
        JButton addObjectButton = createButton("Add Object", "addObject", "conexp/addObj.gif");
        toolbar.add(addObjectButton);
        JButton clarifyObjectsButton = createButton("Clarify Objects", "clarifyObjects", "conexp/clarifyObj.gif");
        toolbar.add(clarifyObjectsButton);
        JButton reduceObjectsButton = createButton("Reduce Objects", "reduceObjects", "conexp/reduceObj.gif");
        toolbar.add(reduceObjectsButton);
        toolbar.addSeparator();
        JButton addAttributeButton = createButton("Add Attribute", "addAttribute", "conexp/addAttr.gif");
        toolbar.add(addAttributeButton);
        JButton clarifyAttributesButton = createButton("Clarify Attributes", "clarifyAttributes", "conexp/clarifyAttr.gif");
        toolbar.add(clarifyAttributesButton);
        JButton reduceAttributesButton = createButton("Reduce Attributes", "reduceAttributes", "conexp/reduceAttr.gif");
        toolbar.add(reduceAttributesButton);
        toolbar.addSeparator();
        JButton reduceContextButton = createButton("Reduce Context", "reduceContext", "conexp/reduceCxt.gif");
        toolbar.add(reduceContextButton);
        JButton transposeContextButton = createButton("Transpose Context", "transposeContext", "conexp/transpose.gif");
        toolbar.add(transposeContextButton);

        // Add actions
        addObjectButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                addObjectAt(state.context.getObjectCount());
            }
        });
        addAttributeButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                addAttributeAt(state.context.getAttributeCount());
            }
        });
        transposeContextButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                state.context.transpose();
                matrixModel.fireTableDataChanged();
            }
        });
    }

    private void createContextMenuActions() {
        final JPopupMenu cellPopupMenu = new JPopupMenu();
        final JPopupMenu objectCellPopupMenu = new JPopupMenu();
        final JPopupMenu attributeCellPopupMenu = new JPopupMenu();

        // ------------------------
        // Inner cells context menu
        // ------------------------
        addMenuItem(cellPopupMenu, "Cut", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                // TODO
            }
        });
        addMenuItem(cellPopupMenu, "Copy", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                // TODO
            }
        });
        addMenuItem(cellPopupMenu, "Paste", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                // TODO
            }
        });
        addMenuItem(cellPopupMenu, "Select all", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                matrix.selectAll();
            }
        });
        //--------
        cellPopupMenu.add(new JPopupMenu.Separator());
        //--------
        addMenuItem(cellPopupMenu, "Fill", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                int i1 = matrix.getSelectedRow() - 1;
                int i2 = i1 + matrix.getSelectedRowCount();
                int j1 = matrix.getSelectedColumn() - 1;
                int j2 = j1 + matrix.getSelectedColumnCount();
                matrix.saveSelectedInterval();
                state.context.fill(i1, i2, j1, j2);
                matrixModel.fireTableDataChanged();
                matrix.restoreSelectedInterval();
            }
        });
        addMenuItem(cellPopupMenu, "Clear", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                int i1 = matrix.getSelectedRow() - 1;
                int i2 = i1 + matrix.getSelectedRowCount();
                int j1 = matrix.getSelectedColumn() - 1;
                int j2 = j1 + matrix.getSelectedColumnCount();
                matrix.saveSelectedInterval();
                state.context.clear(i1, i2, j1, j2);
                matrixModel.fireTableDataChanged();
                matrix.restoreSelectedInterval();
            }
        });
        addMenuItem(cellPopupMenu, "Invert", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                int i1 = matrix.getSelectedRow() - 1;
                int i2 = i1 + matrix.getSelectedRowCount();
                int j1 = matrix.getSelectedColumn() - 1;
                int j2 = j1 + matrix.getSelectedColumnCount();
                matrix.saveSelectedInterval();
                state.context.invert(i1, i2, j1, j2);
                matrixModel.fireTableDataChanged();
                matrix.restoreSelectedInterval();
            }
        });
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
                renameObject(lastClickedRow - 1);
            }
        });
        addMenuItem(objectCellPopupMenu, "Remove", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    state.context.removeObject(state.context.getObjectAtIndex(lastClickedRow-1).getIdentifier());
                } catch (IllegalObjectException e1) {
                    e1.printStackTrace();
                }
                matrixModel.fireTableStructureChanged();
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        matrix.updateUI();
                    }
                });
            }
        });
        addMenuItem(objectCellPopupMenu, "Add above", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                addObjectAt(lastClickedRow - 1);
            }
        });
        addMenuItem(objectCellPopupMenu, "Add below", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                addObjectAt(lastClickedRow);
            }
        });

        // ---------------------------
        // Attribute cell context menu
        // ---------------------------
        addMenuItem(attributeCellPopupMenu, "Rename", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                renameAttribute(lastClickedColumn - 1);
            }
        });
        addMenuItem(attributeCellPopupMenu, "Remove", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                state.context.removeAttribute(state.context.getAttributeAtIndex(lastClickedColumn-1));
                matrixModel.fireTableStructureChanged();
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        matrix.updateUI();
                    }
                });
            }
        });
        addMenuItem(attributeCellPopupMenu, "Add left", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                addAttributeAt(lastClickedColumn - 1);
            }
        });
        addMenuItem(attributeCellPopupMenu, "Add right", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                addAttributeAt(lastClickedColumn);
            }
        });

        // ========================
        // Add right-click behavior
        // ========================
        matrix.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                int i = matrix.getSelectedRow();
                int j = matrix.getSelectedColumn();
                int clicks = e.getClickCount();
                if (clicks >= 2 && clicks % 2 == 0 && !SwingUtilities.isRightMouseButton(e)) { // Double Click
                    if (i > 0 && j > 0) {
                        state.context.toggleAttributeForObject(
                                state.context.getAttributeAtIndex(j - 1),
                                state.context.getObjectAtIndex(i - 1).getIdentifier());
                        matrix.saveSelectedInterval();
                        matrixModel.fireTableDataChanged();
                        matrix.restoreSelectedInterval();
                    }
                }
            }

            public void mousePressed(MouseEvent e) {
                int i = matrix.rowAtPoint(e.getPoint());
                int j = matrix.columnAtPoint(e.getPoint());
                lastClickedRow = i;
                lastClickedColumn = j;
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

    private static void addMenuItem(JPopupMenu menu, String name, ActionListener action) {
        JMenuItem item = new JMenuItem(name);
        menu.add(item);
        item.addActionListener(action);
    }

    private void addAttributeAt(int i) {
        String newAttribute = "attr" + i;
        state.context.addAttributeAt(newAttribute, i);
        matrixModel.fireTableStructureChanged();
        renameAttribute(i);
    }

    private void addObjectAt(int i) {
        FullObject<String,String> newObject = new FullObject<String, String>("obj" + i);
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

        // Create custom TableCellEditor
        editor = new ContextCellEditor(new JTextField());
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
            for (int j = 0; j < fTable.getRowCount(); j++) {
                g.setColor(j % 2 == 0 ? EVEN_ROW_COLOR : ODD_ROW_COLOR);
                g.fillRect(0, j * rowHeight, tableWidth, rowHeight);
            }
        }

        private void paintVerticalHeaderBackground(Graphics g) {
            int tableHeight = fTable.getHeight();
            int firstColumnWidth = fTable.getColumnModel().getColumn(0).getWidth();
            int rowHeight = fTable.getRowHeight();
            g.setColor(HEADER_COLOR);
            g.fillRect(0, 0, firstColumnWidth, tableHeight);

            g.setColor(new Color(255,255,255));
            g.drawLine(firstColumnWidth - 2, rowHeight, firstColumnWidth - 2, tableHeight);
            g.setColor(new Color(235,235,235));
            g.drawLine(firstColumnWidth, rowHeight, firstColumnWidth, tableHeight);
            for (int j = 0; j < fTable.getRowCount() + 1; j++) {
                g.setColor(new Color(255,255,255));
                g.drawLine(0, j * rowHeight - 1, firstColumnWidth - 1, j * rowHeight - 1);
            }
        }

        private void paintHorizontalHeaderBackground(Graphics g) {
            int tableWidth = fTable.getWidth();
            int firstRowHeight = fTable.getRowHeight();
            int columnWidth = fTable.getColumnModel().getColumn(0).getWidth();
            g.setColor(HEADER_COLOR);
            g.fillRect(0, 0, tableWidth, firstRowHeight);

            g.setColor(new Color(255, 255, 255));
            g.drawLine(columnWidth, firstRowHeight - 1, tableWidth, firstRowHeight - 1);
            g.setColor(new Color(235, 235, 235));
            g.drawLine(columnWidth, firstRowHeight+1, tableWidth, firstRowHeight+1);
            for (int j = 1; j < fTable.getColumnCount() + 1; j++) {
                g.setColor(new Color(255,255,255));
                g.drawLine(j * columnWidth - 2, 0, j * columnWidth - 2, firstRowHeight - 1);
            }
        }

        private void paintGridLines(Graphics g) {
            int tableWidth = fTable.getWidth();
            int tableHeight = fTable.getHeight();
            int rowHeight = fTable.getRowHeight();
            g.setColor(TABLE_GRID_COLOR);
            int x = 0;
            for (int i = 0; i < fTable.getColumnCount(); i++) {
                TableColumn column = fTable.getColumnModel().getColumn(i);
                x += column.getWidth();
                g.drawLine(x - 1, 0, x - 1, tableHeight);
            }
            for (int j = 1; j < fTable.getRowCount() + 1; j++) {
                g.drawLine(0, j * rowHeight, tableWidth - 1, j * rowHeight);
            }
        }

    }

}