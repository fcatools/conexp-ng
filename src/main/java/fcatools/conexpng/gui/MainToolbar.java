package fcatools.conexpng.gui;

import de.tudresden.inf.tcs.fcaapi.exception.IllegalObjectException;
import de.tudresden.inf.tcs.fcalib.FullObject;
import de.tudresden.inf.tcs.fcalib.action.StartExplorationAction;
import fcatools.conexpng.OS;
import fcatools.conexpng.ProgramState;
import fcatools.conexpng.Util;
import fcatools.conexpng.io.BurmeisterReader;
import fcatools.conexpng.io.BurmeisterWriter;
import fcatools.conexpng.io.CEXReader;
import fcatools.conexpng.io.CEXWriter;

import javax.swing.*;
import javax.xml.stream.XMLStreamException;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import static fcatools.conexpng.Util.centerDialogInsideMainFrame;
import static fcatools.conexpng.Util.createButton;

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
        this.setMargin(new Insets(getInsets().top + 2, getInsets().left + 3, getInsets().bottom - 3,
                getInsets().right + 4));
        if (OS.isMacOsX) {
            this.setMargin(new Insets(getInsets().top, getInsets().left + 2, getInsets().bottom, getInsets().right));
        }

        // Add buttons
        newButton = createButton("New Context", "newContext", "conexp/new.gif");
        add(newButton);
        openButton = createButton("Open Context", "openContext", "conexp/open.gif");
        add(openButton);
        saveButton = createButton("Save Contex", "saveContext", "conexp/save.gif");
        add(saveButton);
        saveAsButton = createButton("Save as another Context", "saveAsContext", "conexp/save.gif");
        add(saveAsButton);
        addSeparator();
        undoButton = createButton("Undo", "undo", "conexp/Undo.gif");
        add(undoButton);
        redoButton = createButton("Redo", "redo", "conexp/Redo.gif");
        add(redoButton);
        addSeparator();
        countButton = createButton("Count Concepts", "countConcepts", "conexp/numConcepts.gif");
        add(countButton);
        exploreButton = createButton("Explore Attributes", "exploreAttributes", "conexp/attrExploration.gif");
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
                new SaveAction(false).actionPerformed(arg0);
            }
        });
        saveAsButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent arg0) {
                new SaveAction(true).actionPerformed(arg0);
            }
        });
        countButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent arg0) {
                showMessageDialog(state.numberOfConcepts + " Concepts", false);
            }
        });
        exploreButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                MyExpert expert = new MyExpert(mainFrame, state);
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
    private void showMessageDialog(String message, boolean error) {
        JOptionPane pane = new JOptionPane(message);
        pane.setMessageType(error ? JOptionPane.ERROR_MESSAGE : JOptionPane.INFORMATION_MESSAGE);
        JDialog dialog = pane.createDialog(mainFrame, "Error");
        dialog.pack();
        centerDialogInsideMainFrame(mainFrame, dialog);
        dialog.setVisible(true);
    }

    // ////////////////////////////////////////////////////////////////////////

    @SuppressWarnings("serial")
    class SaveAction extends AbstractAction {

        private boolean saveAs;

        public SaveAction(boolean saveAS) {
            this.saveAs = saveAS;
        }

        private void saveFile(String path) {
            try {
                MainToolbar.this.state.filePath = path;
                if (MainToolbar.this.state.filePath.endsWith(".cex"))

                    new CEXWriter(MainToolbar.this.state);

                else
                    new BurmeisterWriter(MainToolbar.this.state);
                System.out.println(path);
                MainToolbar.this.state.setNewFile(path);

                mainFrame.setTitle("ConExp-NG - \"" + path + "\"");
            } catch (FileNotFoundException e1) {
                showMessageDialog("Can not find this file: " + path, true);
            } catch (IOException | XMLStreamException e1) {
                showMessageDialog("The file seems to be corrupt: " + e1.getMessage(), true);
            }

        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if (state.filePath.equals("untitled.cex") || saveAs) {
                final JFileChooser fc = new JFileChooser(state.filePath);
                final JDialog dialog = new JDialog(mainFrame, "Save file as", true);
                dialog.setContentPane(fc);
                fc.setDialogType(JFileChooser.SAVE_DIALOG);
                fc.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        String state = (String) e.getActionCommand();
                        if ((state.equals(JFileChooser.APPROVE_SELECTION) && fc.getSelectedFile() != null)) {
                            File file = fc.getSelectedFile();
                            String path = file.getAbsolutePath();
                            if (file.exists()) {
                                JOptionPane pane = new JOptionPane(new JLabel("Do you really want to overwrite "
                                        + file.getName() + "?"), JOptionPane.YES_NO_OPTION);
                                pane.setMessageType(JOptionPane.QUESTION_MESSAGE);
                                JDialog dialog2 = pane.createDialog(mainFrame, "Overwriting existing file?");
                                Object[] options = { "Yes", "No" };
                                pane.setOptions(options);
                                dialog2.pack();
                                dialog2.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
                                Util.centerDialogInsideMainFrame(mainFrame, dialog2);
                                dialog2.setVisible(true);
                                String n = (String) pane.getValue();
                                if (n != null)
                                    if (n.equals("Yes")) {
                                        saveFile(path);
                                        dialog.setVisible(false);
                                    }
                            } else {
                                MainToolbar.this.state.filePath = path;
                                saveFile(path);
                                dialog.setVisible(false);
                            }
                        } else if (state.equals(JFileChooser.CANCEL_SELECTION)) {
                            dialog.setVisible(false);
                            return;
                        }
                    }
                });
                dialog.pack();
                Util.centerDialogInsideMainFrame(mainFrame, dialog);
                dialog.setVisible(true);

                if (fc.getSelectedFile() != null) {

                } else
                    return;
            } else {
                saveFile(MainToolbar.this.state.filePath);
            }
        }

    }

    @SuppressWarnings("serial")
    class OpenAction extends AbstractAction {

        @Override
        public void actionPerformed(ActionEvent e) {

            final JFileChooser fc = new JFileChooser(state.filePath);
            final JDialog dialog = new JDialog(mainFrame, "Open file", true);

            dialog.setContentPane(fc);
            fc.setDialogType(JFileChooser.OPEN_DIALOG);
            fc.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    String state = (String) e.getActionCommand();
                    if ((state.equals(JFileChooser.APPROVE_SELECTION) && fc.getSelectedFile() != null)
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
                String path = file.getAbsolutePath();
                state.setNewFile(path);
                mainFrame.setTitle("ConExp-NG - \"" + path + "\"");

                try {
                    if (path.endsWith(".cex"))
                        new CEXReader(state);
                    else
                        new BurmeisterReader(state);
                } catch (FileNotFoundException e1) {
                    showMessageDialog("Can not find this file: " + path, true);
                } catch (IllegalObjectException | IOException | XMLStreamException e1) {
                    showMessageDialog("The file seems to be corrupt: " + e1.getMessage(), true);
                }
            }
        }
    }
}
