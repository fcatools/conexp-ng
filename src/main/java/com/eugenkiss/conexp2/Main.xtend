package com.eugenkiss.conexp2

import javax.swing.JFrame
import javax.swing.JLabel

class Main extends JFrame {
	
	def static main(String... args) {
		new Main
	}
	
	new() {
		this => [
			add(new JLabel("Hello World"))
			setSize(100,100)
			setVisible(true)
		]
	}
	
}