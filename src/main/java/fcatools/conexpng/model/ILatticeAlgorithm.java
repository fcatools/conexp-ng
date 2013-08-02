package fcatools.conexpng.model;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;

import de.tudresden.inf.tcs.fcaapi.Concept;
import de.tudresden.inf.tcs.fcalib.FullObject;
import de.tudresden.inf.tcs.fcalib.utils.ListSet;
import fcatools.conexpng.gui.lattice.Edge;
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
	protected Set<Concept<String, FullObject<String, String>>> lattConcepts;


	/**
	 * 
	 * @param set
	 * @return
	 */
	public LatticeGraph computeLatticeGraph(
			Set<Concept<String, FullObject<String, String>>> set){
		this.lattConcepts = set;
		initGraphPositions();
		computeAllIdeals();
		computeLatticeGraphPositions();
		return graph;
	}
	
	private void initGraphPositions(){
		graph = new LatticeGraph();

		Iterator<Concept<String, FullObject<String, String>>> iter = lattConcepts
				.iterator();
		while (iter.hasNext()) {
			Node n = new Node();
			Concept<String, FullObject<String, String>> c = (Concept<String, FullObject<String, String>>) iter
					.next();
			n.addAttributs(c.getIntent());

			ListSet<String> extent = new ListSet<>();
			for (FullObject<String, String> fo : c.getExtent()) {
				extent.add(fo.getIdentifier());
			}
			n.getObjects().addAll(extent);
//			System.out.println(c.getIntent() + " : " + extent);
			graph.getNodes().add(n);
		}

		
		List<Node> topNode = new ArrayList<>();
		for (Node u : graph.getNodes()) {
			topNode.add(u);
			Set<String> uEx = u.getObjects();
			for (Node v : graph.getNodes()) {
				Set<String> vEx = v.getObjects();
				if (isLowerNeighbour(uEx, vEx)) {
					v.addBelowNode(u);
					topNode.remove(u);
				}
			}
		}
		Queue<Node> q = new LinkedList<>();
		q.addAll(topNode);
		while(!q.isEmpty()){
			Node n = q.remove();
			for(Node v : n.getBelow()){
				if(v.getLevel() == 0 || v.getLevel() == n.getLevel()){
					v.setLevel(n.getLevel() + 1);
					v.update((int) (Math.random() * 500), 100*v.getLevel(), true);
					q.add(v);
				}
			}
		}
		
		
	}
	
	public abstract void computeLatticeGraphPositions();

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
				}
			}
			
		}
		graph.getNodes().removeAll(duplicates);
		for(Node n : graph.getNodes()){
			n.getBelow().removeAll(duplicates);
		}
	}

	/**
	 * 
	 */
	public void computeAllIdeals() {
		// sort the list of nodes from bottom too top
		
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
		}
		for (int i = 1; i < q.size(); i++) {
			Node u = q.get(i);
			for (int j = i-1; j >= 0; j--) {
				Node v = q.get(j);
				if(u.getObjects().containsAll(v.getObjects()) && v.getAttributes().containsAll(u.getAttributes())){
					u.getIdeal().add(v);
				}
			}
		}
		removeAllDuplicates();
	}

}
