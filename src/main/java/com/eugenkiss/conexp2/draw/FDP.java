package com.eugenkiss.conexp2.draw;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import de.tudresden.inf.tcs.fcaapi.Concept;
import de.tudresden.inf.tcs.fcalib.FormalContext;
import de.tudresden.inf.tcs.fcalib.FullObject;

/**
 * This class implements the Force Directed Placement (FDP) algorithm for
 * drawing lattices.
 *
 * @author Jan
 *
 */
public class FDP implements ILatticeAlgorithm {

    private Set<Concept<String, FullObject<String, String>>> lattice;
    private List<Node> nodes;
    private List<Edge> edges;

    public FDP() {
        this.nodes = new ArrayList<Node>();
        this.edges = new ArrayList<Edge>();
    }

    @Override
    public boolean init(FormalContext context) {
        // TODO: wie sind die concepte verbunden????
        lattice = context.getConceptLattice();
        // for each concept create a node for the graph
        for (Concept<String, FullObject<String, String>> c : lattice) {
            Node n = new Node();
            // the attributes???
            for (String s : c.getIntent()) {
                n.addAttribut(s);
            }
            // the objects???
            for (FullObject<String, String> f : c.getExtent()) {
                n.addObject(f.toString());
            }
            nodes.add(n);
        }
        return initPhase();
    }

    private boolean initPhase() {
        return forces();
    }

    private boolean forces() {
        return true;
    }

}
