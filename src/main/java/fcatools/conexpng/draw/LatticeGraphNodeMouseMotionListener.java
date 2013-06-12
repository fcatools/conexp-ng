package fcatools.conexpng.draw;

import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;

public class LatticeGraphNodeMouseMotionListener implements MouseMotionListener {

	private Node n;

	public LatticeGraphNodeMouseMotionListener(Node n) {
		this.n = n;
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		int newX = e.getX();
		int newY = e.getY();
		n.update(newX, newY);
	}

	@Override
	public void mouseMoved(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}
	

}
