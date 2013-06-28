package fcatools.conexpng.gui.dependencies;

import fcatools.conexpng.ContextChangeEvents;
import fcatools.conexpng.ProgramState;
import fcatools.conexpng.ProgramState.ContextChangeEvent;
import fcatools.conexpng.gui.View;
import fcatools.conexpng.model.AssociationRule;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

import de.tudresden.inf.tcs.fcaapi.FCAImplication;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Set;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class AssociationView extends View {

    private double minsup = 0.1;

    private double conf = 0.5;

    private static final long serialVersionUID = -6377834669097012170L;

    private JTextPane textpane = new JTextPane();

    private Set<AssociationRule> associationbase;
    private Set<FCAImplication<String>> implications;

    ThreadPoolExecutor pool = new ThreadPoolExecutor(1, 1, 0,
            TimeUnit.NANOSECONDS, new SynchronousQueue<Runnable>());

    private SimpleAttributeSet[] attrs;

    private SimpleAttributeSet header;

    private final int NON_ZERO_SUPPORT_EXACT_RULE = 0;

    private final int INEXACT_RULE = 1;

    private final int ZERO_SUPPORT_EXACT_RULE = 2;

    private final String FOLLOW = " ==> ";

    private final String END_MARK = ";";

    private final String EOL = System.getProperty("line.separator");

    private boolean sortBySupport = true;

    private boolean updateLater = false;

    public AssociationView(ProgramState state) {
        super(state);
        attrs = new SimpleAttributeSet[3];
        attrs[NON_ZERO_SUPPORT_EXACT_RULE] = new SimpleAttributeSet();
        StyleConstants.setForeground(attrs[NON_ZERO_SUPPORT_EXACT_RULE],
                Color.blue);

        attrs[ZERO_SUPPORT_EXACT_RULE] = new SimpleAttributeSet();
        StyleConstants.setForeground(attrs[ZERO_SUPPORT_EXACT_RULE], Color.red);

        attrs[INEXACT_RULE] = new SimpleAttributeSet();
        StyleConstants.setForeground(attrs[INEXACT_RULE], new Color(0, 128, 0));

        header = new SimpleAttributeSet();
        StyleConstants.setFontSize(header, 12);

        textpane.setEditable(false);
        view = new JScrollPane(textpane);
        settings = new JPanel(new BorderLayout());
        settings.add(new AssociationSettings(), BorderLayout.NORTH);
        settings.getComponent(0).addPropertyChangeListener(this);
        super.init();
        associationbase = state.context.getLuxenburgerBase(minsup, 0);
        implications = state.context.getDuquenneGuiguesBase();
        state.associations = associationbase;
        writeAssociations();
    }

    private void writeAssociations() {
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                int i = 0;
                StringBuffer buf;
                int support = 0;
                try {
                    textpane.setText("");
                    ArrayList<FCAImplication<String>> z = new ArrayList<>(
                            implications);
                    if (sortBySupport) {
                        Collections.sort(z,
                                new Comparator<FCAImplication<String>>() {

                                    @Override
                                    public int compare(
                                            FCAImplication<String> o1,
                                            FCAImplication<String> o2) {

                                        return Integer.compare(state.context
                                                .supportCount(o2.getPremise()),
                                                state.context.supportCount(o1
                                                        .getPremise()));
                                    }
                                });
                    }
                    textpane.getDocument().insertString(
                            textpane.getDocument().getLength(),
                            "Duquenne-Guigues Base\n", header);

                    for (FCAImplication<String> impl : z) {
                        support = state.context.supportCount(impl.getPremise());
                        buf = new StringBuffer();
                        buf.append(i);
                        buf.append("< " + support + " > ");
                        buf.append(impl.getPremise() + FOLLOW
                                + impl.getConclusion() + END_MARK);
                        buf.append(EOL);
                        i++;

                        textpane.getDocument().insertString(
                                textpane.getDocument().getLength(),
                                buf.toString(), implicationStyle(support, 1));
                    }

                    ArrayList<AssociationRule> t = new ArrayList<>(
                            associationbase);

                    if (sortBySupport) {
                        Collections.sort(t, new Comparator<AssociationRule>() {

                            @Override
                            public int compare(AssociationRule o1,
                                    AssociationRule o2) {

                                return Double.compare(o2.getSupport(),
                                        o1.getSupport());
                            }
                        });
                    }
                    textpane.getDocument()
                            .insertString(
                                    textpane.getDocument().getLength(),
                                    "------------------------------------\nLuxenburger Base\n",
                                    header);
                    i = 0;
                    for (AssociationRule impl : t) {
                        buf = new StringBuffer();
                        if (impl.getConfidence() >= conf) {
                            i++;
                            buf.append(i);
                            buf.append("< "
                                    + state.context.supportCount(impl
                                            .getPremise()) + " > ");
                            buf.append(impl.getPremise() + " =["
                                    + impl.getConfidence() + "]=> ");
                            buf.append("< "
                                    + state.context.supportCount(impl
                                            .getConsequent()) + " > "
                                    + impl.getConsequent() + END_MARK);
                            buf.append(EOL);

                            textpane.getDocument().insertString(
                                    textpane.getDocument().getLength(),
                                    buf.toString(),
                                    implicationStyle(impl.getSupport(),
                                            impl.getConfidence()));
                        }
                    }
                    ((AssociationSettings) settings.getComponent(0)).update(i,
                            associationbase.size());
                } catch (BadLocationException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        });
    }

    public SimpleAttributeSet implicationStyle(double support, double confidence) {
        if (confidence == 1.0) {
            return support > 0 ? attrs[NON_ZERO_SUPPORT_EXACT_RULE]
                    : attrs[ZERO_SUPPORT_EXACT_RULE];
        }
        return attrs[INEXACT_RULE];
    }

    private void updateAssociations() {
        state.startCalculation("Calculating the Dependencies");
        new SwingWorker<Object, Object>() {

            @Override
            protected Object doInBackground() throws Exception {
                associationbase = state.context.getLuxenburgerBase(minsup, 0);
                implications = state.context.getDuquenneGuiguesBase();
                state.associations = associationbase;
                writeAssociations();
                return null;
            }

            protected void done() {
                state.endCalculation();
            };
        }.execute();

    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {

        if (evt instanceof ContextChangeEvent) {
            ContextChangeEvent cce = (ContextChangeEvent) evt;
            if (cce.getName() == ContextChangeEvents.NEWCONTEXT
                    || cce.getName() == ContextChangeEvents.CONTEXTCHANGED)
                updateLater = true;
            return;
        }
        if (isVisible() && updateLater) {
            updateAssociations();
            updateLater = false;
            return;
        }
        if (evt.getPropertyName().equals("ConfidenceChanged")) {
            conf = (double) evt.getNewValue();

            writeAssociations();

        } else if (evt.getPropertyName().equals("MinimalSupportChanged")) {
            minsup = (double) evt.getNewValue();
            updateAssociations();
        } else if (evt.getPropertyName().equals("ToggleSortingOrder")) {
            sortBySupport = !sortBySupport;
            writeAssociations();
        }

    }

}
