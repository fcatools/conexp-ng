package fcatools.conexpng.gui;

import fcatools.conexpng.ContextChangeEvents;
import fcatools.conexpng.ProgramState;
import fcatools.conexpng.ProgramState.ContextChangeEvent;
import fcatools.conexpng.model.AssociationRule;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
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

    ThreadPoolExecutor pool = new ThreadPoolExecutor(1, 1, 0,
            TimeUnit.NANOSECONDS, new SynchronousQueue<Runnable>());

    private SimpleAttributeSet[] attrs;

    final int NON_ZERO_SUPPORT_EXACT_RULE = 0;

    final int INEXACT_RULE = 1;

    final int ZERO_SUPPORT_EXACT_RULE = 2;

    protected final String FOLLOW = " ==> ";

    protected final String END_MARK = ";";

    private final String EOL = System.getProperty("line.separator");

    private boolean sortBySupport = true;

    private CalculationThread calculation = new CalculationThread();

    private class CalculationThread extends Thread {

        @Override
        public void run() {
            associationbase = state.context.getLuxenburgerBase(minsup, 0);
            state.associations = associationbase;
            Runnable runnable = new Runnable() {
                public void run() {
                    writeAssociations();
                }
            };
            SwingUtilities.invokeLater(runnable);

        };
    };

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

        textpane.setEditable(false);
        view = new JScrollPane(textpane);
        settings = new JPanel(new BorderLayout());
        settings.add(new AssociationSettings(), BorderLayout.NORTH);
        settings.getComponent(0).addPropertyChangeListener(this);
        super.init();
        updateAssociations();
    }

    private void writeAssociations() {
        int i = 0;
        StringBuffer buf;
        textpane.setText("");
        ArrayList<AssociationRule> t = new ArrayList<>(associationbase);

        if (sortBySupport) {
            Collections.sort(t, new Comparator<AssociationRule>() {

                @Override
                public int compare(AssociationRule o1, AssociationRule o2) {

                    return Double.compare(o2.getSupport(), o1.getSupport());
                }
            });
        }

        for (AssociationRule impl : t) {
            buf = new StringBuffer();
            if (impl.getConfidence() >= conf) {
                i++;
                buf.append(i);
                buf.append("< " + state.context.supportCount(impl.getPremise())
                        + " > ");
                buf.append(impl.getPremise() + " =[" + impl.getConfidence()
                        + "]=> ");
                buf.append("< "
                        + state.context.supportCount(impl.getConsequent())
                        + " > " + impl.getConsequent() + END_MARK);
                buf.append(EOL);

                try {
                    textpane.getDocument().insertString(
                            textpane.getDocument().getLength(),
                            buf.toString(),
                            implicationStyle(impl.getSupport(),
                                    impl.getConfidence()));
                } catch (BadLocationException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
        ((AssociationSettings) settings.getComponent(0)).update(i,
                associationbase.size());
    }

    public SimpleAttributeSet implicationStyle(double support, double confidence) {
        if (confidence == 1.0) {
            return support > 0 ? attrs[NON_ZERO_SUPPORT_EXACT_RULE]
                    : attrs[ZERO_SUPPORT_EXACT_RULE];
        }
        return attrs[INEXACT_RULE];
    }

    private void updateAssociations() {
        if (pool.getActiveCount() != 0) {
            pool.remove(calculation);
            pool.shutdownNow();
            pool = new ThreadPoolExecutor(1, 1, 0, TimeUnit.NANOSECONDS,
                    new SynchronousQueue<Runnable>());
        }
        pool.execute(calculation);
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt instanceof ContextChangeEvent) {
            ContextChangeEvent cce = (ContextChangeEvent) evt;
            if (cce.getName() == ContextChangeEvents.CONTEXTCHANGED) {
                updateAssociations();
            }
        } else if (evt.getPropertyName().equals("ConfidenceChanged")) {
            conf = (double) evt.getNewValue();
            writeAssociations();
        } else if (evt.getPropertyName().equals("MinimalSupportChanged")) {
            minsup = (double) evt.getNewValue();
            updateAssociations();
        } else {
            sortBySupport = !sortBySupport;
            writeAssociations();
        }
    }

}
