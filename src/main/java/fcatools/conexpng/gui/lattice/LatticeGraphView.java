package fcatools.conexpng.gui.lattice;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;

import javax.swing.SwingUtilities;

import org.apache.batik.swing.JSVGCanvas;

import fcatools.conexpng.Conf;

/**
 * This class represented the graphical visualisation of the graph. It draws the
 * lattice graph.
 * 
 * The JSVGCanvas provides a set of build-in interactors that let the users
 * manipulate the displayed document, including ones for zooming, panning and
 * rotating. Interactors catch user input to the JSVGCanvas component and
 * translate them into behaviour.
 */
public class LatticeGraphView extends JSVGCanvas {

    private static final long serialVersionUID = -8623872314193862285L;
    private Conf state;
    private ArrayList<Node> lastIdeal;
    private boolean idealHighlighting;
    private boolean move;
    private static Font font = new Font("Monospaced", Font.PLAIN, 12);
    private Stroke drawingStroke = new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0,
            new float[] { 9 }, 0);
    private static Point offset = new Point(0, 0);

    public LatticeGraphView(Conf state) {
        this.state = state;
        this.lastIdeal = new ArrayList<Node>();
    }

    private void init() {
        this.removeAll();
        for (Node n : state.lattice.getNodes()) {
            this.add(n);
            this.add(n.getAttributesLabel());
            this.add(n.getObjectsLabel());
        }

    }

    /**
     * Returns the view's offset.
     * 
     * @return the view's offset
     */
    public static Point getOffset() {
        return offset;
    }

    /**
     * Sets the view's offset.
     * 
     * @param x
     * @param y
     */
    public static void setOffset(double x, double y) {
        offset.setLocation(x, y);
    }

    /**
     * Returns element at given position.
     * 
     * @param point
     *            position the element is expected
     * @return element at given position
     */
    public LatticeGraphElement getLatticeGraphElementAt(Point point) {
        for (Component c : this.getComponents()) {
            if (c.contains(SwingUtilities.convertPoint(this, point, c))) {
                return (LatticeGraphElement) c;
            }
        }
        return null;
    }

    @Override
    public void paint(Graphics g0) {
        super.paint(g0);

        Graphics2D g = (Graphics2D) g0;
        // zoom and pan view
        AffineTransform trans = new AffineTransform();
        trans.scale(LatticeView.zoomFactor, LatticeView.zoomFactor);
        trans.translate(offset.getX(), offset.getY());
        g.transform(trans);

        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        g.setColor(Color.BLACK);
        int radius = LatticeView.radius;

        for (Node n : state.lattice.getNodes()) {
            if (n.isPartOfAnIdeal() && !lastIdeal.contains(n)) {
                lastIdeal.clear();
            }
        }

        if (!state.lattice.getEdges().isEmpty() && state.guiConf.showEdges) {
            for (Edge e : state.lattice.getEdges()) {
                g.setColor(Color.BLACK);
                if (e.getU().isPartOfAnIdeal() && e.getV().isPartOfAnIdeal() && idealHighlighting) {
                    g.setColor(Color.BLUE);
                } else if (!(e.getU().isPartOfAnIdeal() && e.getV().isPartOfAnIdeal()) && idealHighlighting) {
                    g.setColor(Color.LIGHT_GRAY);
                }
                g.drawLine(e.getU().getX() + radius, e.getU().getY() + radius, e.getV().getX() + radius, e.getV()
                        .getY() + radius);
            }
        }
        for (Node n : state.lattice.getNodes()) {
            int x = n.getX();
            int y = n.getY();

            // draw a normal node
            g.setColor(Color.WHITE);
            g.fillOval(x, y, radius * 2, radius * 2);
            g.setColor(Color.BLACK);
            g.drawOval(x, y, radius * 2, radius * 2);

            // label drawing
            if ((!n.getVisibleObjects().isEmpty()) && state.guiConf.showObjectLabel) {
                g.setColor(Color.BLACK);

                // dashed line
                g.setStroke(drawingStroke);
                g.drawLine(n.getObjectsLabel().getX() + n.getObjectsLabel().getBounds().width / 2, n.getObjectsLabel()
                        .getY(), x + radius, y + radius);
                g.setStroke(new BasicStroke());

                // draw the label
                String content = n.getObjectsLabel().elementsToString();
                g.setFont(font);
                FontMetrics fm = g.getFontMetrics();
                Rectangle r = fm.getStringBounds(content, g).getBounds();

                g.setColor(Color.WHITE);
                g.fillRect(n.getObjectsLabel().getX(), n.getObjectsLabel().getY(), r.width, r.height);

                g.setColor(Color.BLACK);

                g.drawString(content, n.getObjectsLabel().getX(), n.getObjectsLabel().getY() - r.y);

                g.drawRect(n.getObjectsLabel().getX(), n.getObjectsLabel().getY(), r.width, r.height);

                n.getObjectsLabel()
.setBounds(n.getObjectsLabel().getX(), n.getObjectsLabel().getY() - r.y,
                        r.width, r.height);

            }

            // analog like objects
            if ((!n.getVisibleAttributes().isEmpty()) && state.guiConf.showAttributLabel) {
                g.setColor(Color.BLACK);
                g.setStroke(drawingStroke);
                g.drawLine(n.getAttributesLabel().getX() + n.getAttributesLabel().getBounds().width / 2, n
                        .getAttributesLabel().getY(), x
                        + radius, y + radius);
                g.setStroke(new BasicStroke());

                String content = n.getAttributesLabel().elementsToString();
                g.setFont(font);
                FontMetrics fm = g.getFontMetrics();
                Rectangle r = fm.getStringBounds(content, g).getBounds();

                g.setColor(Color.WHITE);
                g.fillRect(n.getAttributesLabel().getX(), n.getAttributesLabel().getY(), r.width, r.height);

                g.setColor(Color.BLUE);

                g.drawString(content, n.getAttributesLabel().getX(), n.getAttributesLabel().getY() - r.y);

                g.setColor(Color.BLACK);
                g.drawRect(n.getAttributesLabel().getX(), n.getAttributesLabel().getY(), r.width, r.height);

                n.getAttributesLabel().setBounds(n.getAttributesLabel().getX(), n.getAttributesLabel().getY() - r.y,
                        r.width,
                        r.height);

                // draw filled node

            }
            if (!n.getVisibleAttributes().isEmpty() && !n.getVisibleObjects().isEmpty()) {
                g.setColor(Color.BLACK);
                g.fillOval(x, y, LatticeView.radius * 2, LatticeView.radius * 2);
                g.setColor(Color.BLUE);
                g.fillArc(x, y, LatticeView.radius * 2, LatticeView.radius * 2, 0, 180);
                g.setColor(Color.BLACK);
                g.drawOval(x, y, LatticeView.radius * 2, LatticeView.radius * 2);

            } else if (!n.getVisibleAttributes().isEmpty()) {
                g.setColor(Color.BLUE);
                g.fillOval(x, y, LatticeView.radius * 2, LatticeView.radius * 2);
                g.setColor(Color.WHITE);
                g.fillArc(x, y, LatticeView.radius * 2, LatticeView.radius * 2, 180, 180);
                g.setColor(Color.BLACK);
                g.drawOval(x, y, LatticeView.radius * 2, LatticeView.radius * 2);
            } else if (!n.getVisibleObjects().isEmpty()) {
                g.setColor(Color.BLACK);
                g.fillOval(x, y, LatticeView.radius * 2, LatticeView.radius * 2);
                g.setColor(Color.WHITE);
                g.fillArc(x, y, LatticeView.radius * 2, LatticeView.radius * 2, 0, 180);
                g.setColor(Color.BLACK);
                g.drawOval(x, y, LatticeView.radius * 2, LatticeView.radius * 2);
            }

            // highlight an ideal if it selected

            if (n.isPartOfAnIdeal() && idealHighlighting) {
                lastIdeal.add(n);
                g.setColor(Color.BLUE);
                g.drawOval(x, y, radius * 2, radius * 2);
            } else if ((!n.isPartOfAnIdeal()) && idealHighlighting) {
                g.setColor(Color.LIGHT_GRAY);
                g.drawOval(x, y, radius * 2, radius * 2);
            }

            // highlight the node if it is the node that was clicked on.
            if (n.isClickedOn() && idealHighlighting) {
                g.setColor(Color.RED);
                g.drawOval(x - 1, y - 1, radius * 2 + 2, radius * 2 + 2);
            }
        }
    }

    public void updateLatticeGraph() {
        init();
        setMove(move);
    }

    public void resetHighlighting() {
        for (Node n : lastIdeal) {
            n.setPartOfAnIdeal(false);
            n.setClickedOn(false);
        }
    }

    public void setMove(boolean change) {
        move = change;
        for (Node n : state.lattice.getNodes()) {
            n.moveSubgraph(change);
        }
    }

    public void idealHighlighting(boolean change) {
        this.idealHighlighting = change;
    }

}
