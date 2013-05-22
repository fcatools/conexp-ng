package com.eugenkiss.conexp2.gui;

import java.awt.BorderLayout;

import javax.swing.BorderFactory;
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

	protected JPanel panel;

	protected JSplitPane splitPane;

	public abstract View getDocument();

	public abstract String getTabName();

	public abstract Icon getIcon();

	public abstract String getToolTip();

	public void init() {
		setLayout(new BorderLayout());

		toolbar.setFloatable(false);
		
		panel = new JPanel();
		panel.setLayout(new BorderLayout());
		panel.add(toolbar, BorderLayout.WEST);
		panel.add(view, BorderLayout.CENTER);
		
		// Important to make split pane divider properly visible on osx
        settings.setBorder(BorderFactory.createLoweredSoftBevelBorder());
        panel.setBorder(BorderFactory.createLoweredSoftBevelBorder());
		
		splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, settings, panel);
		splitPane.setOneTouchExpandable(true);
        splitPane.setBorder(null);
		add(splitPane);
	}

	public void addToolbarElement(JButton element) {
		toolbar.add(element);
	}
}
