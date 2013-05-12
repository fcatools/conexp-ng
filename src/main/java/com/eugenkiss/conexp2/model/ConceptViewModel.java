package com.eugenkiss.conexp2.model;

import javax.swing.Icon;
import javax.swing.JComponent;

import com.eugenkiss.conexp2.view.DocumentView;

/**
 * Maybe Model is the wrong name, since it knows the Views
 * 
 * @author David
 * 
 */
public interface ConceptViewModel{

	public JComponent getSettings();

	public DocumentView getDocument();
	
	public String getTabName();

	public Icon getIcon();

	public String getToolTip();
}
