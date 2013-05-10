package com.eugenkiss.conexp2.controller;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.eugenkiss.conexp2.Main;
import com.eugenkiss.conexp2.model.ConceptViewModel;
import com.eugenkiss.conexp2.view.DocumentView;
import com.eugenkiss.conexp2.view.LatticeView;

public class MainFrameController implements ActionListener, ChangeListener {

	private JTabbedPane tabs;
private Main frame;
	
	
	public MainFrameController(Main frame) {
		this.frame=frame;
	}

	public void actionPerformed(ActionEvent arg0) {
		if (arg0.getSource() instanceof JButton) {
			if (((JButton) arg0.getSource()).getName().equals("buildLattice")) {
				ConceptViewModel lattice = new ConceptViewModel() {

					public String getToolTip() {
						return "Lattice test";
					}

					public String getTabName() {
						return "Lattice line diagram";
					}

					public JComponent getSettings() {
						return new JLabel("Lattice Settings");
					}

					public Icon getIcon() {
						return null;
					}

					public DocumentView getDocument() {

						return new LatticeView();
					}
				};

				tabs.insertTab(lattice.getTabName(), lattice.getIcon(),
						lattice.getDocument(), lattice.getToolTip(), 1);
				tabs.setSelectedIndex(1);
				frame.setLeftSplitPane(lattice.getSettings());
			}
		}

		else if (arg0.getSource() instanceof JComboBox<?>) {

		}

	}

	public void setTabs(JTabbedPane tabs) {
		this.tabs = tabs;
		tabs.addChangeListener(this);
	}

	public void stateChanged(ChangeEvent arg0) {
		// Tab wird ja richtig gewechselt, aber das Settingspanel muss noch
		// mit geändert werden
		tabs.getSelectedIndex();
	}

}
