package fcatools.conexpng.gui.lattice;

import fcatools.conexpng.ContextChangeEvents;
import fcatools.conexpng.ProgramState;
import fcatools.conexpng.ProgramState.ContextChangeEvent;
import fcatools.conexpng.Util;
import fcatools.conexpng.gui.View;
import fcatools.conexpng.model.ILatticeAlgorithm;
import fcatools.conexpng.model.TestLatticeAlgorithm;

import javax.swing.*;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;

public class LatticeView extends View {
    private static final long serialVersionUID = 1660117627650529212L;

    public static int radius = 7;
    private ILatticeAlgorithm alg;

    private boolean updateLater;

    public LatticeView(ProgramState state) {
        super(state);

        alg = new TestLatticeAlgorithm();
        LatticeGraph graph = alg.computeLatticeGraph(state.context);
        view = new LatticeGraphView(graph, state);
        settings = new AccordionMenue(state);

        JButton export = Util.createButton("Export as .PDF", "export",
                "conexp/cameraFlash.gif");
        export.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                ((LatticeGraphView) view).exportLatticeAsPDF();

            }
        });
        toolbar.add(export);

        JToggleButton move = Util.createToggleButton("Move subgraph", "move",
                "conexp/moveMode.gif");
        move.addActionListener(new ActionListener() {
            private boolean last = false;

            @Override
            public void actionPerformed(ActionEvent e) {
                last = !last;
                ((LatticeGraphView) view).setMove(last);

            }
        });
        toolbar.add(move);
        
        JToggleButton showIdeal = Util.createToggleButton("Show Ideale", "ideal",
                "conexp/contextIcon.gif");
        showIdeal.addActionListener(new ActionListener() {
        	private boolean last = false;
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				last = !last;
				((LatticeGraphView) view).idealHighlighting(last);
				
			}
		});       
        toolbar.add(showIdeal);
        

        super.init();

    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        // TODO: I would not use ContextChangeEvents for communicating between
        // the Latticeview and the AccordionMenue
		if (evt instanceof ContextChangeEvent
				&& (((ContextChangeEvent) evt).getName() == ContextChangeEvents.CONTEXTCHANGED
				|| ((ContextChangeEvent) evt).getName() == ContextChangeEvents.NEWCONTEXT)) {
			updateLater = true;
            return;
        }
        if (isVisible() && updateLater) {
        	updateLater=false;
            state.startCalculation("Calculating the Dependencies");
            new SwingWorker<Object, Object>() {

                @Override
                protected Object doInBackground() throws Exception {
                    ((LatticeGraphView) view).setLatticeGraph(alg
                            .computeLatticeGraph(state.context));
                    return null;
                }

                protected void done() {
                    state.endCalculation();
                };
            }.execute();

            ((AccordionMenue) settings).update();
            
        }
        if(evt instanceof ContextChangeEvent && (((ContextChangeEvent) evt).getName() == ContextChangeEvents.TEMPORARYCONTEXTCHANGED)){
        	((LatticeGraphView) view).setLatticeGraph(alg
                    .computeLatticeGraph(state.context));
        }
        view.repaint();
    }
}