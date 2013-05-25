package com.eugenkiss.conexp2.gui;

import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import com.eugenkiss.conexp2.ProgramState;

public class ImplicationView extends View {

    private static final long serialVersionUID = -6377834669097012170L;

	public ImplicationView(ProgramState state) {
		super(state);
		view = new JScrollPane(new JTextArea());
		settings = new AssociationSettings();
		super.init();
	}

}
