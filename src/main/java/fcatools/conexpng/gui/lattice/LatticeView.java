package fcatools.conexpng.gui.lattice;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.io.File;
import java.util.HashSet;

import javax.swing.JButton;
import javax.swing.JToggleButton;
import javax.swing.Timer;

import com.alee.laf.button.WebButton;
import com.alee.laf.filechooser.WebFileChooser;
import com.alee.laf.rootpane.WebDialog;

import de.tudresden.inf.tcs.fcaapi.Concept;
import de.tudresden.inf.tcs.fcalib.FullObject;
import de.tudresden.inf.tcs.fcalib.utils.ListSet;
import fcatools.conexpng.Conf;
import fcatools.conexpng.Conf.ContextChangeEvent;
import fcatools.conexpng.Util;
import fcatools.conexpng.gui.MainFrame;
import fcatools.conexpng.gui.MainFrame.OverwritingFileDialog;
import fcatools.conexpng.gui.StatusBarPropertyChangeListener;
import fcatools.conexpng.gui.View;
import fcatools.conexpng.gui.workers.ConceptWorker;
import fcatools.conexpng.model.LatticeGraphComputer;

/**
 * This class implements the lattice tab. It contains the lattice graph view and
 * the accordion menu. Furthermore, in this class the action buttons there
 * functionallties are implemented and added to the vertical toolbar.
 * 
 */
public class LatticeView extends View {
    private static final long serialVersionUID = 1660117627650529212L;

    public static int radius = 7;
    public static double zoomFactor = 1;
    private LatticeGraphComputer alg;
    private MainFrame mainFrame;

    private boolean updateLater;

