package fcatools.conexpng.gui;

import fcatools.conexpng.ProgramState;
import fcatools.conexpng.draw.ILatticeAlgorithm;
import fcatools.conexpng.draw.TestLatticeAlgorithm;

import javax.swing.*;
import java.beans.PropertyChangeEvent;


public class LatticeView extends View {
    private static final long serialVersionUID = 1660117627650529212L;

    public static int radius=7;
    private ILatticeAlgorithm alg;

    public LatticeView(ProgramState state) {
        super(state);
        
        alg = new TestLatticeAlgorithm();
        view = new LatticeGraphView(alg.computeLatticeGraph(state.context));
        settings = new JLabel("Lattice Settings");  
        super.init();
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
    	((LatticeGraphView) view).setLatticeGraph(alg.computeLatticeGraph(state.context));
    	view.repaint();
    }

}
