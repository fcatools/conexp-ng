package fcatools.conexpng.gui.lattice;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

public class NodeMouseClickListener implements MouseListener {

	private Node node;
	private boolean clicked = false;

	public NodeMouseClickListener(Node n){
		this.node = n;
	}
	@Override
	public void mouseClicked(MouseEvent arg0) {
	}

	@Override
	public void mouseEntered(MouseEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseExited(MouseEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mousePressed(MouseEvent arg0) {
		if(!node.isPartOfAnIdeal()){
			node.toggleIdealVisibility();
		}
		else if(!clicked){
			node.toggleIdealVisibility();
		}

	}

	@Override
	public void mouseReleased(MouseEvent arg0) {


	}

}
