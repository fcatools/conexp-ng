package com.eugenkiss.conexp2.view;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
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

	protected JComponent view;

	public DocumentView() {
		toolbar.setFloatable(false);
		add(toolbar);
	}

	public void addToolbarElement(JButton element) {
		toolbar.add(element);
	}
}
