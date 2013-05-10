package com.eugenkiss.conexp2.model;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JToolBar;

public abstract class DocumentView extends JComponent {

	private static final long serialVersionUID = -873702052790459127L;

	private JToolBar toolbar = new JToolBar(JToolBar.VERTICAL);

	private JComponent view;

	public DocumentView() {
		add(toolbar);
	}

	public void addToolbarElement(JButton element) {
		toolbar.add(element);
	}
}
