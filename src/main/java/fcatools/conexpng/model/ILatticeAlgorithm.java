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
	public abstract LatticeGraph computeLatticeGraph(
			Set<Concept<String, FullObject<String, String>>> set);

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
				if (!superEx.equals(set)) {
					if (isSubconcept(subEx, set)) {
						if (isSubconcept(set, superEx)) {
							return false;
						}
					}
				}
			}
		}
		return true;
	}
	
	private void removeAllDuplicates(){
		ArrayList<Node> duplicates = new ArrayList<>();
		for(int i = 0; i < graph.getNodes().size()-1; i++){
			Node u = graph.getNodes().get(i);
			for(int j = i+1; j < graph.getNodes().size(); j++){
				Node v = graph.getNodes().get(j);
				if(u.getObjects().equals(v.getObjects()) && u.getAttributes().equals(v.getAttributes())){
					duplicates.add(v);
					u.getBelow().remove(v);
				}
			}
			
		}
		graph.getNodes().removeAll(duplicates);
	}

	/**
	 * 
	 */
	public void computeAllIdeals() {
		// sort the list of nodes from bottom too top
		removeAllDuplicates();
		ArrayList<Node> q = new ArrayList<>();
		for (Node n : graph.getNodes()) {
			if (q.size() == 0) {
				q.add(n);
			} else {
				for (int i = 0; i < q.size(); i++) {
					if (q.get(i).getObjects().containsAll(n.getObjects())
							|| q.get(i).getObjects().size() > n.getObjects()
									.size()) {
						q.add(i, n);
						break;
					}
					if (i + 1 == q.size()) {
						q.add(i + 1, n);
						break;
					}
				}
			}
			String s = "";
			String a = "";

			for (int i = 0; i < q.size(); i++) {
				Node u = q.get(i);
				s = s + " " + u.getObjects();
				a = a + " " + u.getAttributes();
			}
			System.out.println(s);
			System.out.println(a);
			System.out.println("/");
		}
		System.out.println("test");
		for (int i = 0; i < q.size(); i++) {
			Node n = q.get(i);
			n.computeIdeal();
		}
	}

}
