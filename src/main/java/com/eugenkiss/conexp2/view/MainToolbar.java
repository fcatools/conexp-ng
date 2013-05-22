package com.eugenkiss.conexp2.view;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JTabbedPane;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;

public class MainToolbar extends JToolBar {

	private static final long serialVersionUID = -3495670613141172867L;

	private JTabbedPane tabs;

	public MainToolbar(JTabbedPane tabPane) {
		this.tabs = tabPane;
		JButton button = null;

		button = new JButton("New Context");
		button.setName("newContext");
		add(button);

		button = new JButton("Open File");
		button.setName("openFile");
		add(button);

		button = new JButton("Save File");
		button.setName("saveFile");
		add(button);

		addSeparator();

		addButton(new ContextView(), KeyStroke.getKeyStroke(KeyEvent.VK_E,
				InputEvent.CTRL_DOWN_MASK), true);

		addButton(new LatticeView(), KeyStroke.getKeyStroke(KeyEvent.VK_L,
				InputEvent.CTRL_DOWN_MASK), false);

		button = new JButton("Count Concepts");
		button.setName("countConcepts");
		button.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent arg0) {
				JOptionPane.showMessageDialog(MainToolbar.this,
						"Es sind so und so viele Concepts");
			}
		});
		add(button);

		button = new JButton("Attribute Exploration");
		button.setName("attributeExploration");
		button.addActionListener(new ActionListener() {
			// Maybe it is an own class worth
			public void actionPerformed(ActionEvent arg0) {

				Object[] options = { "Yes", "No", "WTF" };
				int choice;
				do {
					choice = JOptionPane.showOptionDialog(MainToolbar.this,
							"Wenn das Objekt das und das hat...",
							"A Silly Question",
							JOptionPane.YES_NO_CANCEL_OPTION,
							JOptionPane.QUESTION_MESSAGE, null, options,
							options[2]);
				} while (choice != 2);
			}
		});
		add(button);

		button = new JButton("Calc Implications");
		button.setName("calcImplications");

		add(button);

		addButton(new AssociationView(), KeyStroke.getKeyStroke(KeyEvent.VK_A,
				InputEvent.CTRL_DOWN_MASK), false);
	}

	private void addButton(DocumentView dv, KeyStroke ks, boolean startView) {
		JButton button = new JButton();
		Action click = new ShowDocument(dv);
		button.setAction(click);
		button.setName(dv.getToolTip());
		button.setText(dv.getToolTip());
		getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
				.put(ks, button.getName());
		getActionMap().put(button.getName(), click);
		add(button);
		if (startView)
			button.doClick();
	}

	private class ShowDocument extends AbstractAction {

		private static final long serialVersionUID = 45594299517529193L;
		private DocumentView dv;

		private boolean firstStart = true;
		private int tabNumber;

		public ShowDocument(DocumentView dv) {
			this.dv = dv;
		}

		public void actionPerformed(ActionEvent e) {
			if (firstStart) {

				tabNumber = tabs.getTabCount();
				tabs.insertTab(dv.getTabName(), dv.getIcon(), dv.getDocument(),
						dv.getToolTip(), tabNumber);
				firstStart = false;
			}
			tabs.setSelectedIndex(tabNumber);
		}
	}

}
