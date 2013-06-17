package fcatools.conexpng.gui;

import fcatools.conexpng.ProgramState;
import fcatools.conexpng.Util;
import fcatools.conexpng.gui.contexteditor.ContextEditor;
import fcatools.conexpng.gui.dependencies.AssociationView;
import fcatools.conexpng.gui.dependencies.ImplicationView;
import fcatools.conexpng.gui.lattice.LatticeView;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

public class MainFrame extends JFrame {

    private static final long serialVersionUID = -3768163989667340886L;

    // Components
    private JTabbedPane tabPane;
    private View contextView;
    private View latticeView;
    private View associationView;
    private View implicationView;

    private ProgramState state;

    private MainToolbar mainToolbar;

    public MainFrame(ProgramState state) {
        this.state = state;
        tabPane = new JTabbedPane();
        tabPane.setTabPlacement(JTabbedPane.BOTTOM);
        tabPane.setOpaque(false);
        tabPane.setBorder(new EmptyBorder(0, 8, 8, 8));
        add(tabPane);

        contextView = new ContextEditor(state);
        latticeView = new LatticeView(state);
        associationView = new AssociationView(state);
        implicationView = new ImplicationView(state);
        addTab(tabPane, contextView, "Context", "Edit Context (CTRL + E)", 0);
        addTab(tabPane, latticeView, "Lattice", "Show Lattice (CTRL + L)", 1);
        addTab(tabPane, associationView, "Associations",
                "Calculate Associations (CTRL + A)", 2);
        addTab(tabPane, implicationView, "Implications",
                "Calculate Implications (CTRL + I)", 3);

        mainToolbar = new MainToolbar(this, state);
        mainToolbar.disableSaveButton();
        add(mainToolbar, BorderLayout.PAGE_START);

        setSize(1100, 600);
        setTitle("ConExp-NG - \"" + state.filePath + "\"");
    }

    protected void processWindowEvent(WindowEvent e) {

        if (e.getID() == WindowEvent.WINDOW_CLOSING) {
            new CloseAction().actionPerformed(null);
        } else
            super.processWindowEvent(e);

    }

    private void addTab(JTabbedPane t, View v, String title, String toolTip,
            int i) {
        t.insertTab("<html><body width='110' style='text-align:center'>"
                + title + "</body></html>", null, v, toolTip, i);
        KeyStroke shortcut = null;
        switch (i) {
        case 0: {
            shortcut = KeyStroke.getKeyStroke(KeyEvent.VK_E,
                    InputEvent.CTRL_DOWN_MASK);
            break;
        }
        case 1: {
            shortcut = KeyStroke.getKeyStroke(KeyEvent.VK_L,
                    InputEvent.CTRL_DOWN_MASK);
            break;
        }
        case 2: {
            shortcut = KeyStroke.getKeyStroke(KeyEvent.VK_A,
                    InputEvent.CTRL_DOWN_MASK);
            break;
        }
        case 3: {
            shortcut = KeyStroke.getKeyStroke(KeyEvent.VK_I,
                    InputEvent.CTRL_DOWN_MASK);
            break;
        }
        }
        t.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(shortcut, title);
        t.getActionMap().put(title, new SwitchTab(i, t));

    }

    @SuppressWarnings("serial")
    private class SwitchTab extends AbstractAction {

        private int tabnr;
        private JTabbedPane tabPane;

        public SwitchTab(int i, JTabbedPane tabs) {
            tabnr = i;
            tabPane = tabs;
        }

        @Override
        public void actionPerformed(ActionEvent arg0) {
            tabPane.setSelectedIndex(tabnr);
        }

    }

    @SuppressWarnings("serial")
    public class CloseAction extends AbstractAction {

        @Override
        public void actionPerformed(ActionEvent arg0) {
            if (state.unsavedChanges) {
                Object[] options = { "Yes", "No", "Cancel" };
                final JOptionPane optionPane = new JOptionPane(
                        "Do want to save the changes you made to the document?",
                        JOptionPane.QUESTION_MESSAGE,
                        JOptionPane.YES_NO_CANCEL_OPTION);
                optionPane.setOptions(options);
                final JDialog dialog = new JDialog(MainFrame.this,
                        "Document was modified", true);

                dialog.setContentPane(optionPane);
                optionPane
                        .addPropertyChangeListener(new PropertyChangeListener() {
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
                Util.centerDialogInsideMainFrame(MainFrame.this, dialog);
                dialog.setVisible(true);
                String n = (String) optionPane.getValue();
                if (n.equals("Yes")) {
                    // TODO: Question:
                    // if user selects cancel in the filedialog->exit?
                    mainToolbar.new SaveAction().actionPerformed(arg0);
                } else if (n.equals("Cancel")) {
                    return;
                }
            }
            System.exit(0);
        }
    }

}
