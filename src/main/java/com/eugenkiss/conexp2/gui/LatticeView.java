package com.eugenkiss.conexp2.gui;

import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.JLabel;

import com.eugenkiss.conexp2.ProgramState;
import com.eugenkiss.conexp2.draw.Edge;
import com.eugenkiss.conexp2.draw.ILatticeAlgorithm;
import com.eugenkiss.conexp2.draw.LatticeGraph;
import com.eugenkiss.conexp2.draw.Node;
import com.eugenkiss.conexp2.draw.TestLatticeAlgorithm;
import com.eugenkiss.conexp2.model.FormalContext;


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
