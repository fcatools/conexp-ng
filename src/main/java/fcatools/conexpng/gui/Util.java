package fcatools.conexpng.gui;

import javax.swing.*;
import java.awt.*;
import java.net.URL;

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

    public static JToggleButton createToggleButton(String title, String name, String iconPath) {
        JToggleButton b = new JToggleButton();
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

    public static final int clamp(int val, int min, int max) {
        return val < min ? min : (val > max ? max : val);
    }

    // "Real" modulo that works "correctly" for negative numbers
    public static int mod(int x, int y) {
        int result = x % y;
        if (result < 0)
        {
            result += y;
        }
        return result;
    }

}
