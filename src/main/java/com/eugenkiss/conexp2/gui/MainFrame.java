package com.eugenkiss.conexp2.gui;

import java.awt.BorderLayout;

import javax.swing.JFrame;
import javax.swing.JTabbedPane;

public class MainFrame extends JFrame {

	private static final long serialVersionUID = -3768163989667340886L;

	private JTabbedPane tabPane;
	private View contextView;
	private View latticeView;
	private View associationView;
	private View implicationView;
	
	// GUI state
	private boolean unsavedChanges = false;
	
	public MainFrame() {
		tabPane = new JTabbedPane();
		tabPane.setTabPlacement(JTabbedPane.BOTTOM);
		tabPane.setOpaque(false);
		add(tabPane);
		
		contextView = new ContextView();
		latticeView = new LatticeView();
		associationView = new AssociationView();
		implicationView = new ImplicationView();
		addTab(tabPane, contextView, 0);
		addTab(tabPane, latticeView, 1);
		addTab(tabPane, associationView, 2);
		addTab(tabPane, implicationView, 3);
		
		MainToolbar mainToolbar = new MainToolbar(this);
		mainToolbar.disableSaveButton();
		add(mainToolbar, BorderLayout.PAGE_START);

		setSize(1100, 600);
		setTitle("ConExp2 - ../example.cex");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}
	
	public boolean isUnsavedChanges() {
		return unsavedChanges;
	}

	public void setUnsavedChanges(boolean unsavedChanges) {
		this.unsavedChanges = unsavedChanges;
	}

	private static void addTab(JTabbedPane t, View v, int i) {
		t.insertTab(v.getTabName(), v.getIcon(), v.getDocument(), v.getToolTip(), i);
	}
	
}
