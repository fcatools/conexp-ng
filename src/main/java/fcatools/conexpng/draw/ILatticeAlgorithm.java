package fcatools.conexpng.draw;

import fcatools.conexpng.model.FormalContext;


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
