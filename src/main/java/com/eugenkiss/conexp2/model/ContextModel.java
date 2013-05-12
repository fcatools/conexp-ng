package com.eugenkiss.conexp2.model;

import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JLabel;

import com.eugenkiss.conexp2.view.ContextView;
import com.eugenkiss.conexp2.view.DocumentView;

public class ContextModel implements ConceptViewModel {

	public String getToolTip() {
		return "edit Context";
	}
	
	public String getTabName() {
		
		return "Context Editor";
	}
	
	public JComponent getSettings() {
		return new JLabel("Context Settings");
	}
	
	public Icon getIcon() {
		return null;
	}
	
	public DocumentView getDocument() {
		return new ContextView();
	}

}
