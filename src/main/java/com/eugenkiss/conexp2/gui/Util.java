package com.eugenkiss.conexp2.gui;

import java.awt.Dimension;
import java.awt.Point;
import java.net.URL;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;

public class Util {
	
	public static JButton createButton(String title, String name, String iconPath) {
		JButton b = new JButton();
		b.setToolTipText(title);
		b.setName(name);
		URL url = Util.class.getClassLoader().getResource(iconPath);
		ImageIcon icon = new ImageIcon(url);
		b.setIcon(icon);
		return b;
	}
	
	// Needed as 'setLocationRelativeTo' doesn't work properly in a multi-monitor setup
	public static void centerDialogInsideMainFrame(JFrame parent, JDialog dialog) {
        Dimension dialogSize = dialog.getSize();
        Dimension frameSize = parent.getSize();
        Point frameLocation = parent.getLocation();
        int x = frameLocation.x + ((frameSize.width - dialogSize.width) / 2);
        int y = frameLocation.y + ((frameSize.height - dialogSize.height) / 2);
        dialog.setLocation(x, y);	
	}

}
