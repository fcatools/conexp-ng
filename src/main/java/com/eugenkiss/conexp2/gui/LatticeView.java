package com.eugenkiss.conexp2.gui;

import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JLabel;

import com.eugenkiss.conexp2.ProgramState;
import com.eugenkiss.conexp2.draw.LatticeGraph;
import com.eugenkiss.conexp2.draw.Node;

public class LatticeView extends View {
    private static final long serialVersionUID = 1660117627650529212L;

    public LatticeView(ProgramState state) {
        super(state);

        settings = new JLabel("Lattice Settings");
        List<Node >nodes = new ArrayList<>();
        nodes.add(new Node(null, null, 0, 50));
        nodes.add(new Node(null, null, 100, 50));
        nodes.add(new Node(null, null, 200, 50));
        nodes.add(new Node(null, null, 150, 150));
        view = new LatticeGraphView(new LatticeGraph(nodes, null));
        super.init();
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        // TODO Auto-generated method stub

    }

}
