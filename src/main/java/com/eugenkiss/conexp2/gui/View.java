package com.eugenkiss.conexp2.gui;

import java.awt.BorderLayout;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JToolBar;


public abstract class View extends JPanel {

	protected static final long serialVersionUID = -873702052790459127L;

	protected JToolBar toolbar = new JToolBar(JToolBar.VERTICAL);

	protected JComponent view, settings;

	protected JPanel document;

	protected JSplitPane splitPane;

	public abstract View getDocument();

	public abstract String getTabName();

	public abstract Icon getIcon();

	public abstract String getToolTip();

	public void init() {
		setLayout(new BorderLayout());

		document = new JPanel();
		toolbar.setFloatable(false);
		document.setLayout(new BorderLayout());
		document.add(toolbar, BorderLayout.WEST);
		document.add(view, BorderLayout.CENTER);
		
		splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		splitPane.setOneTouchExpandable(true);
		splitPane.add(settings, JSplitPane.LEFT);
		splitPane.add(document, JSplitPane.RIGHT);
		add(splitPane);
	}

	public void addToolbarElement(JButton element) {
		toolbar.add(element);
	}
}
