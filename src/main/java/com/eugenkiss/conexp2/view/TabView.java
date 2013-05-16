package com.eugenkiss.conexp2.view;

import javax.swing.Icon;

/**
 * 
 * @author David
 * 
 */
public interface TabView{

	public DocumentView getDocument();
	
	public String getTabName();

	public Icon getIcon();

	public String getToolTip();
}
