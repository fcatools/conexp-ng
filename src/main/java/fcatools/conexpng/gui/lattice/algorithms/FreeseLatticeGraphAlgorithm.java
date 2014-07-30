package fcatools.conexpng.gui.lattice.algorithms;

import java.util.Collections;
import java.util.List;

import fcatools.conexpng.gui.lattice.LatticeGraph;
import fcatools.conexpng.gui.lattice.Node;

/**
 * Implementation of a lattice graph algorithm by Ralph Freese.
 * 
 * @author Torsten Casselt
 * @see <a href="http://www.latdraw.org/">http://www.latdraw.org</a>
 */
public class FreeseLatticeGraphAlgorithm implements ILatticeGraphAlgorithm {

    final private static int[] PRIMES = { 3, 5, 7, 11, 13, 17, 19, 23, 29, 31 };
    final private static double ATTRACTION_CONSTANT = 0.1;
    final private static double REPULSION_CONSTANT = 1.0;
    final private static int ITERATIONS = 30;

    private double attraction_I, attraction_II, attraction_III;
    private double repulsion_I, repulsion_II, repulsion_III;
    private int repititions_I, repititions_II, repititions_III;

    private List<Node> nodes;
    private int primePointer;
    private double attractionFactor;
    private double repulsionFactor;

    /**
     * {@inheritDoc}
     */
    @Override
    public LatticeGraph computeLatticeGraphPositions(LatticeGraph graph, int screenWidth, int screenHeight) {

        primePointer = -1;
        nodes = graph.getNodes();
        if (nodes.isEmpty()) {
            return graph;
        }
        attractionFactor = ATTRACTION_CONSTANT / Math.sqrt(nodes.size());
        repulsionFactor = REPULSION_CONSTANT / Math.sqrt(nodes.size());
        attraction_I = 0.5 * attractionFactor;
        attraction_II = 3.0 * attractionFactor;
        attraction_III = 0.75 * attractionFactor;
        repulsion_I = 3.0 * repulsionFactor;
        repulsion_II = 0.5 * repulsionFactor;
        repulsion_III = 1.5 * repulsionFactor;
        repititions_I = ITERATIONS + nodes.size() + 30;
        repititions_II = ITERATIONS + nodes.size();
        repititions_III = ITERATIONS + nodes.size() + 20;

        // sort nodes by level
        Collections.sort(nodes);

        Node curr;
        double[] currX = new double[nodes.size()];
        double[] currY = new double[nodes.size()];
        double[] currZ = new double[nodes.size()];
        int level;
        for (int i = 0; i < nodes.size();) {
            curr = nodes.get(i);
            level = curr.getLevel();
            // j will be the number of nodes with the same level as the current
            // one.
            int j;
            for (j = 1; j < nodes.size() - i; j++) {
                if (level != nodes.get(i + j).getLevel()) {
                    break;
                }
            }
            double angle = 2 * Math.PI / j;
            for (int k = 0; k < j; k++) {
                currX[i + k] = j * Math.cos(k * angle + Math.PI / nextPrime());
                currY[i + k] = j * Math.sin(k * angle + Math.PI / nextPrime());
                currZ[i + k] = level;
            }
            i += j;
        }
        
        // Silently improve the diagram through all three stages.
        multipleUpdates(repititions_I, attraction_I, repulsion_I);
        multipleUpdates(repititions_II, attraction_II, repulsion_II);
        multipleUpdates(repititions_III, attraction_III, repulsion_III);
        
        normalize(currX, currY, currZ);

        // project to 2d
        final double cos = Math.cos(0);
        final double sin = Math.sin(0);
        for (int i = 0; i < nodes.size(); i++) {
            curr = nodes.get(i);
            curr.setX((int) ((cos * currX[i] + sin * currY[i]) * (screenWidth / 2)));
            curr.setY((int) (currZ[i] * (screenHeight / 2)));
            curr.positionLabels();
        }
        return graph;
    }

    /**
     * Returns next prime number in list.
     * 
     * @return next prime number in list
     */
    private int nextPrime() {
        primePointer++;
        if (primePointer == PRIMES.length) {
            primePointer = 0;
        }
        return PRIMES[primePointer];
    }

    /**
     * Updates the lattice k times with given attraction and repulsion.
     * 
     * @param k
     *            times to update lattice
     * @param att
     *            attraction for update
     * @param repulsion
     *            repulsion for update
     */
    private void multipleUpdates(int k, double att, double repulsion) {
        for (int i = 0; i < k; i++) {
            update(att, repulsion);
        }
    }

    /**
     * This does a single update using <tt>att</tt> and <tt>repulsion</tt>,
     * improving the diagram.
     * 
     * @param att
     *            attraction for update
     * @param repulsion
     *            repulsion for update
     */
    private void update(double att, double repulsion) {
        // Iterator<Node> list;
        for (int i = 0; i < nodes.size() - 1; i++) {
            // list = nodes.get(i).getFilter().iterator();
            // TODO: uncomment if filters are implemented
            // list.next(); // Skip the first element which is x.
            // while (list.hasNext()) {
            // nodes.get(i).attraction(list.next(), att);
            // }
            // TODO: uncomment if incomparables are implemented
            // list = nodes.get(i).highIncomparables().iterator();
            // while (list.hasNext()) {
            // nodes.get(i).repulsion(list.next(), repulsion);
            // }
        }
        for (int i = 0; i < nodes.size(); i++) {
            nodes.get(i).updateForce();
        }
    }

    /**
     * This does a translation in the x-y plane so that 0 is at the origin. It
     * calculates a scale factor which can be used to get the coords in [0,1]
     * and shifts the coords to [0,1] and to positive coordinates.
     * 
     * @param currX
     *            x coordinates
     * @param currY
     *            y coordinates
     * @param currZ
     *            z coordinates
     */
    private void normalize(double[] currX, double[] currY, double[] currZ) {
        // calculate scale factor
        double x0 = currX[0];
        double y0 = currY[0];
        double distSq;
        double maxSq = 0.0;
        for (int i = 0; i < nodes.size(); i++) {
            currX[i] = currX[i] - x0;
            currY[i] = currY[i] - y0;
            distSq = currX[i] * currX[i] + currY[i] * currY[i];
            if (distSq > maxSq) {
                maxSq = distSq;
            }
        }
        double maxZ = currZ[nodes.size() - 1];
        double scale;
        if (maxZ * maxZ > 4 * maxSq) {
            scale = maxZ;
        } else {
            scale = 2 * Math.sqrt(maxSq);
        }
        // Normalize/Scale the coordinates to be in [0,1] and shifts them to
        // positive coordinates
        if (scale != 0) {
            for (int i = 0; i < nodes.size(); i++) {
                currX[i] = currX[i] / scale + 1;
                currY[i] = currY[i] / scale + 1;
                currZ[i] = currZ[i] / scale + 1;
            }
        }
    }
}
