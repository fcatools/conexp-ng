package fcatools.conexpng.gui.lattice;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.util.HashSet;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JToggleButton;
import javax.swing.KeyStroke;
import javax.swing.Timer;

import com.alee.laf.button.WebButton;
import com.alee.laf.scroll.WebScrollPane;

import de.tudresden.inf.tcs.fcaapi.Concept;
import de.tudresden.inf.tcs.fcalib.FullObject;
import de.tudresden.inf.tcs.fcalib.utils.ListSet;
import fcatools.conexpng.Conf;
import fcatools.conexpng.Conf.ContextChangeEvent;
import fcatools.conexpng.GUIConf;
import fcatools.conexpng.Util;
import fcatools.conexpng.gui.MainFrame;
import fcatools.conexpng.gui.StatusBarPropertyChangeListener;
import fcatools.conexpng.gui.View;
import fcatools.conexpng.gui.actions.OpenSaveExportAction;
import fcatools.conexpng.gui.workers.ConceptWorker;
import fcatools.conexpng.io.locale.LocaleHandler;

/**
 * This class implements the lattice tab. It contains the lattice graph view and
 * the accordion menu. Furthermore, in this class the action buttons there
 * functionallties are implemented and added to the vertical toolbar.
 * 
 */
public class LatticeView extends View {
    private static final long serialVersionUID = 1660117627650529212L;

    public static int radius = 7;
    private static double zoomFactor;
    private LatticeGraphComputer alg;
    private MainFrame mainFrame;
    private LatticeGraphView latticeGraphView;
    private JToggleButton showIdealButton;

    private boolean updateLater;

    private static GUIConf guiConf;

