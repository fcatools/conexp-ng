package com.eugenkiss.conexp2.gui;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Set;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

import com.eugenkiss.conexp2.ProgramState;
import com.eugenkiss.conexp2.gui.ImplicationView.Sort;
import com.eugenkiss.conexp2.model.AssociationRule;

import de.tudresden.inf.tcs.fcaapi.FCAImplication;
import de.tudresden.inf.tcs.fcalib.utils.ListSet;

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
        settings = new AssociationSettings();
        settings.addPropertyChangeListener(this);
        JButton b = Util.createButton("Sort by object count", "sort",
                "conexp/sort.gif");
        toolbar.add(b);
        b.addActionListener(new Sort());
        super.init();
        updateAssociations();
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
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
            sortBySupport = false;
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
        ((AssociationSettings) settings).update(i, associationbase.size());
    }

    public javax.swing.text.SimpleAttributeSet implicationStyle(double support,
            double confidence) {
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
        if (evt.getPropertyName().equals("ConfidenceChanged")) {
            conf = (double) evt.getNewValue();
            writeAssociations();
        }
        if (evt.getPropertyName().equals("MinimalSupportChanged")) {
            minsup = (double) evt.getNewValue();
            updateAssociations();
        }
    }

    private boolean sortBySupport = false;

    @SuppressWarnings("serial")
    class Sort extends AbstractAction {

        public void actionPerformed(ActionEvent e) {
            sortBySupport = true;
            writeAssociations();
        }
    }
}
