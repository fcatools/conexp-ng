package com.eugenkiss.conexp2.controller;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JTabbedPane;

import com.eugenkiss.conexp2.view.DocumentView;

public class MainToolbarButtonController implements ActionListener {

	private JTabbedPane tabs;
	private DocumentView cvm;

	private boolean firstStart = true;
	private int TabNumber;

	public MainToolbarButtonController(DocumentView cvm, JTabbedPane tabPane) {
		this.cvm = cvm;
		tabs = tabPane;
	}

	public void actionPerformed(ActionEvent arg0) {
		if (firstStart) {

			TabNumber = tabs.getTabCount();
			tabs.insertTab(cvm.getTabName(), cvm.getIcon(), cvm.getDocument(),
					cvm.getToolTip(), TabNumber);

			firstStart = false;
		}
		tabs.setSelectedIndex(TabNumber);
	}
}
