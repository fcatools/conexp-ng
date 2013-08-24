package fcatools.conexpng.model;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.TreeSet;

import de.tudresden.inf.tcs.fcaapi.Concept;
import de.tudresden.inf.tcs.fcalib.FullObject;
import de.tudresden.inf.tcs.fcalib.utils.ListSet;
import fcatools.conexpng.gui.lattice.Edge;
import fcatools.conexpng.gui.lattice.LatticeGraph;
import fcatools.conexpng.gui.lattice.Node;

/**
 *
 * @author Jan
 *
 */
public class LatticeGraphComputer {

    private LatticeGraph graph;
    private Set<Concept<String, FullObject<String, String>>> lattConcepts;
    private HashMap<String ,ILatticeGraphAlgorithm> algorithms;
    private int screenWidth;
    private int screenHeight;

    /**
     *
     * @param set
     * @param bounds
     * @return
     */
    public LatticeGraphComputer(){
    	algorithms = new HashMap<>();
    	algorithms.put("Test", new TestLatticeAlgorithm());   	
    }
    
    
    public LatticeGraph computeLatticeGraph(
            Set<Concept<String, FullObject<String, String>>> set, Rectangle bounds) {
        this.lattConcepts = set;
        this.screenWidth = bounds.width;
        this.screenHeight = bounds.height;
        initGraph();
        graph.computeAllIdeals();
        computeVisibleObjectsAndAttributes();
        this.graph = algorithms.get("Test").computeLatticeGraphPositions(graph);
        return graph;
    }

    public void initGraph() {
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
            graph.getNodes().add(n);
        }

        graph.removeAllDuplicates();

        List<Node> topNode = new ArrayList<>();
        for (Node u : graph.getNodes()) {
            topNode.add(u);
            Set<String> uEx = u.getObjects();
            for (Node v : graph.getNodes()) {
                Set<String> vEx = v.getObjects();
                if (isLowerNeighbour(uEx, vEx)) {
                    v.addBelowNode(u);
                    graph.getEdges().add(new Edge(u, v));
                    topNode.remove(u);
                }
            }
        }
        Queue<Node> q = new LinkedList<>();
        q.addAll(topNode);
        while (!q.isEmpty()) {
            Node n = q.remove();
            for (Node v : n.getBelow()) {
                if (v.getLevel() == 0 || v.getLevel() == n.getLevel()) {
                    v.setLevel(n.getLevel() + 1);
                    v.update((int) (Math.random() * 500), 100 * v.getLevel(),
                            true);
                    v.getAttributesLabel()
                            .setXYWRTLabelType(v.getX(), v.getY());
                    v.getObjectsLabel().setXYWRTLabelType(v.getX(), v.getY());
                    q.add(v);
                }
            }
        }

    }

    /**
     * Computes the node positions of the graph.
     */
    public void computeLatticeGraphPositions(){
    	
    }

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



    public void computeVisibleObjectsAndAttributes() {
        // calc which obj/attr has to be shown
        Set<String> usedObj = new TreeSet<>();
        Set<String> usedAttr = new TreeSet<>();
        Node maxNode = new Node();
        Node minNode;
        if (graph.getNodes().size() == 0) {
            minNode = new Node();
        } else {
            minNode = graph.getNode(0);
        }

        for (Node u : graph.getNodes()) {
            if (u.getIdeal().size() >= maxNode.getIdeal().size()) {
                maxNode = u;
            } else if (u.getIdeal().size() <= minNode.getIdeal().size()) {
                minNode = u;
            }
        }

        Queue<Node> pq = new LinkedList<>();
        pq.add(maxNode);
        while (!pq.isEmpty()) {
            Node n = pq.remove();
            for (String a : n.getAttributes()) {
                if (!usedAttr.contains(a)) {
                    n.setVisibleAttribute(a);
                    usedAttr.add(a);
                }
            }
            for (Node u : n.getBelow()) {
                pq.add(u);
            }
        }

        pq.add(minNode);
        while (!pq.isEmpty()) {
            Node n = pq.remove();
            for (String o : n.getObjects()) {
                if (!usedObj.contains(o)) {
                    n.setVisibleObject(o);
                    usedObj.add(o);
                }
            }
            for (Node u : graph.getNodes()) {
                if (u.getBelow().contains(n)) {
                    pq.add(u);
                }
            }
        }
    }

}
