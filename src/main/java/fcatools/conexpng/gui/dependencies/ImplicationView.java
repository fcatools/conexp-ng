package fcatools.conexpng.gui.dependencies;

import de.tudresden.inf.tcs.fcaapi.FCAImplication;
import fcatools.conexpng.ContextChangeEvents;
import fcatools.conexpng.ProgramState;
import fcatools.conexpng.ProgramState.ContextChangeEvent;
import fcatools.conexpng.Util;
import fcatools.conexpng.gui.View;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Set;

public class ImplicationView extends View {

    private static final long serialVersionUID = -6377834669097012170L;

    private JTextPane textpane = new JTextPane();

    private Set<FCAImplication<String>> implications;

    private SimpleAttributeSet[] attrs;
    final int NON_ZERO_SUPPORT_STYLE = 0;
    final int ZERO_SUPPORT_STYLE = 1;

    protected final String FOLLOW = " ==> ";

    protected final String END_MARK = ";";

    private final String EOL = System.getProperty("line.separator");

    public ImplicationView(ProgramState state) {
        super(state);

        attrs = new SimpleAttributeSet[2];
        attrs[NON_ZERO_SUPPORT_STYLE] = new SimpleAttributeSet();
        StyleConstants.setForeground(attrs[NON_ZERO_SUPPORT_STYLE], Color.blue);

        attrs[ZERO_SUPPORT_STYLE] = new SimpleAttributeSet();
        StyleConstants.setForeground(attrs[ZERO_SUPPORT_STYLE], Color.red);

        textpane.setEditable(false);
        view = new JScrollPane(textpane);
        setLayout(new BorderLayout());
        panel = new JPanel();
        panel.setLayout(new BorderLayout());

        toolbar.setFloatable(false);

        panel.add(toolbar, BorderLayout.WEST);
        panel.add(view, BorderLayout.CENTER);
        add(panel);
        JButton b = Util.createButton("Sort by object count", "sort",
                "conexp/sort.gif");
        toolbar.add(b);
        b.addActionListener(new Sort());
        updateImplications();
    }

    private void writeImplications() {
        int i = 1, support = 0;
        StringBuffer buf;
        textpane.setText("");
        for (FCAImplication<String> impl : implications) {
            support = state.context.supportCount(impl.getPremise());
            buf = new StringBuffer();
            buf.append(i);
            buf.append("< " + support + " > ");
            buf.append(impl.getPremise() + FOLLOW + impl.getConclusion()
                    + END_MARK);
            buf.append(EOL);
            i++;
            try {
                textpane.getDocument().insertString(
                        textpane.getDocument().getLength(), buf.toString(),
                        implicationStyle(support));
            } catch (BadLocationException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        }

    }

    public javax.swing.text.SimpleAttributeSet implicationStyle(int support) {
        return 0 == support ? attrs[ZERO_SUPPORT_STYLE]
                : attrs[NON_ZERO_SUPPORT_STYLE];
    }

    private void updateImplications() {
        implications = state.context.getStemBase();
        writeImplications();
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        ContextChangeEvent e = (ContextChangeEvent) evt;
        if (e.getName() == ContextChangeEvents.NEWCONTEXT
                || e.getName() == ContextChangeEvents.CONTEXTCHANGED)
            updateImplications();
    }

    @SuppressWarnings("serial")
    class Sort extends AbstractAction {
        public void actionPerformed(ActionEvent e) {
            ArrayList<FCAImplication<String>> t = new ArrayList<>(implications);

            Collections.sort(t, new Comparator<FCAImplication<String>>() {

                @Override
                public int compare(FCAImplication<String> o1,
                        FCAImplication<String> o2) {
                    return Integer.compare(
                            state.context.supportCount(o1.getPremise()),
                            state.context.supportCount(o1.getPremise()));
                }
            });
            implications.clear();
            for (FCAImplication<String> fcaImplication : t) {
                implications.add(fcaImplication);
            }
            writeImplications();
        }
    }

}
