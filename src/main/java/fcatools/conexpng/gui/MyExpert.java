package fcatools.conexpng.gui;

import de.tudresden.inf.tcs.fcaapi.FCAImplication;
import de.tudresden.inf.tcs.fcaapi.exception.IllegalObjectException;
import de.tudresden.inf.tcs.fcalib.AbstractExpert;
import de.tudresden.inf.tcs.fcalib.FullObject;
import de.tudresden.inf.tcs.fcalib.action.CounterExampleProvidedAction;
import de.tudresden.inf.tcs.fcalib.action.QuestionConfirmedAction;
import fcatools.conexpng.ProgramState;
import fcatools.conexpng.gui.contexteditor.ContextMatrix;
import fcatools.conexpng.gui.contexteditor.ContextMatrixModel;
import fcatools.conexpng.model.FormalContext;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import static fcatools.conexpng.gui.Util.*;

public class MyExpert extends
        AbstractExpert<String, String, FullObject<String, String>> {

    private JFrame frame;
    private FormalContext context;

    public MyExpert(JFrame mainFrame, FormalContext context) {
        this.context = context;
        this.frame = mainFrame;
    }

    /**
     * Called to notify the expert that the exploration finished (either the
     * expert wanted it or the exploration algorithm terminated). An
     * implementation of this method can for instance inform the expert by
     * writing a message, popping up a window etc.
     */
    public void explorationFinished() {
        showFinishDialog("Attribute Exploration is finished");
    }

    /**
     * Checks whether a given implication question holds. If yes, fires an
     * expert action of type {@link #CONFIRMED_QUESTION}, if no an expert action
     * of type {@link #REJECTED_QUESTION} and notifies listeners.
     *
     * @param question
     *            the given implication question
     */
    public void askQuestion(FCAImplication<String> question) {
        String questionstring = question.getPremise() + " ->"
                + question.getConclusion() + "?";
        Object[] options = { "Yes", "No", "Stop Attribute Exploration" };
        final JOptionPane optionPane = new JOptionPane(questionstring,
                JOptionPane.QUESTION_MESSAGE, JOptionPane.YES_NO_CANCEL_OPTION);
        optionPane.setOptions(options);
        final JDialog dialog = new JDialog(frame,
                "Confirm or reject implication", true);

        dialog.setContentPane(optionPane);
        optionPane.addPropertyChangeListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent e) {
                if (dialog.isVisible()
                        && (e.getSource() == optionPane)
                        && (e.getPropertyName()
                                .equals(JOptionPane.VALUE_PROPERTY))) {
                    dialog.setVisible(false);
                }
            }
        });
        dialog.pack();
        Util.centerDialogInsideMainFrame(frame, dialog);
        dialog.setVisible(true);
        String n = (String) optionPane.getValue();
        if (n.equals("Yes")) {
            QuestionConfirmedAction<String, String, FullObject<String, String>> action = new QuestionConfirmedAction<>();
            action.setQuestion(question);
            action.setContext(context);
            fireExpertAction(action);
        } else if (n.equals("No")) {
            requestCounterExample(question);
        } else {
            explorationFinished();
        }
    }

    @SuppressWarnings("serial")
    private class MiniContextEditor extends JPanel {
        ProgramState mcestate;
        final ContextMatrixModel matrixModel;
        final ContextMatrix matrix;

        public MiniContextEditor(FCAImplication<String> question) {
            mcestate = new ProgramState();
            mcestate.context = new FormalContext();
            mcestate.context.addAttributes(context.getAttributes());
            matrixModel = new ContextMatrixModel(mcestate);
            matrix = new ContextMatrix(matrixModel, mcestate.columnWidths);
            try {
                mcestate.context.addObject(new FullObject<String, String>("obj"
                        + context.getObjectCount(), question.getPremise()));
            } catch (IllegalObjectException e) {
                // should never happen, because the context is empty
                e.printStackTrace();
            }
            JScrollPane scrollPane = matrix
                    .createStripedJScrollPane(getBackground());
            // Only the height of 60 is important
            scrollPane.setPreferredSize(new Dimension(100, 60));

            setLayout(new BorderLayout(0, 10));
            add(new JLabel("Implication: " + question), BorderLayout.NORTH);
            add(scrollPane, BorderLayout.CENTER);
            MouseAdapter mouseAdapter = new MouseAdapter() {
                public void mouseClicked(MouseEvent e) {
                    int i = matrix.rowAtPoint(e.getPoint());
                    int j = matrix.columnAtPoint(e.getPoint());
                    int clicks = e.getClickCount();
                    if (clicks >= 2 && clicks % 2 == 0
                            && SwingUtilities.isLeftMouseButton(e)) { // Double
                                                                        // Click
                        if (i > 0 && j > 0) {
                            invokeAction(MiniContextEditor.this,
                                    new ToggleAction(i, j));
                        }
                        // TODO: enable this in the real contexteditor?
                        else if (i == 1 && j == 0)
                            matrix.renameRowHeader(i);
                    }
                }

                public void mousePressed(MouseEvent e) {

                }

                public void mouseReleased(MouseEvent e) {
                }
            };
            matrix.addMouseListener(mouseAdapter);
            matrix.addMouseMotionListener(mouseAdapter);
            matrixModel.fireTableStructureChanged();
            // TODO: @eugen add toggleaction and move action or do what ever you
            // want
        }

        class ToggleAction extends AbstractAction {
            int i, j;

            ToggleAction(int i, int j) {
                this.i = i;
                this.j = j;
            }

            public void actionPerformed(ActionEvent e) {
                if (i <= 0 || j <= 0)
                    return;
                int i = clamp(this.i, 1, mcestate.context.getObjectCount()) - 1;
                int j = clamp(this.j, 1, mcestate.context.getAttributeCount()) - 1;
                mcestate.context.toggleAttributeForObject(
                        mcestate.context.getAttributeAtIndex(j),
                        mcestate.context.getObjectAtIndex(i).getIdentifier());
                matrix.saveSelection();
                matrixModel.fireTableDataChanged();
                matrix.restoreSelection();
                mcestate.contextChanged();
            }
        }

        public FullObject<String, String> getCounterexample() {
            return mcestate.context.getObjectAtIndex(0);
        }
    }

    private void showCounterExampleDialog(FCAImplication<String> question) {
        MiniContextEditor mce = new MiniContextEditor(question);
        JOptionPane pane = new JOptionPane(mce,
                JOptionPane.YES_NO_CANCEL_OPTION);
        pane.setMessageType(JOptionPane.PLAIN_MESSAGE);
        JDialog dialog = pane.createDialog(frame, "Provide a counterexample");
        Object[] options = { "Provide counterexample", "Accept implication",
                "Stop" };
        pane.setOptions(options);
        dialog.pack();
        Util.centerDialogInsideMainFrame(frame, dialog);
        dialog.setVisible(true);
        String n = (String) pane.getValue();
        if (n != null)
            if (n.equals("Provide counterexample")) {
                // TODO: Change CounterExampleProvidedAction, so it can
                // propagate a ContextChangedEvent
                CounterExampleProvidedAction<String, String, FullObject<String, String>> action = new CounterExampleProvidedAction<>(
                        context, question, mce.getCounterexample());
                fireExpertAction(action);
            } else if (n.equals("Accept implication")) {
                QuestionConfirmedAction<String, String, FullObject<String, String>> action = new QuestionConfirmedAction<>();
                action.setQuestion(question);
                action.setContext(context);
                fireExpertAction(action);
            } else {
                explorationFinished();
            }
    }

    /**
     * Called to notify the expert that the specified counterexample is invalid
     * due to the given reason. The reason is one of
     * {@link #COUNTEREXAMPLE_EXISTS} or {@link #COUNTEREXAMPLE_INVALID}. An
     * implementation of this method should then perform the necessary actions.
     * For instance, if it is a human expert, it should display an error message
     * with the reason.
     *
     * @param counterExample
     *            the counterexample given by the expert
     * @param reason
     *            the reason why the counterexample is not valid
     */
    public void counterExampleInvalid(
            FullObject<String, String> counterexample, int reason) {
        if (reason == COUNTEREXAMPLE_INVALID) {
            showErrorDialog(counterexample.getIdentifier()
                    + " doesn't respect the implication.");
        }
        if (reason == COUNTEREXAMPLE_EXISTS) {
            showErrorDialog(counterexample.getIdentifier() + " already exists.");
        }

    }

    private void showErrorDialog(String message) {
        JOptionPane pane = new JOptionPane(message, JOptionPane.ERROR_MESSAGE);
        JDialog dialog = pane.createDialog(frame, "Error");
        dialog.pack();
        centerDialogInsideMainFrame(frame, dialog);
        dialog.setVisible(true);
    }

    public void forceToCounterExample(FCAImplication<String> implication) {
        // nothing todo
    }

    /**
     * Gets a counterexample, fires an expert action of type
     * {@link #PROVIDED_COUNTEREXAMPLE}
     *
     * @param question
     *            the given implication question
     */
    public void requestCounterExample(FCAImplication<String> implication) {
        showCounterExampleDialog(implication);
    }

    private void showFinishDialog(String message) {
        JOptionPane pane = new JOptionPane();
        JDialog dialog = pane.createDialog(frame, "Information");
        pane.setMessageType(JOptionPane.INFORMATION_MESSAGE);
        pane.setMessage(new JLabel(message));
        dialog.pack();
        Util.centerDialogInsideMainFrame(frame, dialog);
        dialog.setVisible(true);
    }

    @Override
    public void implicationFollowsFromBackgroundKnowledge(
            FCAImplication<String> arg0) {
        // nothing todo
    }

}
