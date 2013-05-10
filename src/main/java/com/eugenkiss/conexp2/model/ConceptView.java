package com.eugenkiss.conexp2.model;

import javax.swing.Icon;
import javax.swing.JComponent;

public interface ConceptView {
	
	public JComponent getSettings();

	public DocumentView getDocument();

	public String getTabName();

	public Icon getIcon();

	public String getToolTip();
}
