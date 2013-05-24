package com.eugenkiss.conexp2.gui;

import javax.swing.JLabel;

import com.eugenkiss.conexp2.ProgramState;

public class LatticeView extends View {
	private static final long serialVersionUID = 1660117627650529212L;

	public LatticeView(ProgramState state) {
		super(state);
		view = new JLabel("Hier Verbanddiagramm");
		settings = new JLabel("Lattice Settings");
		super.init();
	}

}
