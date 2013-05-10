package com.eugenkiss.conexp2;

import java.awt.BorderLayout;
import java.awt.GridLayout;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JToolBar;

public class Main extends JFrame {

	private static final long serialVersionUID = -3768163989667340886L;

	public static void main(String... args) {
		new Main();
	}

	Main() {
		
		addToolbar();
		

		JSplitPane mainSplitPane=new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		mainSplitPane.add(new JPanel(),JSplitPane.LEFT);
		mainSplitPane.setOneTouchExpandable(true);
		
		
		// litte TabbedPane-test
		final JTabbedPane tabPane = new JTabbedPane();
		for (int i = 0; i < 4; i++) {
			String title = "Tab " + i;
			tabPane.insertTab(title, null, new TabComponent(i), null, i);
		}
		tabPane.setTabPlacement(JTabbedPane.BOTTOM);
		mainSplitPane.add(tabPane, JSplitPane.RIGHT);
		// conexp benutzt einen BasePropertyChangeSupplier, mal angucken
		
		add(mainSplitPane);
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
		toolBar.add(button);

		button = new JButton("Open File");
		toolBar.add(button);

		button = new JButton("Save File");
		toolBar.add(button);

		toolBar.addSeparator();
		
		button = new JButton("Edit Context");
		toolBar.add(button);

		button = new JButton("Build Lattice");
		toolBar.add(button);

		button = new JButton("Attribute Exploration");
		toolBar.add(button);

		button = new JButton("Calc Implications");
		toolBar.add(button);

		button = new JButton("Calc Associations");
		toolBar.add(button);

		toolBar.addSeparator();
		
		String[] cstrings = { "Clear dependent", "Recalculate dependent" };
		JComboBox<String> combo = new JComboBox<String>(cstrings);
		toolBar.add(new JLabel("Update:"));
		toolBar.add(combo);
	}

	private class TabComponent extends JPanel {

		private static final long serialVersionUID = 8653733413060081454L;

		public TabComponent(final int i) {
			JLabel label = new JLabel() {

				private static final long serialVersionUID = -9116966818508859332L;

				public String getText() {

					return "Tab " + i;
				}
			};
			label.setHorizontalAlignment(JLabel.CENTER);
			setLayout(new GridLayout(1, 1));
			add(label);
		}

	}
}
