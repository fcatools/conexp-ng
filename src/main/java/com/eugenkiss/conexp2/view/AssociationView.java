package com.eugenkiss.conexp2.view;

import javax.swing.Icon;
import javax.swing.JTextArea;

public class AssociationView extends DocumentView {

	private static final long serialVersionUID = -6377834669097012170L;

	public AssociationView() {
		view = new JTextArea("Hi");
		settings = new AssociationSettings();
		init();
	}

	public DocumentView getDocument() {
		return this;
	}

	public String getTabName() {
		return "Association Rules";
	}

	public Icon getIcon() {
		return null;
	}

	public String getToolTip() {
		return "calc Associations";
	}
}
