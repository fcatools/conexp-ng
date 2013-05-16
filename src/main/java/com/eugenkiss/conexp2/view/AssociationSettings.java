package com.eugenkiss.conexp2.view;

import java.awt.GridLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;

public class AssociationSettings extends JPanel {

	private static final long serialVersionUID = -3692280021161777005L;

	JLabel minSupLabel=new JLabel("Minimal Support 0.1");
	JSlider minSup = new JSlider(0, 100, 10);

	JLabel confLabel=new JLabel("Confidence 0.5");
	JSlider conf = new JSlider(0, 100, 50);
	
	public AssociationSettings() {
		setLayout(new GridLayout(5, 1));
		add(minSupLabel);
		add(minSup);
		add(confLabel);
		add(conf);
	}

	
}
