package com.eugenkiss.conexp2.draw;

import com.eugenkiss.conexp2.model.FormalContext;


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
