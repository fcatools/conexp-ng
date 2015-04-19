package fcatools.conexpng.gui.lattice.algorithms;

import java.util.List;

import org.latdraw.diagram.Diagram;
import org.latdraw.diagram.Vertex;
import org.latdraw.orderedset.NonOrderedSetException;
import org.latdraw.orderedset.OrderedSet;

import fcatools.conexpng.gui.lattice.LatticeGraph;
import fcatools.conexpng.gui.lattice.LatticeView;
import fcatools.conexpng.gui.lattice.Node;

/**
 * This uses the lattice graph algorithm by Ralph Freese in jar form.
 * 
 * @author Torsten Casselt
 * @see <a href="http://www.latdraw.org/">http://www.latdraw.org</a>
 */
public class ExternalFreeseLatticeGraphAlgorithm implements ILatticeGraphAlgorithm {

    /**
     * {@inheritDoc}
     */
    @Override
    public LatticeGraph computeLatticeGraphPositions(LatticeGraph graph, int screenWidth, int screenHeight) {
        Diagram diagram = null;
        if (!graph.isEmpty()) {
            List<Node> nodes = graph.getNodes();
            try {
                diagram = new Diagram(new OrderedSet("", nodes, graph.getUpperNeighbors()));
            } catch (NonOrderedSetException e) {
                e.printStackTrace();
            }
            diagram.improve();
            // project to 2d
            final double cos = Math.cos(0);
            final double sin = Math.sin(0);
            Vertex[] vertices = diagram.getVertices();
            Vertex currVertex;
            Node currNode;
            for (int i = 0; i < vertices.length; i++) {
                currVertex = vertices[i];
                currNode = (Node) currVertex.getUnderlyingObject();
                currNode.setX((int) ((cos * currVertex.getNormalizedX() + sin * currVertex.getNormalizedY())
                        * screenWidth * 0.9 + (screenWidth / 2) - LatticeView.radius));
                currNode.setY((int) (currVertex.getNormalizedZ() * screenHeight * 0.9));
                currNode.positionLabels();
            }
        }
        return graph;
    }
}
