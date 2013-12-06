package fcatools.conexpng.gui.lattice;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.Timer;

/**
 * Listener for interactions in lattice view like pan or zoom.
 * 
 * @author Torsten Casselt
 */
public class LatticeViewInteractions extends MouseAdapter {

    private int dragBeginX;
    private int dragBeginY;
    private Timer timer;

    /**
     * {@inheritDoc}
     */
    public void mousePressed(final MouseEvent e) {
        // zoom if ctrl is pressed
        if (e.isControlDown()) {
            // timer used to fire the event as long as the mouse button is
            // pressed
            timer = new Timer(0, new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent arg0) {
                    if (e.getButton() == MouseEvent.BUTTON1) {
                        LatticeView.zoomFactor += 0.01;
                    } else if (e.getButton() == MouseEvent.BUTTON3) {
                        LatticeView.zoomFactor -= 0.01;
                    }
                    // reset zoom factor to 0 if too low
                    if (LatticeView.zoomFactor < 0) {
                        LatticeView.zoomFactor = 0;
                    }
                    // repaint
                    ((LatticeGraphView) e.getSource()).repaint();
                }
            });
            timer.start();

        }
        // save mouse position for dragging
        dragBeginX = (int) (LatticeGraphView.getOffset().getX() - e.getX());
        dragBeginY = (int) (LatticeGraphView.getOffset().getY() - e.getY());
    }

    /**
     * {@inheritDoc}
     */
    public void mouseReleased(MouseEvent e) {
        if (timer != null) {
            timer.stop();
        }
    }

    /**
     * {@inheritDoc}
     * 
     * @since 1.6
     */
    public void mouseDragged(MouseEvent e) {
        // move lattice view
        LatticeGraphView view = (LatticeGraphView) e.getSource();
        LatticeGraphView.setOffset(dragBeginX + e.getX(), dragBeginY + e.getY());
        view.repaint();
    }
}
