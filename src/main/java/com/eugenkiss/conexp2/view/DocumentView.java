package com.eugenkiss.conexp2.view;

import java.awt.BorderLayout;
import java.awt.Color;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JToolBar;

/**
 * The right view for example the lattice-draw-panel or the table
 * 
 * @author David
 * 
 */
public abstract class DocumentView extends JPanel {

	protected static final long serialVersionUID = -873702052790459127L;

	protected JToolBar toolbar = new JToolBar(JToolBar.VERTICAL);

	protected JComponent view, settings;

	protected JSplitPane splitPane;
	
	public abstract DocumentView getDocument();
	
	public abstract String getTabName();

	public abstract Icon getIcon();

	public abstract String getToolTip();
	
	public void addSplitPane() {
		splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		splitPane.setOneTouchExpandable(true);
		splitPane.add(settings, JSplitPane.LEFT);
		splitPane.add(view, JSplitPane.RIGHT);
		toolbar.setFloatable(false);
		view.add(toolbar);
		add(splitPane);
	}
	
	public void addToolbarElement(JButton element) {
		toolbar.add(element);
	}
}
