package com.eugenkiss.conexp2;

import javax.swing.JFrame;
import javax.swing.JLabel;

public class Main extends JFrame {
	
	private static final long serialVersionUID = -3768163989667340886L;

	public static void main(String... args) {
		new Main();
	}
	
	Main() {
		JLabel lbl = new JLabel("Hello World");
		add(lbl);
		this.setSize(100, 100);
		setVisible(true);
	}

}
