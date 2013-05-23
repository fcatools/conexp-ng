package com.eugenkiss.conexp2.gui;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JToolBar;

public class MainToolbar extends JToolBar {

	private static final long serialVersionUID = -3495670613141172867L;

	private final JFrame mainFrame;
	private final JButton saveButton;

	public MainToolbar(final JFrame mainFrame) {
		this.mainFrame = mainFrame;
		this.setFloatable(false);
		
		JButton button = null;

		button = new JButton("New");
		button.setName("newContext");
		add(button);

		button = new JButton("Open");
		button.setName("openFile");
		add(button);

		saveButton = new JButton("Save");
		saveButton.setName("saveFile");
		// TODO: Maybe create an (public) Action since we want to also save from the exit prompt
		//       when there are unsaved changes.
		saveButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				JOptionPane.showMessageDialog(MainToolbar.this,
						"Save");
			}
		});
		add(saveButton);

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
				showMessageDialog("10 Concepts");
			}
		});
		add(button);

		button = new JButton("Attribute Exploration");
		button.setName("attributeExploration");
		button.addActionListener(new ActionListener() {
			// Maybe it is an own class worth
			public void actionPerformed(ActionEvent arg0) {

				// TODO: Placeholder
				showMessageDialog("10 Concepts");
				/*
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
				*/
			}
		});
		add(button);
		
		addSeparator();
		
		button = new JButton("Help");
		button.setName("help");
		add(button);
	}
	
	public void enableSaveButton() {
		saveButton.setEnabled(true);
	}

	public void disableSaveButton() {
		saveButton.setEnabled(false);
	}

	// TODO: Needs to be improved
	private void showMessageDialog(String message) {
		JOptionPane pane = new JOptionPane();
	    JDialog dialog = pane.createDialog(mainFrame, message);
	    dialog.pack();
        centerDialogInsideMainFrame(dialog);
        dialog.setVisible(true);
	}
	
	// Needed as 'setLocationRelativeTo' doesn't work properly in a multi-monitor setup
	private void centerDialogInsideMainFrame(JDialog dialog) {
        Dimension dialogSize = dialog.getSize();
        Dimension frameSize = mainFrame.getSize();
        Point frameLocation = mainFrame.getLocation();
        int x = frameLocation.x + ((frameSize.width - dialogSize.width) / 2);
        int y = frameLocation.y + ((frameSize.height - dialogSize.height) / 2);
        dialog.setLocation(x, y);	
	}
	
}
