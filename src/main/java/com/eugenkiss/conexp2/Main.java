package com.eugenkiss.conexp2;

import java.awt.GridLayout;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

public class Main extends JFrame {

	private static final long serialVersionUID = -3768163989667340886L;

	public static void main(String... args) {
		new Main();
	}

	Main() {
		
		// litte TabbedPane-test
		final JTabbedPane pane = new JTabbedPane();
		for (int i = 0; i < 4; i++) {
			String title = "Tab " + i;
			pane.insertTab(title, null, new TabComponent(i), null, i);
		}
		pane.setTabPlacement(JTabbedPane.BOTTOM);

		add(pane);
		setLocationRelativeTo(null);
		setSize(400, 200);
		setVisible(true);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
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
