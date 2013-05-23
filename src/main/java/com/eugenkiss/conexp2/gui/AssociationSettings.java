package com.eugenkiss.conexp2.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class AssociationSettings extends JPanel {

	private static final long serialVersionUID = -3692280021161777005L;

	JLabel minSupLabel = new JLabel("Minimal Support 0.1");
	JSlider minSupSlider = new JSlider(0, 100, 10);

	JLabel confLabel = new JLabel("Confidence 0.5");
	JSlider confSlider = new JSlider(0, 100, 50);

	double minSup = 0.1, conf = 0.5;

	// Only for testing
	int current = 140, all = 200;

	PieChart piechart = new PieChart();

	public AssociationSettings() {
		setLayout(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.WEST;
		gbc.insets = new Insets(0, 0, 10, 0);
		gbc.gridx = 0;
		gbc.gridy = 0;
		add(minSupLabel, gbc);
		gbc.gridy = 1;
		add(minSupSlider, gbc);
		gbc.gridy = 2;
		add(confLabel, gbc);
		gbc.gridy = 3;
		add(confSlider, gbc);
		gbc.gridy = 4;
		gbc.gridheight = GridBagConstraints.REMAINDER;
		piechart.setPreferredSize(new Dimension(200, 200));
		add(piechart, gbc);

		confSlider.addChangeListener(new SliderListener(false));
		minSupSlider.addChangeListener(new SliderListener(true));
	}

	private class PieChart extends JPanel {

		private static final long serialVersionUID = -5226331499362209414L;

		@Override
		public void paint(Graphics g) {
			super.paint(g);
			int degree = (int) ((current * 360.0) / all);

			g.setColor(Color.BLUE);
			g.fillArc(25, 5, 140, 140, 0, 360);
			g.drawString("#Association Rules = " +all, 10, 165);
			
			g.setColor(Color.RED);
			g.fillArc(25, 5, 140, 140, 90, degree);
			
			g.drawString("#Assoc. Rules with minSup = " +current, 10, 180);

		}

	}

	private class SliderListener implements ChangeListener {

		private boolean minSup;

		public SliderListener(boolean minSup) {
			this.minSup = minSup;
		}

		public void stateChanged(ChangeEvent e) {
			JSlider slider = (JSlider) e.getSource();
			double value = slider.getValue() / 100.0;
			if (minSup) {
				minSupLabel.setText("Minimal Support " + value);
				AssociationSettings.this.minSup = value;
			} else {
				confLabel.setText("Confidence " + value);
				AssociationSettings.this.conf = value;
			}

			if (!slider.getValueIsAdjusting()) {
				current = 200-(minSupSlider.getValue() + confSlider.getValue());
				piechart.repaint();
			}
		}
	}
}
