package fcatools.conexpng;

import javax.swing.*;

import com.alee.laf.button.WebButton;
import com.alee.laf.button.WebToggleButton;
import com.alee.laf.menu.WebMenuItem;
import com.alee.laf.menu.WebPopupMenu;
import com.alee.laf.optionpane.WebOptionPane;
import com.alee.laf.rootpane.WebDialog;
import com.alee.laf.rootpane.WebFrame;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.FileNotFoundException;
import java.net.URL;

public class Util {

    public static WebButton createButton(String tooltip, String name, String iconPath) {
        WebButton b = new WebButton();
        b.setDrawFocus(false);
        b.setToolTipText(tooltip);
        b.setName(name);
        b.setIcon(loadIcon(iconPath));
        return b;
    }

    public static WebButton createButton(String tooltip, String name, String iconPath, Action action) {
        WebButton b = createButton(tooltip, name, iconPath);
        b.addActionListener(action);
        return b;
    }

    public static ImageIcon loadIcon(String iconPath) {
        URL url = Util.class.getClassLoader().getResource(iconPath);
        ImageIcon icon = new ImageIcon(url);
        return icon;
    }

    public static WebToggleButton createToggleButton(String title, String name, String iconPath) {
        WebToggleButton b = new WebToggleButton();
        b.setToolTipText(title);
        b.setName(name);
        URL url = Util.class.getClassLoader().getResource(iconPath);
        ImageIcon icon = new ImageIcon(url);
        b.setIcon(icon);
        return b;
    }

    public static WebToggleButton createToggleButton(String tooltip, String name, String iconPath,
            ItemListener itemListener) {
        WebToggleButton b = createToggleButton(tooltip, name, iconPath);
        b.addItemListener(itemListener);
        return b;
    }

    public static void addMenuItem(WebPopupMenu menu, String name, ActionListener action) {
        WebMenuItem item = new WebMenuItem(name);
        menu.add(item);
        item.addActionListener(action);
    }

    // Needed as 'setLocationRelativeTo' doesn't work properly in a
    // multi-monitor setup
    public static void centerDialogInsideMainFrame(WebFrame parent, WebDialog dialog) {
        Dimension dialogSize = dialog.getSize();
        Dimension frameSize = parent.getSize();
        Point frameLocation = parent.getLocation();
        int x = frameLocation.x + ((frameSize.width - dialogSize.width) / 2);
        int y = frameLocation.y + ((frameSize.height - dialogSize.height) / 2);
        dialog.setLocation(x, y);
    }

    public static void handleIOExceptions(WebFrame parent, Exception ex, String path) {
        if (ex instanceof FileNotFoundException) {
            showMessageDialog(parent, "Can not find this file: " + path, true);
        } else {
            showMessageDialog(parent, "The file seems to be corrupt: " + ex.getMessage(), true);
        }
    }

    public static void showMessageDialog(WebFrame parent, String message, boolean error) {
        final WebOptionPane pane = new WebOptionPane(message);
        Object[] options = { "Okay" };
        pane.setOptions(options);
        pane.setMessageType(error ? WebOptionPane.ERROR_MESSAGE : WebOptionPane.INFORMATION_MESSAGE);
        final WebDialog dialog = new WebDialog(parent, error ? "Error" : "Message");
        pane.addPropertyChangeListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent e) {
                if (dialog.isVisible() && (e.getSource() == pane)
                        && (e.getPropertyName().equals(JOptionPane.VALUE_PROPERTY))) {
                    dialog.setVisible(false);
                }
            }
        });
        dialog.setModal(true);
        dialog.setContentPane(pane);
        dialog.pack();
        centerDialogInsideMainFrame(parent, dialog);
        dialog.setVisible(true);
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
        if (result < 0) {
            result += y;
        }
        return result;
    }

}
