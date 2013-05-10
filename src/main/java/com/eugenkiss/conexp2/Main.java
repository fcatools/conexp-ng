package com.eugenkiss.conexp2;

import java.awt.BorderLayout;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JToolBar;

import com.eugenkiss.conexp2.controller.MainFrameController;
import com.eugenkiss.conexp2.model.ConceptViewModel;
import com.eugenkiss.conexp2.view.ContextView;
import com.eugenkiss.conexp2.view.DocumentView;

public class Main extends JFrame {

	private static final long serialVersionUID = -3768163989667340886L;

	private MainFrameController mfc = new MainFrameController(this);

	private JSplitPane mainSplitPane;
	
	public static void main(String... args) {
		new Main();
	}

	Main() {

		addToolbar();

		mainSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		mainSplitPane.setOneTouchExpandable(true);

		// litte TabbedPane-test
		final JTabbedPane tabPane = new JTabbedPane();
		tabPane.setTabPlacement(JTabbedPane.BOTTOM);
		mfc.setTabs(tabPane);

		ConceptViewModel conEdit= new ConceptViewModel() {
			
			public String getToolTip() {
				return "edit Context";
			}
			
			public String getTabName() {
				
				return "Context Editor";
			}
			
			public JComponent getSettings() {
				return new JLabel("Context Settings");
			}
			
			public Icon getIcon() {
				return null;
			}
			
			public DocumentView getDocument() {
				return new ContextView();
			}
		};
		tabPane.setOpaque(false);
		tabPane.insertTab(conEdit.getTabName(), conEdit.getIcon(), conEdit.getDocument(), conEdit.getToolTip(), 0);
		
		setLeftSplitPane(conEdit.getSettings());
		setRightSplitPane(tabPane);
		// conexp benutzt einen BasePropertyChangeSupplier, mal angucken

		add(mainSplitPane);
		setSize(1100, 600);
		setVisible(true);
		setTitle("Concept Explorer Reloaded");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}

	public void setLeftSplitPane(JComponent c){
		mainSplitPane.add(c, JSplitPane.LEFT);
	}
	
	public void setRightSplitPane(JComponent c){
		mainSplitPane.add(c, JSplitPane.RIGHT);
	}
	
	private void addToolbar() {
		JToolBar toolBar = new JToolBar();
		add(toolBar, BorderLayout.PAGE_START);

		JButton button = null;

		button = new JButton("New Context");
		button.setName("newContext");
		button.addActionListener(mfc);
		toolBar.add(button);

		button = new JButton("Open File");
		button.setName("openFile");
		button.addActionListener(mfc);
		toolBar.add(button);

		button = new JButton("Save File");
		button.setName("saveFile");
		button.addActionListener(mfc);
		toolBar.add(button);

		toolBar.addSeparator();

		button = new JButton("Edit Context");
		button.setName("editContext");
		button.addActionListener(mfc);
		toolBar.add(button);

		button = new JButton("Build Lattice");
		button.setName("buildLattice");
		button.addActionListener(mfc);
		toolBar.add(button);

		button = new JButton("Count Concepts");
		button.setName("countConcepts");
		button.addActionListener(mfc);
		toolBar.add(button);

		button = new JButton("Attribute Exploration");
		button.setName("attributeExploration");
		button.addActionListener(mfc);
		toolBar.add(button);

		button = new JButton("Calc Implications");
		button.setName("calcImplications");
		button.addActionListener(mfc);
		toolBar.add(button);

		button = new JButton("Calc Associations");
		button.setName("calcAssociations");
		button.addActionListener(mfc);
		toolBar.add(button);

		toolBar.addSeparator();

		String[] cstrings = { "Clear dependent", "Recalculate dependent" };
		JComboBox<String> combo = new JComboBox<String>(cstrings);
		combo.addActionListener(combo);
		toolBar.add(new JLabel("Update:"));
		toolBar.add(combo);
	}

}
