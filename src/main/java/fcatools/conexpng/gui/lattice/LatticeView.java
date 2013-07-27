package fcatools.conexpng.gui.lattice;

import fcatools.conexpng.ContextChangeEvents;
import fcatools.conexpng.Conf;
import fcatools.conexpng.Conf.ContextChangeEvent;
import fcatools.conexpng.Conf.StatusMessage;
import fcatools.conexpng.Util;
import fcatools.conexpng.gui.View;
import fcatools.conexpng.model.ILatticeAlgorithm;
import fcatools.conexpng.model.TestLatticeAlgorithm;

import javax.swing.*;

import de.tudresden.inf.tcs.fcaapi.Concept;
import de.tudresden.inf.tcs.fcalib.FullObject;
import de.tudresden.inf.tcs.fcalib.utils.ListSet;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.beans.PropertyChangeEvent;

public class LatticeView extends View {
    private static final long serialVersionUID = 1660117627650529212L;

    public static int radius = 7;
    private ILatticeAlgorithm alg;

    private boolean updateLater;

    public LatticeView(final Conf state) {
        super(state);

        alg = new TestLatticeAlgorithm();
        LatticeGraph graph = alg.computeLatticeGraph(new ListSet<Concept<String, FullObject<String, String>>>());
        view = new LatticeGraphView(graph, state);
        settings = new AccordionMenue(state);
        settings.setMinimumSize(new Dimension(170, 400));

        JButton export = Util.createButton("Export as .PDF", "export", "conexp/cameraFlash.gif");
        export.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                ((LatticeGraphView) view).exportLatticeAsPDF();

            }
        });
        toolbar.add(export);

        JToggleButton move = Util.createToggleButton("Move subgraph", "move", "conexp/moveMode.gif");
        move.addActionListener(new ActionListener() {
            private boolean last = false;

            @Override
            public void actionPerformed(ActionEvent e) {
                last = !last;
                ((LatticeGraphView) view).setMove(last);

            }
        });
        toolbar.add(move);

        JToggleButton showIdeal = Util.createToggleButton("Show Ideale", "ideal", "conexp/contextIcon.gif");
        showIdeal.addActionListener(new ActionListener() {
            private boolean last = false;

            @Override
            public void actionPerformed(ActionEvent arg0) {
                last = !last;
                ((LatticeGraphView) view).idealHighlighting(last);
                ((LatticeGraphView) view).repaint();
            }
        });
        toolbar.add(showIdeal);

        super.init();
        splitPane.setDividerLocation(state.guiConf.latticesettingssplitpos);
        splitPane.addDividerListener(new ComponentListener() {
            @Override
            public void componentShown(ComponentEvent arg0) {
            }

            @Override
            public void componentResized(ComponentEvent arg0) {
            }

            @Override
            public void componentMoved(ComponentEvent arg0) {
                state.guiConf.latticesettingssplitpos = splitPane.getDividerLocation();
            }

            @Override
            public void componentHidden(ComponentEvent arg0) {
            }
        });
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        // TODO: I would not use ContextChangeEvents for communicating between
        // the Latticeview and the AccordionMenue
        if (evt instanceof ContextChangeEvent
                && (((ContextChangeEvent) evt).getName() == ContextChangeEvents.CONTEXTCHANGED || ((ContextChangeEvent) evt)
                        .getName() == ContextChangeEvents.NEWCONTEXT)) {
            updateLater = true;
        }
        if (isVisible() && updateLater) {
            updateLater = false;
            state.startCalculation(StatusMessage.CALCULATINGLATTICE);
            new SwingWorker<Void, Object>() {

                @Override
                protected Void doInBackground() throws Exception {
                    ((LatticeGraphView) view).setLatticeGraph(alg.computeLatticeGraph(state.context.getConcepts()));
                    return null;
                }

                protected void done() {
                    state.endCalculation(StatusMessage.CALCULATINGLATTICE);
                };
            }.execute();

            ((AccordionMenue) settings).update();

        }
        if (evt instanceof ContextChangeEvent
                && (((ContextChangeEvent) evt).getName() == ContextChangeEvents.TEMPORARYCONTEXTCHANGED)) {
            ((LatticeGraphView) view).setLatticeGraph(alg.computeLatticeGraph(state.context
                    .getConceptsWithoutConsideredElementa()));
        }
        if (evt instanceof ContextChangeEvent
                && (((ContextChangeEvent) evt).getName() == ContextChangeEvents.LOADEDFILE)) {
            ((LatticeGraphView) view).setLatticeGraph((LatticeGraph) evt.getNewValue());
        }
        view.repaint();
    }
}