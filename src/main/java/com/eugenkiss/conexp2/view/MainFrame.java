package com.eugenkiss.conexp2.view;

import java.awt.BorderLayout;

import javax.swing.JFrame;
import javax.swing.JTabbedPane;

public class MainFrame extends JFrame {
	

	private static final long serialVersionUID = -3768163989667340886L;

	private JTabbedPane tabPane = new JTabbedPane();
	
	public MainFrame() {
		tabPane.setTabPlacement(JTabbedPane.BOTTOM);
		tabPane.setOpaque(false);
		add(tabPane);
		add(new MainToolbar(tabPane), BorderLayout.PAGE_START);

		

		setSize(1100, 600);
		setVisible(true);
		setTitle("Concept Explorer Reloaded");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}
	
}
