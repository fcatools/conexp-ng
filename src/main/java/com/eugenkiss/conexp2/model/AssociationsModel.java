package com.eugenkiss.conexp2.model;

import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JLabel;

import com.eugenkiss.conexp2.view.AssociationView;
import com.eugenkiss.conexp2.view.DocumentView;

public class AssociationsModel implements ConceptViewModel{

	public JComponent getSettings() {
		return new JLabel("Associations Settings");
	}

	public DocumentView getDocument() {
		return new AssociationView();
	}

	public String getTabName() {
		return "Association Rules";
	}

	public Icon getIcon() {
		return null;
	}

	public String getToolTip() {
		return "Assoziationen bla";
	}

}
