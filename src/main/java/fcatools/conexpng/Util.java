package fcatools.conexpng;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemListener;
import java.net.URL;

public class Util {

    public static JButton createButton(String title, String name, String iconPath) {
        JButton b = new JButton();
        b.setToolTipText(title);
        b.setName(name);
        b.setIcon(loadIcon(iconPath));
        return b;
    }

    public static ImageIcon loadIcon(String iconPath) {
        URL url = Util.class.getClassLoader().getResource(iconPath);
        ImageIcon icon = new ImageIcon(url);
        return icon;
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

    public static void addMenuItem(JPopupMenu menu, String name, ActionListener action) {
        JMenuItem item = new JMenuItem(name);
        menu.add(item);
        item.addActionListener(action);
    }

    public static void addToolbarButton(JToolBar toolbar, String name, String tooltip, String iconPath, Action action) {
        JButton b = createButton(tooltip, name, iconPath);
        toolbar.add(b);
        b.addActionListener(action);
    }

    public static void addToolbarToggleButton(JToolBar toolbar, String name, String tooltip, String iconPath, ItemListener itemListener) {
        JToggleButton b = createToggleButton(tooltip, name, iconPath);
        toolbar.add(b);
        b.addItemListener(itemListener);
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

    public static void invokeAction(ActionEvent e, Action action) {
        action.actionPerformed(e);
    }

    public static void invokeAction(JComponent source, Action action) {
        invokeAction(new ActionEvent(source, 0, ""), action);
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
