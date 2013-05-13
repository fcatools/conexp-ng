package com.eugenkiss.conexp2;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTabbedPane;
import javax.swing.JToolBar;

import com.eugenkiss.conexp2.controller.MainToolbarButtonController;
import com.eugenkiss.conexp2.model.AssociationsModel;
import com.eugenkiss.conexp2.model.ConceptViewModel;
import com.eugenkiss.conexp2.model.ContextModel;
import com.eugenkiss.conexp2.model.LatticeModel;

public class Main extends JFrame {

	private static final long serialVersionUID = -3768163989667340886L;

	private JTabbedPane tabPane = new JTabbedPane();

	public static void main(String... args) {
		new Main();
	}

	Main() {

		addToolbar();

		tabPane.setTabPlacement(JTabbedPane.BOTTOM);
		tabPane.setOpaque(false);
		add(tabPane);

		// conexp benutzt einen BasePropertyChangeSupplier, mal angucken

		setSize(1100, 600);
		setVisible(true);
		setTitle("Concept Explorer Reloaded");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}

	private void addToolbar() {
		JToolBar toolBar = new JToolBar();
		add(toolBar, BorderLayout.PAGE_START);

		JButton button = null;

		button = new JButton("New Context");
		button.setName("newContext");
		toolBar.add(button);

		button = new JButton("Open File");
		button.setName("openFile");
		toolBar.add(button);

		button = new JButton("Save File");
		button.setName("saveFile");
		toolBar.add(button);

		toolBar.addSeparator();

		button = new JButton("Edit Context");
		button.setName("editContext");
		ConceptViewModel conEdit = new ContextModel();
		button.addActionListener(new MainToolbarButtonController(conEdit,
				tabPane));
		toolBar.add(button);
		button.doClick();

		button = new JButton("Build Lattice");
		button.setName("buildLattice");
		ConceptViewModel lattice = new LatticeModel();
		button.addActionListener(new MainToolbarButtonController(lattice,
				tabPane));
		toolBar.add(button);

		button = new JButton("Count Concepts");
		button.setName("countConcepts");
		button.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent arg0) {
				JOptionPane.showMessageDialog(Main.this,
						"Es sind so und so viele Concepts");
			}
		});
		toolBar.add(button);

		button = new JButton("Attribute Exploration");
		button.setName("attributeExploration");
		button.addActionListener(new ActionListener() {
			// Maybe it is an own class worth
			public void actionPerformed(ActionEvent arg0) {

				Object[] options = { "Yes", "No", "WTF" };
				int choice;
				do {
					choice = JOptionPane.showOptionDialog(Main.this,
							"Wenn das Objekt das und das hat...",
							"A Silly Question",
							JOptionPane.YES_NO_CANCEL_OPTION,
							JOptionPane.QUESTION_MESSAGE, null, options,
							options[2]);
				} while (choice != 2);
			}
		});
		toolBar.add(button);

		button = new JButton("Calc Implications");
		button.setName("calcImplications");
		
		toolBar.add(button);

		button = new JButton("Calc Associations");
		button.setName("calcAssociations");
		ConceptViewModel am=new AssociationsModel();
		button.addActionListener(new MainToolbarButtonController(am,tabPane));
		toolBar.add(button);

		toolBar.addSeparator();

		String[] cstrings = { "Clear dependent", "Recalculate dependent" };
		JComboBox<String> combo = new JComboBox<String>(cstrings);
		combo.addActionListener(combo);
		toolBar.add(new JLabel("Update:"));
		toolBar.add(combo);
	}

}
