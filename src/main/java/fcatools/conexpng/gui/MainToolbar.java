package fcatools.conexpng.gui;

import fcatools.conexpng.OS;
import fcatools.conexpng.ProgramState;

import javax.swing.*;

import de.tudresden.inf.tcs.fcalib.FullObject;
import de.tudresden.inf.tcs.fcalib.action.StartExplorationAction;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import static fcatools.conexpng.gui.Util.centerDialogInsideMainFrame;
import static fcatools.conexpng.gui.Util.createButton;

public class MainToolbar extends JToolBar {

    private ProgramState state;

    private static final long serialVersionUID = -3495670613141172867L;

    private final JFrame mainFrame;
    private final JButton newButton;
    private final JButton openButton;
    private final JButton saveButton;
    private final JButton saveAsButton;
    private final JButton undoButton;
    private final JButton redoButton;
    private final JButton countButton;
    private final JButton exploreButton;
    private final JButton helpButton;

    public MainToolbar(final JFrame mainFrame, final ProgramState state) {
        this.mainFrame = mainFrame;
        this.state = state;
        this.setFloatable(false);
        this.setMargin(new Insets(getInsets().top + 2, getInsets().left + 3,
                getInsets().bottom - 3, getInsets().right + 4));
        if (OS.isMacOsX) {
            this.setMargin(new Insets(getInsets().top, getInsets().left + 2,
                    getInsets().bottom, getInsets().right));
        }

        // Add buttons
        newButton = createButton("New Context", "newContext", "conexp/new.gif");
        add(newButton);
        openButton = createButton("Open Context", "openContext",
                "conexp/open.gif");
        add(openButton);
        saveButton = createButton("Save Contex", "saveContext",
                "conexp/save.gif");
        add(saveButton);
        saveAsButton = createButton("Save as another Context", "saveAsContext",
                "conexp/save.gif");
        add(saveAsButton);
        addSeparator();
        undoButton = createButton("Undo", "undo", "conexp/Undo.gif");
        add(undoButton);
        redoButton = createButton("Redo", "redo", "conexp/Redo.gif");
        add(redoButton);
        addSeparator();
        countButton = createButton("Count Concepts", "countConcepts",
                "conexp/numConcepts.gif");
        add(countButton);
        exploreButton = createButton("Explore Attributes", "exploreAttributes",
                "conexp/attrExploration.gif");
        add(exploreButton);
        helpButton = createButton("Help", "help", "conexp/question.gif");
        add(Box.createGlue());
        add(helpButton);

        // Add actions
        openButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent arg0) {
                new OpenAction().actionPerformed(arg0);
            }
        });
        saveButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent arg0) {
                new SaveAction().actionPerformed(arg0);
            }
        });
        saveAsButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent arg0) {
                state.filePath = "";
                new SaveAction().actionPerformed(arg0);
            }
        });
        countButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent arg0) {
                showMessageDialog("10 Concepts");
            }
        });
        exploreButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                MyExpert expert = new MyExpert(mainFrame, state.context);
                state.context.setExpert(expert);
                expert.addExpertActionListener(state.context);
                // Create an expert action for starting attribute exploration
                StartExplorationAction<String, String, FullObject<String, String>> action = new StartExplorationAction<String, String, FullObject<String, String>>();
                action.setContext(state.context);
                // Fire the action, exploration starts...
                expert.fireExpertAction(action);
            }
        });
    }

    public void enableSaveButton() {
        saveButton.setEnabled(true);
    }

    public void disableSaveButton() {
        saveButton.setEnabled(false);
    }

    public void enableUndoButton() {
        undoButton.setEnabled(true);
    }

    public void disableUndoButton() {
        undoButton.setEnabled(false);
    }

    public void enableRedoButton() {
        redoButton.setEnabled(true);
    }

    public void disableRedoButton() {
        redoButton.setEnabled(false);
    }

    // TODO: Needs to be improved
    private void showMessageDialog(String message) {
        JOptionPane pane = new JOptionPane();
        JDialog dialog = pane.createDialog(mainFrame, message);
        dialog.pack();
        centerDialogInsideMainFrame(mainFrame, dialog);
        dialog.setVisible(true);
    }

    // ////////////////////////////////////////////////////////////////////////

    @SuppressWarnings("serial")
    class SaveAction extends AbstractAction {

        @Override
        public void actionPerformed(ActionEvent e) {
            if (state.filePath.isEmpty()) {
                final JFileChooser fc = new JFileChooser();
                final JDialog dialog = new JDialog(mainFrame, "Save file as",
                        true);

                dialog.setContentPane(fc);
                fc.setDialogType(JFileChooser.SAVE_DIALOG);
                fc.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        String state = (String) e.getActionCommand();
                        if ((state.equals(JFileChooser.APPROVE_SELECTION) && fc
                                .getSelectedFile() != null)
                                || state.equals(JFileChooser.CANCEL_SELECTION)) {
                            dialog.setVisible(false);
                        }
                    }
                });
                dialog.pack();
                Util.centerDialogInsideMainFrame(mainFrame, dialog);
                dialog.setVisible(true);

                if (fc.getSelectedFile() != null) {
                    File file = fc.getSelectedFile();
                    state.filePath = file.getAbsolutePath();
                    mainFrame
                            .setTitle("ConExp-NG - \"" + state.filePath + "\"");
                }
            }
            // TODO: save document
        }
    }

    @SuppressWarnings("serial")
    class OpenAction extends AbstractAction {

        @Override
        public void actionPerformed(ActionEvent e) {
            if (state.filePath.isEmpty()) {
                final JFileChooser fc = new JFileChooser();
                int returnVal = fc.showOpenDialog(mainFrame);

                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    File file = fc.getSelectedFile();
                    state.filePath = file.getAbsolutePath();
                    mainFrame
                            .setTitle("ConExp-NG - \"" + state.filePath + "\"");
                }
            }
            // TODO: open document
        }
    }

}
