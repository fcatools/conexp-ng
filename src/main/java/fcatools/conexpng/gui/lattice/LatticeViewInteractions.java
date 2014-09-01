package fcatools.conexpng.gui.lattice;

import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;

import javax.swing.Timer;

/**
 * Listener for interactions in lattice view like pan, zoom or movement of
 * labels/nodes.
 * 
 * @author Torsten Casselt
 */
public class LatticeViewInteractions extends MouseAdapter {

    private int dragBeginX, originalElementPosX;
    private int dragBeginY, originalElementPosY;
    private Timer timer;
    private boolean clicked = false;
    private LatticeGraphElement clickedOn;
    private LatticeViewUndoManager undoManager;

    /**
     * Creates a mouse adapter.
     * 
     * @param undoManager
     *            undo manager for lattice to add undoable edits when moving
     *            elements
     */
    public LatticeViewInteractions(LatticeViewUndoManager undoManager) {
        this.undoManager = undoManager;
    }

    /**
     * {@inheritDoc}
     */
    public void mousePressed(final MouseEvent e) {
        final LatticeGraphView view = (LatticeGraphView) e.getSource();
        // zoom if ctrl is pressed
        if (e.isControlDown()) {
            // timer used to fire the event as long as the mouse button is
            // pressed
            timer = new Timer(0, new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent arg0) {
                    if (e.getButton() == MouseEvent.BUTTON1) {
                        LatticeView.setZoomFactor(LatticeView.getZoomFactor() + 0.01);
                    } else if (e.getButton() == MouseEvent.BUTTON3) {
                        LatticeView.setZoomFactor(LatticeView.getZoomFactor() - 0.01);
                    }
                    // reset zoom factor to 0 if too low
                    if (LatticeView.getZoomFactor() < 0) {
                        LatticeView.setZoomFactor(0);
                    }
                    // repaint
                    view.repaint();
                }
            });
            timer.start();

        }
        // save mouse position for dragging
        dragBeginX = (int) (LatticeGraphView.getOffset().getX() - e.getX());
        dragBeginY = (int) (LatticeGraphView.getOffset().getY() - e.getY());
        // check if clicked on label or node
        Point2D point = e.getPoint();
        // remove zoom and offset to reach the model
        AffineTransform trans = new AffineTransform();
        trans.scale(LatticeView.getZoomFactor(), LatticeView.getZoomFactor());
        trans.translate(LatticeGraphView.getOffset().getX(), LatticeGraphView.getOffset().getY());
        try {
            trans.inverseTransform(point, point);
        } catch (NoninvertibleTransformException e1) {
            e1.printStackTrace();
        }
        // get element clicked on
        clickedOn = view.getLatticeGraphElementAt((Point) point);
        if (clickedOn != null) {
            // handle node clicked on
            if (clickedOn instanceof Node) {
                Node node = (Node) clickedOn;
                if (!node.isPartOfAnIdeal()) {
                    node.toggleIdealVisibility();
                } else if (!clicked) {
                    node.toggleIdealVisibility();
                }
            }
            // save mouse position for dragging without
            // jumping of dragged component
            dragBeginX = e.getX();
            dragBeginY = e.getY();
            // save original element position
            originalElementPosX = clickedOn.getX();
            originalElementPosY = clickedOn.getY();
        }
    }

    /**
     * {@inheritDoc}
     */
    public void mouseReleased(MouseEvent e) {
        if (timer != null) {
            timer.stop();
        }
        // register action with undo manager only if lattice graph element is
        // moved, not if viewport is panned
        if (clickedOn != null) {
            undoManager.makeRedoable(clickedOn, originalElementPosX, originalElementPosY);
            clickedOn = null;
        }
    }

    /**
     * {@inheritDoc}
     * 
     * @since 1.6
     */
    public void mouseDragged(MouseEvent e) {
        if (clickedOn != null) {
            // update position of the element with respect to drag start point
            clickedOn.update((int) (originalElementPosX + (e.getX() - dragBeginX) / LatticeView.getZoomFactor()),
                    (int) (originalElementPosY + (e.getY() - dragBeginY) / LatticeView.getZoomFactor()));
        } else {
            // move lattice view
            LatticeGraphView view = (LatticeGraphView) e.getSource();
            LatticeGraphView.setOffset(dragBeginX + e.getX(), dragBeginY + e.getY());
            view.repaint();
        }
    }
}
