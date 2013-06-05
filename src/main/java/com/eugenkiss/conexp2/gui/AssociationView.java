package com.eugenkiss.conexp2.gui;

import java.awt.Color;
import java.beans.PropertyChangeEvent;
import java.util.Set;

import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

import com.eugenkiss.conexp2.ProgramState;
import com.eugenkiss.conexp2.model.AssociationRule;

public class AssociationView extends View {

    private double minsup = 0.1;

    private double conf = 0.5;

    private static final long serialVersionUID = -6377834669097012170L;

    private JTextPane textpane = new JTextPane();

    private Set<AssociationRule> associationbase;

    private SimpleAttributeSet[] attrs;
    final int NON_ZERO_SUPPORT_EXACT_RULE = 0;

    final int INEXACT_RULE = 1;

    final int ZERO_SUPPORT_EXACT_RULE = 2;

    protected final String FOLLOW = " ==> ";

    protected final String END_MARK = ";";

    private final String EOL = System.getProperty("line.separator");

    private CalculationThread calculation = new CalculationThread();

    private class CalculationThread extends Thread {

        public void run() {
            associationbase = state.context.getLuxenburgerBase(minsup, 0);
            state.associations = associationbase;
            writeAssociations();
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
        for (AssociationRule impl : associationbase) {
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
        calculation.run();
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

}
