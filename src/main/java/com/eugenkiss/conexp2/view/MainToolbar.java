package com.eugenkiss.conexp2.view;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JTabbedPane;
import javax.swing.JToolBar;

import com.eugenkiss.conexp2.controller.MainToolbarButtonController;

public class MainToolbar extends JToolBar {

	private static final long serialVersionUID = -3495670613141172867L;

	public MainToolbar(JTabbedPane tabPane) {

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

		button = new JButton("Edit Context");
		button.setName("editContext");
		button.addActionListener(new MainToolbarButtonController(
				new ContextView(), tabPane));
		add(button);
		button.doClick();

		button = new JButton("Build Lattice");
		button.setName("buildLattice");
		button.addActionListener(new MainToolbarButtonController(
				new LatticeView(), tabPane));
		add(button);

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

		button = new JButton("Calc Associations");
		button.setName("calcAssociations");
		button.addActionListener(new MainToolbarButtonController(
				new AssociationView(), tabPane));
		add(button);

	}
}
