package fcatools.conexpng.gui;

import fcatools.conexpng.ProgramState;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

public class MainFrame extends JFrame {

    private static final long serialVersionUID = -3768163989667340886L;

    // Components
    private JTabbedPane tabPane;
    private View contextView;
    private View latticeView;
    private View associationView;
    private View implicationView;

    public MainFrame(ProgramState state) {
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

        MainToolbar mainToolbar = new MainToolbar(this);
        mainToolbar.disableSaveButton();
        add(mainToolbar, BorderLayout.PAGE_START);

        setSize(1100, 600);
        setTitle("ConExp-NG - \"" + state.filePath + "\"");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
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

}
