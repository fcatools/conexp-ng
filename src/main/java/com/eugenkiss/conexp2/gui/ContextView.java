package com.eugenkiss.conexp2.gui;

import static com.eugenkiss.conexp2.gui.Util.createButton;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JViewport;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;

import com.eugenkiss.conexp2.ProgramState;

import de.tudresden.inf.tcs.fcalib.FormalContext;

public class ContextView extends View {

    private static final long serialVersionUID = 1660117627650529212L;

    private final ContextMatrix matrix;
    private final JButton addObjectButton;
    private final JButton clarifyObjectsButton;
    private final JButton reduceObjectsButton;
    private final JButton addAttributeButton;
    private final JButton clarifyAttributesButton;
    private final JButton reduceAttributesButton;
    private final JButton reduceContextButton;
    private final JButton transposeContextButton;

    public ContextView(final ProgramState state) {
        super(state);

        panel = new JPanel();
        panel.setLayout(new BorderLayout());

        matrix = new ContextMatrix(new ContextTableModel(state.context), panel.getBackground());
        JScrollPane scrollPane = ContextMatrix.createStripedJScrollPane(matrix, panel.getBackground());
        toolbar.setFloatable(false);
        toolbar.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, Color.LIGHT_GRAY));
        panel.add(toolbar, BorderLayout.WEST);
        panel.add(scrollPane, BorderLayout.CENTER);
        setLayout(new BorderLayout());
        add(panel);

        // Add buttons
        addObjectButton = createButton("Add Object", "addObject", "conexp/addObj.gif");
        toolbar.add(addObjectButton);
        clarifyObjectsButton = createButton("Clarify Objects", "clarifyObjects", "conexp/clarifyObj.gif");
        toolbar.add(clarifyObjectsButton);
        reduceObjectsButton = createButton("Reduce Objects", "reduceObjects", "conexp/reduceObj.gif");
        toolbar.add(reduceObjectsButton);
        toolbar.addSeparator();
        addAttributeButton = createButton("Add Attribute", "addAttribute", "conexp/addAttr.gif");
        toolbar.add(addAttributeButton);
        clarifyAttributesButton = createButton("Clarify Attributes", "clarifyAttributes", "conexp/clarifyAttr.gif");
        toolbar.add(clarifyAttributesButton);
        reduceAttributesButton = createButton("Reduce Attributes", "reduceAttributes", "conexp/reduceAttr.gif");
        toolbar.add(reduceAttributesButton);
        toolbar.addSeparator();
        reduceContextButton = createButton("Reduce Context", "reduceContext", "conexp/reduceCxt.gif");
        toolbar.add(reduceContextButton);
        transposeContextButton = createButton("Transpose Context", "transposeContext", "conexp/transpose.gif");
        toolbar.add(transposeContextButton);
    }

}

// inspired by http://explodingpixels.wordpress.com/2009/05/18/creating-a-better-jtable/
class ContextMatrix extends JTable {

    private static final long serialVersionUID = -7474568014425724962L;

    Color BACKGROUND_COLOR = Color.LIGHT_GRAY;
    private static final Color EVEN_ROW_COLOR = new Color(241, 245, 250);
    private static final Color ODD_ROW_COLOR = new Color(255, 255, 255);
    private static final Color TABLE_GRID_COLOR = new Color(0xd9d9d9);

    public ContextMatrix(TableModel dm, Color bg) {
        super(dm);
        init();
        BACKGROUND_COLOR = bg;
    }

    private void init() {
        setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        setTableHeader(null);
        setOpaque(false);
        setGridColor(TABLE_GRID_COLOR);
        setIntercellSpacing(new Dimension(0, 0));
        // turn off grid painting as we'll handle this manually in order to paint
        // grid lines over the entire viewport.
        setShowGrid(false);
        this.getModel().addTableModelListener(new TableModelListener() {
            @Override
            public void tableChanged(TableModelEvent e) {
                alignCells();
            }
        });
        alignCells();
    }

    private void alignCells() {
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        for (int i = 0; i < getColumnCount(); i++) {
            getColumnModel().getColumn(i).setCellRenderer( centerRenderer );
        }
    }

    @Override
    public Component prepareRenderer(TableCellRenderer renderer, int row,
                                     int column) {
        Component component = super.prepareRenderer(renderer, row, column);
        // if the rendere is a JComponent and the given row isn't part of a
        // selection, make the renderer non-opaque so that striped rows show
        // through.
        if (component instanceof JComponent) {
            ((JComponent)component).setOpaque(getSelectionModel().isSelectedIndex(row));
        }
        return component;
    }

    // Stripe painting Viewport. //////////////////////////////////////////////

    /**
     * Creates a JViewport that draws a striped backgroud corresponding to the
     * row positions of the given JTable.
     */
    private static class StripedViewport extends JViewport {

        private static final long serialVersionUID = 171992496170114834L;

        Color BACKGROUND_COLOR = Color.LIGHT_GRAY;

        private final JTable fTable;

        public StripedViewport(JTable table, Color bg) {
            BACKGROUND_COLOR = bg;
            fTable = table;
            setBackground(BACKGROUND_COLOR);
            setOpaque(false);
            initListeners();
        }

        private void initListeners() {
            // install a listener to cause the whole table to repaint when
            // a column is resized. we do this because the extended grid
            // lines may need to be repainted. this could be cleaned up,
            // but for now, it works fine.
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
                g.setColor(getRowColor(j));
                g.fillRect(0, j * rowHeight, tableWidth, rowHeight);
            }
        }

        private Color getRowColor(int row) {
            return row % 2 == 0 ? EVEN_ROW_COLOR : ODD_ROW_COLOR;
        }

        private void paintGridLines(Graphics g) {
            int rowHeight = fTable.getRowHeight();
            int columnHeight = rowHeight * fTable.getColumnCount();
            g.setColor(TABLE_GRID_COLOR);
            int x = 0;
            g.drawLine(x, g.getClipBounds().y, x, columnHeight);
            for (int i = 0; i < fTable.getColumnCount(); i++) {
                TableColumn column = fTable.getColumnModel().getColumn(i);
                x += column.getWidth();
                g.drawLine(x - 1, g.getClipBounds().y, x - 1, columnHeight);
            }
            // x is now the table width
            for (int j = 0; j < fTable.getRowCount() + 1; j++) {
                g.drawLine(0, j * rowHeight, x - 1, j * rowHeight);
            }
        }

    }

    public static JScrollPane createStripedJScrollPane(JTable table, Color bg) {
        JScrollPane scrollPane =  new JScrollPane(table);
        scrollPane.setViewport(new StripedViewport(table, bg));
        scrollPane.getViewport().setView(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        return scrollPane;
    }

}

class ContextTableModel extends AbstractTableModel {

    private static final long serialVersionUID = -1509387655329719071L;

    private static final String X = "X";

    private final FormalContext<String,String> context;

    ContextTableModel(FormalContext<String,String> context) {
        this.context = context;
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
                context.getAttributeAtIndex(columnIndex - 1)) ? X : "";
    }

}