    public LatticeView(final Conf state, MainFrame mainframe) {
        super(state);
        guiConf = state.guiConf;
        this.mainFrame = mainframe;
        zoomFactor = state.guiConf.zoomFactor;
        if (state.lattice == null || state.lattice.isEmpty()) {
            state.lattice = LatticeGraphComputer.computeLatticeGraph(
                    new ListSet<Concept<String, FullObject<String, String>>>(),
                    new Rectangle(800, 600));
        }
        latticeGraphView = new LatticeGraphView(state);
        view = new WebScrollPane(latticeGraphView);
        LatticeViewInteractions interactions = new LatticeViewInteractions(state.getLatticeViewUndoManager());
        latticeGraphView.addMouseListener(interactions);
        latticeGraphView.addMouseMotionListener(interactions);
        latticeGraphView.addMouseWheelListener(interactions);
        settingsPanel = new LatticeSettings(state);
        settingsPanel.setMinimumSize(new Dimension(170, 400));

        JButton export = Util.createButton(LocaleHandler.getString("LatticeView.LatticeView.export"), "export",
                "conexp/cameraFlash.gif");
        OpenSaveExportAction exportLatticeAction = new OpenSaveExportAction(mainFrame, state, latticeGraphView);
        export.addActionListener(exportLatticeAction);
        // add shortcut
        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_F12, 0),
                "exportLatticeGraph");
        getActionMap().put("exportLatticeGraph", exportLatticeAction);
        toolbar.add(export);

        JToggleButton move = Util.createToggleButton(LocaleHandler.getString("LatticeView.LatticeView.move"), "move",
                "conexp/moveMode.gif");
        move.addActionListener(new ActionListener() {
            private boolean last = false;

            @Override
            public void actionPerformed(ActionEvent e) {
                last = !last;
                latticeGraphView.setMove(last);

            }
        });
        toolbar.add(move);

        showIdealButton = Util.createToggleButton(LocaleHandler.getString("LatticeView.LatticeView.showIdeal"),
                "ideal", "conexp/contextIcon.gif");
        showIdealButton.addActionListener(new ActionListener() {
            private boolean last = false;

            @Override
            public void actionPerformed(ActionEvent arg0) {
                last = !last;
                state.guiConf.idealHighlighting = last;
                latticeGraphView.repaint();
            }
        });
        toolbar.add(showIdealButton);

        WebButton resetGraphPosition = Util.createButton(
                LocaleHandler.getString("LatticeView.LatticeView.resetGraphPosition"), "resetGraphPosition",
                "icons/jlfgr/Home24.gif");
        resetGraphPosition.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                LatticeGraphView.setOffset(0, 0);
                latticeGraphView.repaint();
            }
        });
        toolbar.add(resetGraphPosition);

        WebButton panUp = Util.createButton(LocaleHandler.getString("LatticeView.LatticeView.panUp"), "panUp",
                "icons/jlfgr/Up24.gif");
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
                        LatticeGraphView v = latticeGraphView;
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

        WebButton panDown = Util.createButton(LocaleHandler.getString("LatticeView.LatticeView.panDown"), "panDown",
                "icons/jlfgr/Down24.gif");
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
                        LatticeGraphView v = latticeGraphView;
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

        WebButton panRight = Util.createButton(LocaleHandler.getString("LatticeView.LatticeView.panRight"), "panRight",
                "icons/jlfgr/Forward24.gif");
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
                        LatticeGraphView v = latticeGraphView;
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

        WebButton panLeft = Util.createButton(LocaleHandler.getString("LatticeView.LatticeView.panLeft"), "panLeft",
                "icons/jlfgr/Back24.gif");
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
                        LatticeGraphView v = latticeGraphView;
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

        WebButton zoomOriginal = Util.createButton(LocaleHandler.getString("LatticeView.LatticeView.zoomOriginal"),
                "zoomOriginal",
 "icons/jlfgr/Zoom24.gif");
        zoomOriginal.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                LatticeView.setZoomFactor(1);
                latticeGraphView.repaint();
            }
        });
        toolbar.add(zoomOriginal);

        WebButton zoomIn = Util.createButton(LocaleHandler.getString("LatticeView.LatticeView.zoomIn"), "zoomIn",
                "icons/jlfgr/ZoomIn24.gif");
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
                        LatticeView.setZoomFactor(LatticeView.getZoomFactor() + 0.01);
                        // reset zoom factor to 0 if too low
                        if (LatticeView.getZoomFactor() < 0) {
                            LatticeView.setZoomFactor(0);
                        }
                        latticeGraphView.repaint();
                    }
                });
                timer.start();
            }
        });
        toolbar.add(zoomIn);

        WebButton zoomOut = Util.createButton(LocaleHandler.getString("LatticeView.LatticeView.zoomOut"), "zoomOut",
                "icons/jlfgr/ZoomOut24.gif");
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
                        LatticeView.setZoomFactor(LatticeView.getZoomFactor() - 0.01);
                        // reset zoom factor to 0 if too low
                        if (LatticeView.getZoomFactor() < 0) {
                            LatticeView.setZoomFactor(0);
                        }
                        latticeGraphView.repaint();
                    }
                });
                timer.start();
            }
        });
        toolbar.add(zoomOut);

        super.init();
        setSplitPanePosition();
        splitPane.addDividerListener(new ComponentListener() {
            @Override
            public void componentShown(ComponentEvent arg0) {
            }

            @Override
            public void componentResized(ComponentEvent arg0) {
            }

            @Override
            public void componentMoved(ComponentEvent arg0) {
                state.guiConf.latticeSettingsSplitPos = splitPane.getDividerLocation();
            }

            @Override
            public void componentHidden(ComponentEvent arg0) {
            }
        });
    }

    /**
     * Set split pane divider position.
     */
    private void setSplitPanePosition() {
        splitPane.setDividerLocation(state.guiConf.latticeSettingsSplitPos);
    }

    /**
     * Returns the used lattice graph algorithm.
     * 
     * @return lattice graph algorithm
     */
    public LatticeGraphComputer getAlg() {
        return alg;
    }

    /**
     * Getter for zoom factor.
     * 
     * @return zoom factor
     */
    public static double getZoomFactor() {
        return zoomFactor;
    }

    /**
     * Setter for zoom factor
     * 
     * @param zoom
     *            new zoom factor
     */
    public static void setZoomFactor(double zoom) {
        // round to two decimal places
        zoomFactor = Math.round(zoom * 100) / 100.0;
        guiConf.zoomFactor = zoomFactor;
    }

    boolean loadedfile = false;
    ConceptWorker cc;

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt instanceof ContextChangeEvent) {
            ContextChangeEvent cce = (ContextChangeEvent) evt;
            switch (cce.getName()) {
            case CANCELCALCULATIONS: {
                if (cc != null) {
                    cc.cancel(true);
                }
                break;
            }
            case CONTEXTCHANGED: {
                state.concepts = new HashSet<>();
                state.lattice = new LatticeGraph();
                latticeGraphView.repaint();
                updateLater = true;
                break;
            }
            case NEWCONTEXT: {
                state.concepts = new HashSet<>();
                state.lattice = new LatticeGraph();
                latticeGraphView.repaint();
                updateLater = true;
                break;
            }
            case LOADEDFILE: {
                if (state.lattice.isEmpty()) {
                    updateLater = true;
                } else {
                    loadedfile = true;
                }
                break;
            }
            case TEMPORARYCONTEXTCHANGED: {
                if (cc != null && !cc.isDone()) {
                    cc.cancel(true);
                }
                Long progressBarId = state.getStatusBar().startCalculation();
                cc = new ConceptWorker(this, true, progressBarId);
                cc.addPropertyChangeListener(new StatusBarPropertyChangeListener(progressBarId, state.getStatusBar()));
                state.getStatusBar().addCalculation(progressBarId, cc);
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
                    state.getStatusBar().addCalculation(progressBarId, coca);
                    coca.execute();
                } else {
                    state.lattice.addEdges(state.concepts);
                    updateLatticeGraph();
                }
            }
        }
        if (isVisible() && updateLater) {
            updateLater = false;
            if (cc != null && !cc.isDone()) {
                cc.cancel(true);
            }
            Long progressBarId = state.getStatusBar().startCalculation();
            cc = new ConceptWorker(this, true, progressBarId);
            cc.addPropertyChangeListener(new StatusBarPropertyChangeListener(progressBarId, state.getStatusBar()));
            state.getStatusBar().addCalculation(progressBarId, cc);
            cc.execute();
        }
        latticeGraphView.repaint();
    }

    /**
     * Updates lattice graph.
     */
    public void updateLatticeGraph() {
        latticeGraphView.updateLatticeGraph();
    }

    /**
     * Updates the GUI after a new GUIConf is loaded.
     */
    public void updateGUI() {
        // refresh guiConf in static scope
        guiConf = state.guiConf;
        setZoomFactor(state.guiConf.zoomFactor);
        LatticeGraphView.setOffset(state.guiConf.xOffset, state.guiConf.yOffset);
        setSplitPanePosition();
        showIdealButton.setSelected(state.guiConf.idealHighlighting);
        ((LatticeSettings) settingsPanel).update(state);
    }
}
