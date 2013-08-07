package fcatools.conexpng.gui.dependencies;

import com.alee.laf.panel.WebPanel;
import com.alee.laf.scroll.WebScrollPane;
import com.alee.laf.splitpane.WebSplitPane;
import com.alee.laf.text.WebTextPane;
import de.tudresden.inf.tcs.fcaapi.FCAImplication;
import fcatools.conexpng.Conf;
import fcatools.conexpng.Conf.ContextChangeEvent;
import fcatools.conexpng.Conf.StatusMessage;
import fcatools.conexpng.ContextChangeEvents;
import fcatools.conexpng.gui.View;
import fcatools.conexpng.model.AssociationRule;
import fcatools.conexpng.model.FormalContext.StemBaseCalculator;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import java.awt.*;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.beans.PropertyChangeEvent;
import java.math.BigDecimal;
import java.util.*;

public class DependencyView extends View {

    private static final long serialVersionUID = -6377834669097012170L;

    private WebTextPane implpane = new WebTextPane();

    private WebTextPane assopane = new WebTextPane();

    private SimpleAttributeSet[] attrs;

    private SimpleAttributeSet header;

    private final int NON_ZERO_SUPPORT_EXACT_RULE = 0;

    private final int INEXACT_RULE = 1;

    private final int ZERO_SUPPORT_EXACT_RULE = 2;

    private final String FOLLOW = " ==> ";

    private final String END_MARK = ";";

    private final String EOL = System.getProperty("line.separator");

    private boolean updateLater = false;

