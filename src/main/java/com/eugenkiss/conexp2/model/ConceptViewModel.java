package com.eugenkiss.conexp2.model;

import javax.swing.Icon;
import javax.swing.JComponent;

import com.eugenkiss.conexp2.view.DocumentView;

/**
 * For the lattice view, the concepttable and so on
 * 
 * @author David
 * 
 */
public interface ConceptViewModel {

	public JComponent getSettings();

	public DocumentView getDocument();

	public String getTabName();

	public Icon getIcon();

	public String getToolTip();
}
