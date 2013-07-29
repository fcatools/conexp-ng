package fcatools.conexpng.gui.lattice;

import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;

import javax.swing.JPanel;

public class LatticeGraphNodeMouseMotionListener implements MouseMotionListener {

	private LatticeGraphElement n;

	public LatticeGraphNodeMouseMotionListener(LatticeGraphElement n) {
		this.n = n;
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		int newX = e.getX();
		int newY = e.getY();
		n.update(newX, newY, true);
	}

	@Override
	public void mouseMoved(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}
	

}
