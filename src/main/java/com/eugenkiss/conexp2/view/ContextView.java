package com.eugenkiss.conexp2.view;

import javax.swing.JLabel;


public class ContextView extends DocumentView {

	private static final long serialVersionUID = 1660117627650529212L;

	public ContextView() {
		super();
		view = new JLabel("Hier Tabelle");
		add(view);
	}

}
