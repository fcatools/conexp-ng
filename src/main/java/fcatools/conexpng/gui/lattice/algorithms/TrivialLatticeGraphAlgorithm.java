package fcatools.conexpng.gui.lattice.algorithms;

import fcatools.conexpng.gui.lattice.LatticeGraph;

public class TrivialLatticeGraphAlgorithm implements ILatticeGraphAlgorithm {

    /**
     * {@inheritDoc}
     */
    @Override
    public LatticeGraph computeLatticeGraphPositions(LatticeGraph graph, int screenWidth, int screenHeight) {
        return graph;
    }

}
