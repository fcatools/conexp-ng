package fcatools.conexpng.model;

import java.util.Iterator;
import java.util.Set;

import de.tudresden.inf.tcs.fcaapi.Concept;
import de.tudresden.inf.tcs.fcalib.FullObject;
import de.tudresden.inf.tcs.fcalib.utils.ListSet;
import fcatools.conexpng.gui.lattice.Edge;
import fcatools.conexpng.gui.lattice.LatticeGraph;
import fcatools.conexpng.gui.lattice.Node;

public class TestLatticeAlgorithm extends ILatticeAlgorithm {

	private Set<Concept<String, FullObject<String, String>>> lattConcepts;

	@Override
	public LatticeGraph computeLatticeGraph(Set<Concept<String, FullObject<String, String>>> concepts) {
		graph = new LatticeGraph();
		this.lattConcepts = concepts;
		
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
			u.update((int) (Math.random() * 500), count * 100, true);
		}
		
		
		computeAllIdeals();
		return graph;
	}
	
	
}
