package com.eugenkiss.conexp2.gui;

import java.awt.BorderLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTree;

import com.eugenkiss.conexp2.ProgramState;

public class ContextView extends View {

    private static final long serialVersionUID = 1660117627650529212L;

	public ContextView(ProgramState state) {
		super(state);
		view = new JLabel("Hier Tabelle");
		AccordionMenue acc = new AccordionMenue();
		acc.addMenueEntry("Punkt 1", new JLabel("Test"));
		
		JPanel tree = new JPanel(new BorderLayout());
		tree.add(new JTree());
		acc.addMenueEntry("Punkt 2", tree);
		
		acc.addMenueEntry("Punkt 3", new JLabel("Hallo"));
		settings = acc;
		super.init();
	}
}
