package fcatools.conexpng.model;

import java.util.Iterator;
import java.util.Set;

import de.tudresden.inf.tcs.fcaapi.Concept;
import de.tudresden.inf.tcs.fcalib.FullObject;
import de.tudresden.inf.tcs.fcalib.utils.ListSet;
import fcatools.conexpng.gui.lattice.Edge;
import fcatools.conexpng.gui.lattice.LatticeGraph;
import fcatools.conexpng.gui.lattice.Node;

public class TestLatticeAlgorithm implements ILatticeAlgorithm {

	private Set<Concept<String, FullObject<String, String>>> lattConcepts;
	private LatticeGraph graph;

	@Override
	public LatticeGraph computeLatticeGraph(FormalContext context) {
		graph = new LatticeGraph();
		this.lattConcepts = context.getConcepts();
		
		Iterator<Concept<String, FullObject<String, String>>> iter = lattConcepts.iterator();
		while(iter.hasNext()){
			Node n = new Node();
			Concept<String, FullObject<String, String>> c = (Concept<String, FullObject<String, String>>) iter.next();
			n.addAttributs(c.getIntent());
			
			ListSet<String> extent = new ListSet<>();
			for(FullObject<String, String> fo : c.getExtent()){
				extent.add(fo.getIdentifier());
			}			
			n.getObjects().addAll(extent);
			System.out.println(c.getIntent() + " : "+ extent);
			graph.getNodes().add(n);
		}

		for (Node u : graph.getNodes()) {
			Set<String> uEx = u.getObjects();
			int count = 0;

			for (Node v : graph.getNodes()) {
				Set<String> vEx = v.getObjects();
				if (isLowerNeighbour(uEx, vEx)) {
					v.addBelowNode(u);
					graph.getEdges().add(new Edge(v,u));
					count++;
				}

			}
			u.setLevel(count);
			u.update((int) (Math.random() * 500), count * 100);
		}
		

		return graph;
	}

	private boolean isSubconcept(Set<String> subEx, Set<String> superEx) {
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

	private boolean isLowerNeighbour(Set<String> subEx, Set<String> superEx) {
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
}
