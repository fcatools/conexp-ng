package com.eugenkiss.conexp2.gui;

import javax.swing.Icon;
import javax.swing.JTextArea;

public class ImplicationView extends View {

	private static final long serialVersionUID = -6377834669097012170L;

	public ImplicationView() {
		view = new JTextArea("Hi");
		settings = new AssociationSettings();
		init();
	}

	public View getDocument() {
		return this;
	}

	public String getTabName() {
		return "Implication Rules";
	}

	public Icon getIcon() {
		return null;
	}

	public String getToolTip() {
		return "calc implications";
	}
}
