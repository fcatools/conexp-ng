package fcatools.conexpng.model;

import fcatools.conexpng.gui.lattice.LatticeGraph;


/**
 * 
 * @author Jan
 *
 */
public interface ILatticeAlgorithm {


	/**
	 * 
	 * @param context
	 * @return
	 */
    public LatticeGraph computeLatticeGraph(FormalContext context);


}
