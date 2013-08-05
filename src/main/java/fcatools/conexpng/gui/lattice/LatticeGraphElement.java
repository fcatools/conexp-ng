package fcatools.conexpng.gui.lattice;

import java.awt.Rectangle;

public interface LatticeGraphElement {
	
	public abstract void update(int x, int y, boolean first);
	public abstract int getX();
	public abstract int getY();
	public abstract Rectangle getBounds();

}
