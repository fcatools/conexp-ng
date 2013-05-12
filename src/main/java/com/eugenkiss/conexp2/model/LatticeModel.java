package com.eugenkiss.conexp2.model;

import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JLabel;

import com.eugenkiss.conexp2.view.DocumentView;
import com.eugenkiss.conexp2.view.LatticeView;

public class LatticeModel implements ConceptViewModel {

	public String getToolTip() {
		return "Lattice test";
	}

	public String getTabName() {
		return "Lattice line diagram";
	}

	public JComponent getSettings() {
		return new JLabel("Lattice Settings");
	}

	public Icon getIcon() {
		return null;
	}

	public DocumentView getDocument() {

		return new LatticeView();
	}

}
