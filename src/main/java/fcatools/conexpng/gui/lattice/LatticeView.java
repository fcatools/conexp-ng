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

import com.alee.laf.optionpane.WebOptionPane;
import com.alee.laf.rootpane.WebDialog;

import de.tudresden.inf.tcs.fcaapi.Concept;
import de.tudresden.inf.tcs.fcalib.FullObject;
import de.tudresden.inf.tcs.fcalib.utils.ListSet;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.beans.PropertyChangeEvent;
import java.io.File;

public class LatticeView extends View {
    private static final long serialVersionUID = 1660117627650529212L;

    public static int radius = 7;
    private ILatticeAlgorithm alg;

    private boolean updateLater;

    public LatticeView(final Conf state) {
        super(state);

        alg = new TestLatticeAlgorithm();
        LatticeGraph graph = alg.computeLatticeGraph(new ListSet<Concept<String, FullObject<String, String>>>(),
                new Rectangle());
        view = new LatticeGraphView(graph, state);
        settings = new LatticeSettings(state);
        settings.setMinimumSize(new Dimension(170, 400));

        JButton export = Util.createButton("Export as .PDF", "export", "conexp/cameraFlash.gif");
        export.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                final JFileChooser fc = new JFileChooser(state.filePath);
                final WebDialog dialog = new WebDialog();
                dialog.setContentPane(fc);
                fc.setDialogType(JFileChooser.SAVE_DIALOG);
                fc.addActionListener(new ActionListener() {

                    @Override
                    public void actionPerformed(ActionEvent e) {
                        String command = (String) e.getActionCommand();
                        if (command.equals(JFileChooser.CANCEL_SELECTION)) {
                            dialog.setVisible(false);
                            return;
                        } else if (command.equals(JFileChooser.APPROVE_SELECTION)) {
                            File file = fc.getSelectedFile();
                            String path = file.getAbsolutePath();
                            if (file.exists()) {
                                WebOptionPane pane = new WebOptionPane(
                                        new JLabel("File already exists. Do you really want to overwrite "
                                                + file.getName() + "?"), JOptionPane.YES_NO_OPTION);
                                pane.setMessageType(WebOptionPane.QUESTION_MESSAGE);
                                Object[] options = { "Yes", "No" };
                                pane.setOptions(options);
                                JDialog dialog2 = pane.createDialog("Overwriting existing file?");
                                dialog2.pack();
                                dialog2.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
                                String n = (String) pane.getValue();
                                dialog2.setVisible(true);
                                // TODO
                                if (n.equals("Yes")) {
                                    ((LatticeGraphView) view).exportLattice(path);
                                    dialog.setVisible(false);
                                } else {
                                }
                            } else {
                                ((LatticeGraphView) view).exportLattice(path);
                                dialog.setVisible(false);
                            }
                        }
                    }

                });
                dialog.pack();
                dialog.setVisible(true);

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
    
    boolean loadedfile = false;
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        // TODO: I would not use ContextChangeEvents for communicating between
        // the Latticeview and the AccordionMenue
        if (evt instanceof ContextChangeEvent
                && (((ContextChangeEvent) evt).getName() == ContextChangeEvents.CONTEXTCHANGED || ((ContextChangeEvent) evt)
                        .getName() == ContextChangeEvents.NEWCONTEXT)) {
            updateLater = true;
        }
        
        if (evt instanceof ContextChangeEvent
                && (((ContextChangeEvent) evt).getName() == ContextChangeEvents.LOADEDFILE)) {

            if (state.lattice.isEmpty())
                updateLater = true;
            else
                loadedfile = true;
        }
        if (isVisible() && loadedfile) {
            loadedfile=false;
            if (state.lattice.missingEdges()) {
                if (state.concepts.isEmpty())
                    state.concepts = state.context.getConceptsWithoutConsideredElements();
                System.out.println(state.concepts);
                state.lattice.addEdges(state.concepts);
            }
            ((LatticeGraphView) view).setLatticeGraph(state.lattice);
        }
        if (isVisible() && updateLater) {
            updateLater = false;
            state.startCalculation(StatusMessage.CALCULATINGLATTICE);
            new SwingWorker<Void, Object>() {

                @Override
                protected Void doInBackground() throws Exception {
                    state.concepts = state.context.getConcepts();
                    state.lattice = alg.computeLatticeGraph(state.concepts, view.getBounds());
                    ((LatticeGraphView) view).setLatticeGraph(state.lattice);
                    return null;
                }

                protected void done() {
                    state.endCalculation(StatusMessage.CALCULATINGLATTICE);
                };
            }.execute();

            ((LatticeSettings) settings).update();

        }
        if (evt instanceof ContextChangeEvent
                && (((ContextChangeEvent) evt).getName() == ContextChangeEvents.TEMPORARYCONTEXTCHANGED)) {
            state.concepts = state.context.getConceptsWithoutConsideredElements();
            state.lattice = alg.computeLatticeGraph(state.concepts, view.getBounds());
            ((LatticeGraphView) view).setLatticeGraph(state.lattice);
        }
        view.repaint();
    }
}
