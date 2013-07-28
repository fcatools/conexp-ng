package fcatools.conexpng.model;

import java.util.ArrayList;
import java.util.Set;

import de.tudresden.inf.tcs.fcaapi.Concept;
import de.tudresden.inf.tcs.fcalib.FullObject;

import fcatools.conexpng.gui.lattice.LatticeGraph;
import fcatools.conexpng.gui.lattice.LatticeGraphNodeMouseMotionListener;
import fcatools.conexpng.gui.lattice.Node;
import fcatools.conexpng.gui.lattice.NodeMouseClickListener;


/**
 * 
 * @author Jan
 *
 */
public abstract class ILatticeAlgorithm {


	protected LatticeGraph graph;
	/**
	 * 
	 * @param set
	 * @return
	 */
    public abstract LatticeGraph computeLatticeGraph(Set<Concept<String, FullObject<String, String>>> set);
    
    
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
		//sort the list of nodes from bottom too top
		ArrayList<Node> q = new ArrayList<>();
        for (Node n : graph.getNodes()) {
            if (q.size() == 0) {
                q.add(n);
            }else{
            	for (int i = 0; i < q.size(); i++) {
                    if (n.getObjects().containsAll(q.get(i).getObjects())) {
                        q.add(i, n);
                        break;
                    }
                    if(i + 1 == q.size()){
                    	q.add(i+1, n);
                    	break;
                    }
                }
            }
            String s = "";

            for (int i = q.size() - 1; i >= 0; i--) {
                Node u = q.get(i);
               s = s + " " + u.getObjects();
            }
            System.out.println(s);
            System.out.println("/");
        }
        
        for (int i = q.size() - 1; i >= 0; i--) {
            Node n = q.get(i);
            n.computeIdeal();
        }
	}


}
