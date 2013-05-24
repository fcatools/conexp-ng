package com.eugenkiss.conexp2.gui;

import java.awt.BorderLayout;

import javax.swing.JFrame;
import javax.swing.JTabbedPane;

import com.eugenkiss.conexp2.ProgramState;

public class MainFrame extends JFrame {

    private static final long serialVersionUID = -3768163989667340886L;

    // GUI state
    private boolean unsavedChanges = false;

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
        add(tabPane);

        contextView = new ContextView(state);
        latticeView = new LatticeView(state);
        associationView = new AssociationView(state);
        implicationView = new ImplicationView(state);
        addTab(tabPane, contextView, "Context", "Edit Context", 0);
        addTab(tabPane, latticeView, "Lattice", "Edit Lattice", 1);
        addTab(tabPane, associationView, "Associations", "Calculate Associations", 2);
        addTab(tabPane, implicationView, "Implications", "Calculate Implications", 3);

        MainToolbar mainToolbar = new MainToolbar(this);
        mainToolbar.disableSaveButton();
        add(mainToolbar, BorderLayout.PAGE_START);

        setSize(1100, 600);
        setTitle("ConExp2 - \"" + state.filePath + "\"");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    public boolean isUnsavedChanges() {
        return unsavedChanges;
    }

    public void setUnsavedChanges(boolean unsavedChanges) {
        this.unsavedChanges = unsavedChanges;
    }

    private static void addTab(JTabbedPane t, View v, String title, String toolTip, int i) {
        t.insertTab("<html><body width='110' style='text-align:center'>"+title+"</body></html>",
                null, v, toolTip, i);
    }

}
