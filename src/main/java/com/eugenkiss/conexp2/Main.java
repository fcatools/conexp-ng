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
import com.eugenkiss.conexp2.view.AssociationView;
import com.eugenkiss.conexp2.view.ContextView;
import com.eugenkiss.conexp2.view.LatticeView;
import com.eugenkiss.conexp2.view.MainToolbar;
import com.eugenkiss.conexp2.view.TabView;

public class Main extends JFrame {

	private static final long serialVersionUID = -3768163989667340886L;

	private JTabbedPane tabPane = new JTabbedPane();

	public static void main(String... args) {
		new Main();
	}

	Main() {

		add(new MainToolbar(tabPane), BorderLayout.PAGE_START);

		tabPane.setTabPlacement(JTabbedPane.BOTTOM);
		tabPane.setOpaque(false);
		add(tabPane);

		// conexp benutzt einen BasePropertyChangeSupplier, mal angucken

		setSize(1100, 600);
		setVisible(true);
		setTitle("Concept Explorer Reloaded");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}

}