    public DependencyView(final Conf state) {
        super(state);

        attrs = new SimpleAttributeSet[3];
        attrs[NON_ZERO_SUPPORT_EXACT_RULE] = new SimpleAttributeSet();
        StyleConstants.setForeground(attrs[NON_ZERO_SUPPORT_EXACT_RULE], Color.blue);

        attrs[ZERO_SUPPORT_EXACT_RULE] = new SimpleAttributeSet();
        StyleConstants.setForeground(attrs[ZERO_SUPPORT_EXACT_RULE], Color.red);

        attrs[INEXACT_RULE] = new SimpleAttributeSet();
        StyleConstants.setForeground(attrs[INEXACT_RULE], new Color(0, 128, 0));

        header = new SimpleAttributeSet();
        StyleConstants.setFontSize(header, 12);

        implpane.setEditable(false);
        // only for the Splitpanes height
        // implpane.setPreferredSize(new Dimension(300, 200));
        assopane.setEditable(false);

        final WebScrollPane scroll2 = new WebScrollPane(assopane);
        scroll2.getViewport().addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                state.guiConf.assoscrollpos = scroll2.getVerticalScrollBar().getValue();
            }
        });

        final WebScrollPane scroll1 = new WebScrollPane(implpane);
        scroll1.getViewport().addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                state.guiConf.implscrollpos = scroll1.getVerticalScrollBar().getValue();
            }
        });
        final WebSplitPane splitPane = new WebSplitPane(WebSplitPane.VERTICAL_SPLIT, scroll1, scroll2);
        splitPane.setOneTouchExpandable(true);
        splitPane.setContinuousLayout(true);
        splitPane.addDividerListener(new ComponentListener() {
            @Override
            public void componentShown(ComponentEvent arg0) {
            }

            @Override
            public void componentResized(ComponentEvent arg0) {
            }

            @Override
            public void componentMoved(ComponentEvent arg0) {
                state.guiConf.splitpanepos = splitPane.getDividerLocation();
            }

            @Override
            public void componentHidden(ComponentEvent arg0) {
            }
        });
        view = splitPane;
        splitPane.setDividerLocation(state.guiConf.splitpanepos);

        settings = new WebPanel(new BorderLayout());

        settings.add(new DependencySettings(state.guiConf), BorderLayout.NORTH);
        settings.getComponent(0).addPropertyChangeListener(this);
        settings.setMinimumSize(new Dimension(190, 400));
        toolbar = null;
        super.init();
        this.splitPane.setDividerLocation(state.guiConf.dependenciessettingssplitpos);
        this.splitPane.addDividerListener(new ComponentListener() {
            @Override
            public void componentShown(ComponentEvent arg0) {
            }

            @Override
            public void componentResized(ComponentEvent arg0) {
            }

            @Override
            public void componentMoved(ComponentEvent arg0) {
                state.guiConf.dependenciessettingssplitpos = DependencyView.this.splitPane.getDividerLocation();
            }

            @Override
            public void componentHidden(ComponentEvent arg0) {
            }
        });
    }

    private void writeAssociations(final int assoscrollpos) {
        if (state.associations!= null)
            SwingUtilities.invokeLater(new Runnable() {

                @Override
                public void run() {
                    int i = 0;
                    StringBuffer buf;
                    assopane.setText("");
                    ArrayList<AssociationRule> t = new ArrayList<>(state.associations);

                    if (!state.guiConf.lexsorting) {
                        Collections.sort(t, new Comparator<AssociationRule>() {
                            @Override
                            public int compare(AssociationRule o1, AssociationRule o2) {
                                int support1 = state.context.supportCount(o1.getPremise());
                                int support2 = state.context.supportCount(o2.getPremise());

                                if (support1 == support2)
                                    return (int) (o2.getConfidence() - o1.getConfidence());
                                else
                                    return support2 - support1;
                            }
                        });
                    }
                    try {
                        assopane.getDocument().insertString(assopane.getDocument().getLength(),
                                "Associations (Luxenburger Base)\n", header);
                        i = 0;
                        for (AssociationRule asso : t) {
                            buf = new StringBuffer();
                            if (asso.getConfidence() >= state.guiConf.confidence) {
                                i++;
                                buf.append(i);
                                buf.append("< " + state.context.supportCount(asso.getPremise()) + " > ");
                                buf.append(asso.getPremise()
                                        + " =["
                                        + new BigDecimal(asso.getConfidence()).setScale(3, BigDecimal.ROUND_HALF_UP)
                                                .doubleValue() + "]=> ");
                                Set<String> temp = new HashSet<>(asso.getConsequent());
                                temp.addAll(asso.getPremise());
                                buf.append("< " + state.context.supportCount(temp) + " > " + asso.getConsequent()
                                        + END_MARK);
                                buf.append(EOL);

                                assopane.getDocument().insertString(assopane.getDocument().getLength(), buf.toString(),
                                        implicationStyle(asso.getSupport(), asso.getConfidence()));
                            }
                        }
                        ((WebScrollPane) ((WebSplitPane) view).getBottomComponent()).getVerticalScrollBar().setValue(
                                assoscrollpos);
                        ((DependencySettings) settings.getComponent(0)).update(i, state.associations.size());
                    } catch (BadLocationException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            });
    }

    private void writeImplications(final int implscrollpos) {
        if (state.implications != null)
            SwingUtilities.invokeLater(new Runnable() {

                @Override
                public void run() {
                    int i = 0;
                    StringBuffer buf;
                    int support = 0;
                    implpane.setText("");
                    ArrayList<FCAImplication<String>> z = new ArrayList<>(state.implications);
                    if (!state.guiConf.lexsorting) {
                        Collections.sort(z, new Comparator<FCAImplication<String>>() {

                            @Override
                            public int compare(FCAImplication<String> o1, FCAImplication<String> o2) {

                                return Integer.compare(state.context.supportCount(o2.getPremise()),
                                        state.context.supportCount(o1.getPremise()));
                            }
                        });
                    }
                    try {
                        implpane.getDocument().insertString(implpane.getDocument().getLength(),
                                "Implications (Duquenne-Guigues Base/Stem Base)\n", header);

                        for (FCAImplication<String> impl : z) {
                            support = state.context.supportCount(impl.getPremise());
                            buf = new StringBuffer();
                            buf.append(i);
                            buf.append("< " + support + " > ");
                            buf.append(impl.getPremise() + FOLLOW + impl.getConclusion() + END_MARK);
                            buf.append(EOL);
                            i++;

                            implpane.getDocument().insertString(implpane.getDocument().getLength(), buf.toString(),
                                    implicationStyle(support, 1));
                        }
                        ((WebScrollPane) ((WebSplitPane) view).getTopComponent()).getVerticalScrollBar().setValue(
                                implscrollpos);
                    } catch (BadLocationException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            });
    }

    public SimpleAttributeSet implicationStyle(double support, double confidence) {
        if (confidence == 1.0) {
            return support > 0 ? attrs[NON_ZERO_SUPPORT_EXACT_RULE] : attrs[ZERO_SUPPORT_EXACT_RULE];
        }
        return attrs[INEXACT_RULE];
    }

    private AssociationWorker associationWorker;
    private ImplicationWorker implicationWorker;

    private void updateAssociations(final boolean withImplications) {
        if (withImplications) {
            if (implicationWorker != null && !implicationWorker.isDone()) {
                implicationWorker.cancel(true);
            }
            implicationWorker = new ImplicationWorker();
            implicationWorker.execute();
        }
        if (associationWorker != null && !associationWorker.isDone()) {
            associationWorker.cancel(true);
        }
        associationWorker = new AssociationWorker();
        associationWorker.execute();
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt instanceof ContextChangeEvent) {
            ContextChangeEvent cce = (ContextChangeEvent) evt;
            if (cce.getName() == ContextChangeEvents.CANCELCALCULATIONS) {
                if (associationWorker != null)
                    associationWorker.cancel(true);
                if (implicationWorker != null)
                    implicationWorker.cancel(true);
            }
            if (cce.getName() == ContextChangeEvents.NEWCONTEXT || cce.getName() == ContextChangeEvents.CONTEXTCHANGED)
                updateLater = true;

            if (cce.getName() == ContextChangeEvents.LOADEDFILE) {
                if (state.associations != null) {
                    writeAssociations(state.guiConf.assoscrollpos);
                    writeImplications(state.guiConf.implscrollpos);
                    updateLater = false;
                } else
                    updateLater = true;
            }
        }
        if (isVisible() && updateLater) {

            updateLater = false;
            state.associations = new TreeSet<AssociationRule>();
            state.implications = new HashSet<FCAImplication<String>>();
            updateAssociations(true);
            return;
        }
        if (evt.getPropertyName().equals("ConfidenceChanged")) {
            writeAssociations(0);
        } else if (evt.getPropertyName().equals("MinimalSupportChanged")) {
            updateAssociations(false);
        } else if (evt.getPropertyName().equals("ToggleSortingOrder")) {
            writeAssociations(0);
            writeImplications(0);
        }

    }

    private class ImplicationWorker extends SwingWorker<Void, Void> {

        protected Void doInBackground() throws Exception {
            state.startCalculation(StatusMessage.CALCULATINGIMPLICATIONS);
            StemBaseCalculator sbc = state.context.new StemBaseCalculator();
            Thread t = new Thread(sbc);
            t.setPriority(Thread.MIN_PRIORITY);
            t.start();
            while (t.isAlive()) {
                if (isCancelled()) {
                    t.interrupt();
                    t.join();
                    System.out.println("canceledImplications");
                    break;
                }
            }
            if (!isCancelled()) {
                state.implications = sbc.getResult();
            }
            return null;
        }

        protected void done() {
            if (!isCancelled()) {
                writeImplications(0);
            }
            state.endCalculation(StatusMessage.CALCULATINGIMPLICATIONS);
        };
    }

    private class AssociationWorker extends SwingWorker<Void, Void> {

        protected Void doInBackground() throws Exception {
            state.startCalculation(StatusMessage.CALCULATINGASSOCIATIONS);

            ThreadedAssociationMiner tam = new ThreadedAssociationMiner(state.context, state.guiConf.support, 0);
            Thread t = new Thread(tam);
            t.setPriority(Thread.MIN_PRIORITY);
            t.start();
            while (t.isAlive()) {
                if (isCancelled()) {
                    t.interrupt();
                    t.join();
                    System.out.println("canceledAssociations");
                    break;
                }
            }
            if (!isCancelled()) {
                state.associations = tam.getResult();
            }
            return null;
        }

        protected void done() {
            if (!isCancelled()) {
                writeAssociations(0);
            }
            state.endCalculation(StatusMessage.CALCULATINGASSOCIATIONS);
        };
    }

}
