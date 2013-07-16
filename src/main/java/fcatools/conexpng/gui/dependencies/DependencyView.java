package fcatools.conexpng.gui.dependencies;

import de.tudresden.inf.tcs.fcaapi.FCAImplication;
import de.tudresden.inf.tcs.fcalib.ImplicationSet;
import fcatools.conexpng.ContextChangeEvents;
import fcatools.conexpng.ProgramState;
import fcatools.conexpng.ProgramState.ContextChangeEvent;
import fcatools.conexpng.gui.View;
import fcatools.conexpng.model.AssociationRule;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

import com.alee.laf.panel.WebPanel;
import com.alee.laf.scroll.WebScrollPane;
import com.alee.laf.splitpane.WebSplitPane;
import com.alee.laf.text.WebTextPane;

import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.math.BigDecimal;
import java.util.*;

public class DependencyView extends View {

    private double minsup = 0.1;

    private double conf = 0.5;

    private static final long serialVersionUID = -6377834669097012170L;

    private WebTextPane implpane = new WebTextPane();

    private WebTextPane assopane = new WebTextPane();

    private Set<AssociationRule> associationbase;
    private Set<FCAImplication<String>> implications;

    private SimpleAttributeSet[] attrs;

    private SimpleAttributeSet header;

    private final int NON_ZERO_SUPPORT_EXACT_RULE = 0;

    private final int INEXACT_RULE = 1;

    private final int ZERO_SUPPORT_EXACT_RULE = 2;

    private final String FOLLOW = " ==> ";

    private final String END_MARK = ";";

    private final String EOL = System.getProperty("line.separator");

    private boolean sortBySupport = false;

    private boolean updateLater = false;

    public DependencyView(ProgramState state) {
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
        implpane.setPreferredSize(new Dimension(300, 200));
        assopane.setEditable(false);

        WebSplitPane splitPane = new WebSplitPane(WebSplitPane.VERTICAL_SPLIT, new WebScrollPane(implpane),
                new WebScrollPane(assopane));
        splitPane.setOneTouchExpandable(true);
        splitPane.setContinuousLayout(true);
        view = splitPane;

        settings = new WebPanel(new BorderLayout());
        settings.add(new DependencySettings(), BorderLayout.NORTH);
        settings.getComponent(0).addPropertyChangeListener(this);
        settings.setMinimumSize(new Dimension(170, 400));
        toolbar = null;
        super.init();
    }

    private void writeAssociations() {
        if (associationbase != null && implications != null)
            SwingUtilities.invokeLater(new Runnable() {

                @Override
                public void run() {
                    int i = 0;
                    StringBuffer buf;
                    int support = 0;
                    try {
                        implpane.setText("");
                        ArrayList<FCAImplication<String>> z = new ArrayList<>(implications);
                        if (sortBySupport) {
                            Collections.sort(z, new Comparator<FCAImplication<String>>() {

                                @Override
                                public int compare(FCAImplication<String> o1, FCAImplication<String> o2) {

                                    return Integer.compare(state.context.supportCount(o2.getPremise()),
                                            state.context.supportCount(o1.getPremise()));
                                }
                            });
                        }
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
                        // Associations
                        assopane.setText("");
                        ArrayList<AssociationRule> t = new ArrayList<>(associationbase);

                        if (sortBySupport) {
                            Collections.sort(t, new Comparator<AssociationRule>() {

                                @Override
                                public int compare(AssociationRule o1, AssociationRule o2) {
                                    return Double.compare(o2.getSupport(), o1.getSupport());
                                }
                            });
                        }
                        assopane.getDocument().insertString(assopane.getDocument().getLength(),
                                "Associations (Luxenburger Base)\n", header);
                        i = 0;
                        for (AssociationRule impl : t) {
                            buf = new StringBuffer();
                            if (impl.getConfidence() >= conf) {
                                i++;
                                buf.append(i);
                                buf.append("< " + state.context.supportCount(impl.getPremise()) + " > ");
                                buf.append(impl.getPremise()
                                        + " =["
                                        + new BigDecimal(impl.getConfidence()).setScale(3, BigDecimal.ROUND_HALF_UP)
                                                .doubleValue() + "]=> ");
                                Set<String> temp = new HashSet<>(impl.getConsequent());
                                temp.addAll(impl.getPremise());
                                buf.append("< " + state.context.supportCount(temp) + " > " + impl.getConsequent()
                                        + END_MARK);
                                buf.append(EOL);

                                assopane.getDocument().insertString(assopane.getDocument().getLength(), buf.toString(),
                                        implicationStyle(impl.getSupport(), impl.getConfidence()));
                            }
                        }
                        ((DependencySettings) settings.getComponent(0)).update(i, associationbase.size());
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

    private void updateAssociations(final boolean withImplications) {
        if (associationbase != null && implications != null) {
            state.startCalculation("Calculating the Dependencies");
            new SwingWorker<Object, Object>() {

                @Override
                protected Object doInBackground() throws Exception {
                    associationbase = state.context.getLuxenburgerBase(minsup, 0);
                    if (withImplications)
                        implications = state.context.getDuquenneGuiguesBase();
                    state.associations = associationbase;
                    return null;
                }

                protected void done() {
                    writeAssociations();
                    state.endCalculation();
                };
            }.execute();
        }
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt instanceof ContextChangeEvent) {
            ContextChangeEvent cce = (ContextChangeEvent) evt;
            if (cce.getName() == ContextChangeEvents.NEWCONTEXT || cce.getName() == ContextChangeEvents.CONTEXTCHANGED)
                updateLater = true;
            // until we save the associations in the file
            if (cce.getName() == ContextChangeEvents.LOADEDFILE)
                updateLater = true;
        }
        if (isVisible() && updateLater) {
            updateLater = false;
            associationbase = new TreeSet<AssociationRule>();
            implications = new ImplicationSet<>(state.context);
            updateAssociations(true);
            return;
        }
        if (evt.getPropertyName().equals("ConfidenceChanged")) {
            conf = (double) evt.getNewValue();

            writeAssociations();

        } else if (evt.getPropertyName().equals("MinimalSupportChanged")) {
            minsup = (double) evt.getNewValue();
            updateAssociations(false);
        } else if (evt.getPropertyName().equals("ToggleSortingOrder")) {
            sortBySupport = !sortBySupport;
            writeAssociations();
        }

    }

}
