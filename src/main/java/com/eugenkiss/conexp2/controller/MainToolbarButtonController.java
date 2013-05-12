package com.eugenkiss.conexp2.controller;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import com.eugenkiss.conexp2.model.ConceptViewModel;

public class MainToolbarButtonController implements ActionListener {

	private JTabbedPane tabs;
	private ConceptViewModel cvm;

	private boolean firstStart = true;
	private int TabNumber;

	public MainToolbarButtonController(ConceptViewModel cvm, JTabbedPane tabPane) {
		this.cvm = cvm;
		tabs = tabPane;
	}

	public void actionPerformed(ActionEvent arg0) {
		if (arg0.getSource() instanceof JButton) {
			if (firstStart) {
				JSplitPane splitPane = new JSplitPane(
						JSplitPane.HORIZONTAL_SPLIT);
				splitPane.setOneTouchExpandable(true);
				splitPane.add(cvm.getSettings(), JSplitPane.LEFT);
				splitPane.add(cvm.getDocument(), JSplitPane.RIGHT);
				TabNumber = tabs.getTabCount();
				tabs.insertTab(cvm.getTabName(), cvm.getIcon(), splitPane,
						cvm.getToolTip(), TabNumber);
				
				firstStart = false;
			}
			tabs.setSelectedIndex(TabNumber);
		}

		else if (arg0.getSource() instanceof JComboBox<?>) {

		}

	}
}
