package fcatools.conexpng.model;

import fcatools.conexpng.gui.lattice.LatticeGraph;

/**
 * For adding an new lattice drawing algorithm you have to implement this
 * interface.
 * 
 * 
 */
public interface ILatticeGraphAlgorithm {

	/**
	 * This method will compute the correct position of the graph's nodes.
	 * 
	 * @param graph
	 *            contains already initialized nodes with correct y-values and
	 *            random x-values
	 * @param screenWidth
	 *            width of the viewport
	 * @param screenHeight
	 *            height of the viewport
	 * @return
	 */
	public abstract LatticeGraph computeLatticeGraphPositions(
			LatticeGraph graph, int screenWidth, int screenHeight);

}
