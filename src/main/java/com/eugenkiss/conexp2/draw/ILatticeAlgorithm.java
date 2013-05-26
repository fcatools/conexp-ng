package com.eugenkiss.conexp2.draw;

import java.util.Set;

import de.tudresden.inf.tcs.fcaapi.Concept;
import de.tudresden.inf.tcs.fcalib.FullObject;

public interface ILatticeAlgorithm {

	
	
	/**
	 * Compute the closure.
	 * @return
	 */
	public boolean closure();
	
	
	/**
	 * Draw the given ConceptLattice.
	 * 
	 * @param ConceptLattice
	 *            The lattice which has to be drawn.
	 * @return true, iff  drew?.
	 */
	public boolean draw(Set<Concept<String, FullObject<String, String>>> ConceptLattice);

	
}
