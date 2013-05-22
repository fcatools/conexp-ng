package com.eugenkiss.conexp2.gui;

import javax.swing.Icon;
import javax.swing.JLabel;

public class ContextView extends DocumentView {

	private static final long serialVersionUID = 1660117627650529212L;

	public ContextView() {
		view = new JLabel("Hier Tabelle");
		settings = new JLabel("Context Settings");
		init();
	}

	public String getToolTip() {
		return "Edit Context";
	}

	public String getTabName() {

		return "Context Editor";
	}

	public Icon getIcon() {
		return null;
	}

	public DocumentView getDocument() {
		return new ContextView();
	}
}
