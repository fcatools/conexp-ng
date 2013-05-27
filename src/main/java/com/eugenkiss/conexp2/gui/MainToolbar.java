package com.eugenkiss.conexp2.gui;

import com.eugenkiss.conexp2.OS;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import static com.eugenkiss.conexp2.gui.Util.centerDialogInsideMainFrame;
import static com.eugenkiss.conexp2.gui.Util.createButton;

public class MainToolbar extends JToolBar {

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

    public MainToolbar(final JFrame mainFrame) {
        this.mainFrame = mainFrame;
        this.setFloatable(false);
        this.setMargin(new Insets(getInsets().top + 2, getInsets().left + 3, getInsets().bottom - 3, getInsets().right + 4));
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
        addSeparator();
        helpButton = createButton("Help", "help", "conexp/question.gif");
        add(Box.createGlue());
        add(helpButton);

        // Add actions
        // TODO: Maybe create an (public) Action since we want to also save from the exit prompt
        //       when there are unsaved changes.
        saveButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                JOptionPane.showMessageDialog(MainToolbar.this, "Save");
            }
        });
        countButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent arg0) {
                showMessageDialog("10 Concepts");
            }
        });
        exploreButton.addActionListener(new ActionListener() {
            // Maybe it is an own class worth
            public void actionPerformed(ActionEvent arg0) {

                // TODO: Placeholder
                showMessageDialog("10 Concepts");
                /*
                Object[] options = { "Yes", "No", "WTF" };
                int choice;
                do {
                    choice = JOptionPane.showOptionDialog(MainToolbar.this,
                            "Wenn das Objekt das und das hat...",
                            "A Silly Question",
                            JOptionPane.YES_NO_CANCEL_OPTION,
                            JOptionPane.QUESTION_MESSAGE, null, options,
                            options[2]);
                } while (choice != 2);
                */
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

}
