package fcatools.conexpng.gui.lattice;

import fcatools.conexpng.ProgramState;
import fcatools.conexpng.Util;
import fcatools.conexpng.gui.View;
import fcatools.conexpng.model.ILatticeAlgorithm;
import fcatools.conexpng.model.TestLatticeAlgorithm;

import javax.swing.*;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;



public class LatticeView extends View {
    private static final long serialVersionUID = 1660117627650529212L;

    public static int radius=7;
    private ILatticeAlgorithm alg;

    public LatticeView(ProgramState state) {
        super(state);
        
        alg = new TestLatticeAlgorithm();
        view = new LatticeGraphView(alg.computeLatticeGraph(state.context));
        settings = new AccordionMenue();
        
        JButton export = Util.createButton("Export as .PDF", "export",
                "conexp/cameraFlash.gif");
        export.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				((LatticeGraphView) view).exportLatticeAsPDF();
				
			}
		});
        toolbar.add(export);
        
        JToggleButton  move = Util.createToggleButton("Move subgraph", "move",
                "conexp/moveMode.gif");
        move.addActionListener(new ActionListener() {
			boolean last = false;
			@Override
			public void actionPerformed(ActionEvent e) {
				last = !last;
				((LatticeGraphView) view).setMove(last);
				
			}
		});
        toolbar.add(move);
        
        super.init();
        
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
    	((LatticeGraphView) view).setLatticeGraph(alg.computeLatticeGraph(state.context));
    	view.repaint();
    }

}
