package fcatools.conexpng.model;

import java.util.Set;

import fcatools.conexpng.gui.lattice.LatticeGraph;
import fcatools.conexpng.gui.lattice.Node;


/**
 * 
 * @author Jan
 *
 */
public abstract class ILatticeAlgorithm {


	protected LatticeGraph graph;
	/**
	 * 
	 * @param context
	 * @return
	 */
    public abstract LatticeGraph computeLatticeGraph(FormalContext context);
    
    
    /**
     * 
     * @param subEx
     * @param superEx
     * @return
     */
	public boolean isSubconcept(Set<String> subEx, Set<String> superEx) {
		if (subEx == superEx) {
			return false;
		}
		if (subEx.size() > superEx.size()) {
			return false;
		}
		for (String s : subEx) {
			if (!superEx.contains(s)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * 
	 * @param subEx
	 * @param superEx
	 * @return
	 */
	public boolean isLowerNeighbour(Set<String> subEx, Set<String> superEx) {
		if (subEx == superEx) {
			return false;
		}
		if (!isSubconcept(subEx, superEx)) {
			return false;
		}
		for (Node n : graph.getNodes()) {
			Set<String> set = n.getObjects();
			if (!subEx.equals(set)) {
				if(!superEx.equals(set)){
					if(isSubconcept(subEx, set)){
						if(isSubconcept(set, superEx)){
							return false;
						}
					}
				}
			}
		}
		return true;
	}
	
	/**
	 * 
	 */
	public void computeAllIdeals(){
		for(Node n:graph.getNodes()){
			n.computeIdeal();
		}
	}


}
