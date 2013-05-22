package com.eugenkiss.conexp2.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JTabbedPane;
import javax.swing.JToolBar;

public class MainToolbar extends JToolBar {

	private static final long serialVersionUID = -3495670613141172867L;

	private JTabbedPane tabs;

	public MainToolbar(JTabbedPane tabPane) {
		this.tabs = tabPane;
		JButton button = null;

		button = new JButton("New");
		button.setName("newContext");
		add(button);

		button = new JButton("Open");
		button.setName("openFile");
		add(button);

		button = new JButton("Save");
		button.setName("saveFile");
		add(button);

		button = new JButton("Save as");
		button.setName("saveAsFile");
		add(button);
		
		button = new JButton("Undo");
		button.setName("undo");
		add(button);
		
		button = new JButton("Redo");
		button.setName("redo");
		add(button);
		
		addSeparator();

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
		
		addSeparator();
		
		button = new JButton("Help");
		button.setName("help");
		add(button);
	}

}
