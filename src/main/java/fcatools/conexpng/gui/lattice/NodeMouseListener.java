package fcatools.conexpng.gui.lattice;

import java.awt.event.MouseEvent;

import javax.swing.event.MouseInputAdapter;

/**
 * Listener for mouse events on objects in lattice view.
 * 
 * @author blu2lz
 */
public class NodeMouseListener extends MouseInputAdapter {

	private boolean clicked = false;
	private int dragBeginX = 0;
	private int dragBeginY = 0;
	
	@Override
	public void mousePressed(MouseEvent me) {
		if (me.getSource() instanceof Node) {
			Node node = (Node) me.getSource();
			if (!node.isPartOfAnIdeal()) {
				node.toggleIdealVisibility();
			} else if (!clicked) {
				node.toggleIdealVisibility();
			}
		}
		// save mouse position for dragging without
		// jumping of dragged component
		dragBeginX = me.getX();
		dragBeginY = me.getY();
	}
	
	@Override
	public void mouseDragged(MouseEvent e) {
		// get element clicked on
		LatticeGraphElement nodeClickedOn = (LatticeGraphElement) e.getSource();
		// update position of the node with respect to drag start point
		nodeClickedOn.update(nodeClickedOn.getX() + e.getX() - dragBeginX,
				nodeClickedOn.getY() + e.getY() - dragBeginY, true);
	}
}