    public LatticeView(final Conf state, MainFrame mainframe) {
        super(state);
        this.mainFrame = mainframe;
        alg = new LatticeGraphComputer();
        if (state.lattice.isEmpty()) {
            state.lattice = alg.computeLatticeGraph(new ListSet<Concept<String, FullObject<String, String>>>(),
                    new Rectangle(800, 600));
        }
        view = new LatticeGraphView(state);
        LatticeViewInteractions interactions = new LatticeViewInteractions();
        view.addMouseListener(interactions);
        view.addMouseMotionListener(interactions);
        view.addMouseWheelListener(interactions);
        settings = new LatticeSettings(state);
        settings.setMinimumSize(new Dimension(170, 400));

        JButton export = Util.createButton("Export", "export", "conexp/cameraFlash.gif");
        export.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                final WebFileChooser fc = new WebFileChooser();
                fc.setCurrentDirectory(state.filePath.substring(0,
                        state.filePath.lastIndexOf(System.getProperty("file.separator"))));
                final WebDialog dialog = new WebDialog(mainFrame, "Save file as", true);
                dialog.setContentPane(fc);
                fc.setMultiSelectionEnabled(false);
                fc.setAcceptAllFileFilterUsed(false);
                fc.setFileSelectionMode(WebFileChooser.FILES_ONLY);
                fc.setDialogType(WebFileChooser.SAVE_DIALOG);
                fc.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        String state = (String) e.getActionCommand();
                        if ((state.equals(WebFileChooser.APPROVE_SELECTION) && fc.getSelectedFile() != null)) {
                            File file = fc.getSelectedFile();
                            String path = file.getAbsolutePath();
                            if (file.exists()) {
                                OverwritingFileDialog ofd = mainFrame.new OverwritingFileDialog(file);
                                if (ofd.isYes()) {
                                    ((LatticeGraphView) view).exportLattice(path);
                                    dialog.setVisible(false);
                                }
                            } else {
                                ((LatticeGraphView) view).exportLattice(path);
                                dialog.setVisible(false);
                            }
                        } else if (state.equals(WebFileChooser.CANCEL_SELECTION)) {
                            dialog.setVisible(false);
                            return;
                        }
                    }
                });
                dialog.pack();
                Util.centerDialogInsideMainFrame(mainFrame, dialog);
                dialog.setVisible(true);

                if (fc.getSelectedFile() == null)
                    return;

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

        WebButton centerGraph = Util.createButton("Center Graph", "centerGraph",
                "icons/lattice-view/transform-scale.png");
        centerGraph.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                LatticeGraphView.setOffset(0, 0);
                ((LatticeGraphView) view).repaint();
            }
        });
        toolbar.add(centerGraph);

        WebButton panUp = Util.createButton("Pan Up", "panUp", "icons/lattice-view/draw-triangle3.png");
        panUp.addMouseListener(new MouseAdapter() {
            Timer timer;

            @Override
            public void mouseReleased(MouseEvent e) {
                timer.stop();
            }
            @Override
            public void mousePressed(MouseEvent e) {
                // timer used to fire the event as long as the mouse button is
                // pressed
                timer = new Timer(0, new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent arg0) {
                        LatticeGraphView v = ((LatticeGraphView) view);
                        int offsetX = (int) LatticeGraphView.getOffset().getX();
                        int offsetY = (int) LatticeGraphView.getOffset().getY();
                        LatticeGraphView.setOffset(offsetX, offsetY - 1);
                        v.repaint();
                    }
                });
                timer.start();
            }
        });
        toolbar.add(panUp);

        WebButton panDown = Util.createButton("Pan Down", "panDown", "icons/lattice-view/draw-triangle4.png");
        panDown.addMouseListener(new MouseAdapter() {
            Timer timer;

            @Override
            public void mouseReleased(MouseEvent e) {
                timer.stop();
            }
            @Override
            public void mousePressed(MouseEvent e) {
                // timer used to fire the event as long as the mouse button is
                // pressed
                timer = new Timer(0, new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent arg0) {
                        LatticeGraphView v = ((LatticeGraphView) view);
                        int offsetX = (int) LatticeGraphView.getOffset().getX();
                        int offsetY = (int) LatticeGraphView.getOffset().getY();
                        LatticeGraphView.setOffset(offsetX, offsetY + 1);
                        v.repaint();
                    }
                });
                timer.start();
            }
        });
        toolbar.add(panDown);

        WebButton panLeft = Util.createButton("Pan Left", "panLeft", "icons/lattice-view/draw-triangle1.png");
        panLeft.addMouseListener(new MouseAdapter() {
            Timer timer;

            @Override
            public void mouseReleased(MouseEvent e) {
                timer.stop();
            }
            @Override
            public void mousePressed(MouseEvent e) {
                // timer used to fire the event as long as the mouse button is
                // pressed
                timer = new Timer(0, new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent arg0) {
                        LatticeGraphView v = ((LatticeGraphView) view);
                        int offsetX = (int) LatticeGraphView.getOffset().getX();
                        int offsetY = (int) LatticeGraphView.getOffset().getY();
                        LatticeGraphView.setOffset(offsetX - 1, offsetY);
                        v.repaint();
                    }
                });
                timer.start();
            }
        });
        toolbar.add(panLeft);

        WebButton panRight = Util.createButton("Pan Right", "panRight", "icons/lattice-view/draw-triangle2.png");
        panRight.addMouseListener(new MouseAdapter() {
            Timer timer;

            @Override
            public void mouseReleased(MouseEvent e) {
                timer.stop();
            }
            @Override
            public void mousePressed(MouseEvent e) {
                // timer used to fire the event as long as the mouse button is
                // pressed
                timer = new Timer(0, new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent arg0) {
                        LatticeGraphView v = ((LatticeGraphView) view);
                        int offsetX = (int) LatticeGraphView.getOffset().getX();
                        int offsetY = (int) LatticeGraphView.getOffset().getY();
                        LatticeGraphView.setOffset(offsetX + 1, offsetY);
                        v.repaint();
                    }
                });
                timer.start();
            }
        });
        toolbar.add(panRight);

        WebButton zoomOriginal = Util.createButton("Original Zoom", "zoomOriginal",
                "icons/lattice-view/zoom-original.png");
        zoomOriginal.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                LatticeView.zoomFactor = 1;
                ((LatticeGraphView) view).repaint();
            }
        });
        toolbar.add(zoomOriginal);

        WebButton zoomIn = Util.createButton("Zoom In", "zoomIn", "icons/lattice-view/zoom-in-2.png");
        zoomIn.addMouseListener(new MouseAdapter() {
            Timer timer;

            @Override
            public void mouseReleased(MouseEvent e) {
                timer.stop();
            }
            @Override
            public void mousePressed(MouseEvent e) {
                // timer used to fire the event as long as the mouse button is
                // pressed
                timer = new Timer(0, new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent arg0) {
                        LatticeView.zoomFactor += 0.01;
                        // reset zoom factor to 0 if too low
                        if (LatticeView.zoomFactor < 0) {
                            LatticeView.zoomFactor = 0;
                        }
                        ((LatticeGraphView) view).repaint();
                    }
                });
                timer.start();
            }
        });
        toolbar.add(zoomIn);

        WebButton zoomOut = Util.createButton("Zoom Out", "zoomOut", "icons/lattice-view/zoom-out-2.png");
        zoomOut.addMouseListener(new MouseAdapter() {
            Timer timer;

            @Override
            public void mouseReleased(MouseEvent e) {
                timer.stop();
            }
            @Override
            public void mousePressed(MouseEvent e) {
                // timer used to fire the event as long as the mouse button is
                // pressed
                timer = new Timer(0, new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent arg0) {
                        LatticeView.zoomFactor -= 0.01;
                        // reset zoom factor to 0 if too low
                        if (LatticeView.zoomFactor < 0) {
                            LatticeView.zoomFactor = 0;
                        }
                        ((LatticeGraphView) view).repaint();
                    }
                });
                timer.start();
            }
        });
        toolbar.add(zoomOut);

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

    /**
     * Returns the used lattice graph algorithm.
     * 
     * @return lattice graph algorithm
     */
    public LatticeGraphComputer getAlg() {
        return alg;
    }

    boolean loadedfile = false;
    ConceptWorker cc;

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt instanceof ContextChangeEvent) {
            ContextChangeEvent cce = (ContextChangeEvent) evt;
            switch (cce.getName()) {
            case CANCELCALCULATIONS: {
                if (cc != null)
                    cc.cancel(true);
                break;
            }
            case CONTEXTCHANGED: {
                state.concepts = new HashSet<>();
                state.lattice = new LatticeGraph();
                view.repaint();
                updateLater = true;
                break;
            }
            case NEWCONTEXT: {
                state.concepts = new HashSet<>();
                state.lattice = new LatticeGraph();
                view.repaint();
                updateLater = true;
                break;
            }
            case LOADEDFILE: {
                if (state.lattice.isEmpty())
                    updateLater = true;
                else
                    loadedfile = true;
                break;
            }
            case TEMPORARYCONTEXTCHANGED: {
                if (cc != null && !cc.isDone()) {
                    cc.cancel(true);
                }
                Long progressBarId = state.getStatusBar().startCalculation();
                cc = new ConceptWorker(this, true, progressBarId);
                cc.addPropertyChangeListener(new StatusBarPropertyChangeListener(progressBarId, state.getStatusBar()));
                cc.execute();
                break;
            }
            default:
                break;
            }
        }

        if (isVisible() && loadedfile) {
            loadedfile = false;
            updateLater = false;
            if (state.lattice.missingEdges()) {
                if (state.concepts.isEmpty()) {
                    Long progressBarId = state.getStatusBar().startCalculation();
                    ConceptWorker coca = new ConceptWorker(this, false, progressBarId);
                    coca.addPropertyChangeListener(new StatusBarPropertyChangeListener(progressBarId, state
                            .getStatusBar()));
                    coca.execute();
                } else {
                    state.lattice.addEdges(state.concepts);
                    ((LatticeGraphView) view).updateLatticeGraph();
                }
            }
            ((LatticeSettings) settings).update(state);
        }
        if (isVisible() && updateLater) {
            updateLater = false;
            if (cc != null && !cc.isDone()) {
                cc.cancel(true);
            }
            Long progressBarId = state.getStatusBar().startCalculation();
            cc = new ConceptWorker(this, true, progressBarId);
            cc.addPropertyChangeListener(new StatusBarPropertyChangeListener(progressBarId, state.getStatusBar()));
            cc.execute();
        }
        view.repaint();
    }
}
