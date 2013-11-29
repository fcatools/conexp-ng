package fcatools.conexpng.gui.lattice;

import java.awt.Rectangle;

/**
 * This interface has to be implemented by all elements which are in relation to
 * the LatticeGraph.class.
 * 
 */
public interface LatticeGraphElement {

    public abstract void update(int x, int y, boolean first);

    public abstract int getX();

    public abstract int getY();

    public abstract Rectangle getBounds();

}
