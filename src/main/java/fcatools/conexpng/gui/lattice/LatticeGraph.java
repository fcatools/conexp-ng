package fcatools.conexpng.gui.lattice;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import de.tudresden.inf.tcs.fcaapi.Concept;
import de.tudresden.inf.tcs.fcalib.FullObject;
import fcatools.conexpng.model.TestLatticeAlgorithm;

/**
 *
 * @author Jan
 *
 */
public class LatticeGraph {

    private List<Node> nodes;
    private List<Edge> edges;

    /**
     *
     */
    public LatticeGraph() {
        this.nodes = new ArrayList<>();
        this.edges = new ArrayList<>();
    }

    /**
     *
     * @param nodes
     * @param edges
     */
    public LatticeGraph(List<Node> nodes, List<Edge> edges) {
        this.nodes = nodes;
        this.edges = edges;
    }

    /**
     *
     * @param i
     * @return
     */
    public Node getNode(int i) {
        return nodes.get(i);
    }

    /**
     *
     * @return
     */
    public List<Node> getNodes() {
        return nodes;
    }

    /**
     *
     * @param nodes
     */
    public void setNodes(List<Node> nodes) {
        this.nodes = nodes;
    }

    /**
     *
     * @param i
     * @return
     */
    public Edge getEdge(int i) {
        return edges.get(i);
    }

    /**
     *
     * @return
     */
    public List<Edge> getEdges() {
        return edges;
    }

    /**
     *
     * @param edges
     */
    public void setEdges(List<Edge> edges) {
        this.edges = edges;
    }

    public boolean missingEdges() {
        return edges.isEmpty() && nodes.size() > 1;
    }

    public void translate(int d, int e) {
        for (Node n : nodes) {
            n.setX(n.getX() + d);
            n.setY(n.getY() + e);
            n.getObjectsLabel().update(d, e, false);
            n.getAttributesLabel().update(d, e, false);
        }
    }

    public void addEdges(Set<Concept<String, FullObject<String, String>>> concepts) {
        LatticeGraph temp = new TestLatticeAlgorithm().computeLatticeGraph(concepts);
        for (Edge e : temp.edges) {
            Node u = getNodeWithIntent(e.getU().getAttributes());
            Node v = getNodeWithIntent(e.getV().getAttributes());
            if (u != null && v != null && !u.equals(v)) {
                u.getObjects().addAll(e.getU().getObjects());
                v.getObjects().addAll(e.getV().getObjects());
                u.setVisibleAttributes(e.getU().getVisibleAttributes());
                v.setVisibleAttributes(e.getV().getVisibleAttributes());
                u.setVisibleObjects(e.getU().getVisibleObjects());
                v.setVisibleObjects(e.getV().getVisibleObjects());
                u.setLevel(e.getU().getLevel());
                v.setLevel(e.getV().getLevel());
                u.addBelowNode(v);
                edges.add(new Edge(u, v));
            }
        }
        computeAllIdeals();
    }

    public void removeAllDuplicates() {
        ArrayList<Node> duplicates = new ArrayList<>();
        for (Node n : getNodes()) {
            if (n.getObjects().isEmpty() && n.getAttributes().isEmpty()) {
                duplicates.add(n);
            }
        }
        getNodes().removeAll(duplicates);
        duplicates.clear();
        for (int i = 0; i < getNodes().size() - 1; i++) {
            Node u = getNodes().get(i);
            for (int j = i + 1; j < getNodes().size(); j++) {
                Node v = getNodes().get(j);
                if (u.getObjects().equals(v.getObjects()) && u.getAttributes().equals(v.getAttributes())) {
                    duplicates.add(v);
                }
            }

        }
        getNodes().removeAll(duplicates);
        for (Node n : getNodes()) {
            n.getBelow().removeAll(duplicates);
        }
    }

    /**
     *
     */
    public void computeAllIdeals() {
        // sort the list of nodes from bottom too top

        ArrayList<Node> q = new ArrayList<>();
        for (Node n : getNodes()) {
            if (q.size() == 0) {
                q.add(n);
            } else {
                for (int i = 0; i < q.size(); i++) {
                    if (q.get(i).getObjects().containsAll(n.getObjects())
                            || q.get(i).getObjects().size() > n.getObjects().size()) {
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
            for (int j = i - 1; j >= 0; j--) {
                Node v = q.get(j);
                if (u.getObjects().containsAll(v.getObjects()) && v.getAttributes().containsAll(u.getAttributes())) {
                    u.getIdeal().add(v);
                }
            }
        }
    }

    private Node getNodeWithIntent(Set<String> attributes) {
        for (Node n : nodes) {
            if (n.getAttributes().containsAll(attributes) && attributes.containsAll(n.getAttributes()))
                return n;
        }
        return null;
    }

    public boolean isEmpty() {
        return nodes.isEmpty();
    }

}
