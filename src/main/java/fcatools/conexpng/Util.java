package fcatools.conexpng;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;

import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.xml.stream.XMLStreamException;

import org.apache.batik.transcoder.TranscoderException;

import com.alee.laf.button.WebButton;
import com.alee.laf.button.WebToggleButton;
import com.alee.laf.menu.WebMenuItem;
import com.alee.laf.menu.WebPopupMenu;
import com.alee.laf.optionpane.WebOptionPane;
import com.alee.laf.rootpane.WebDialog;
import com.alee.laf.rootpane.WebFrame;

import fcatools.conexpng.io.locale.LocaleHandler;

public class Util {

    /**
     * Enumeration that encapsulates all classes where I/O errors could happen
     * 
     * @author Torsten Casselt
     */
    public static enum FileOperationType {
        EXPORT, OPEN, SAVE, GUI
    };

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

    /**
     * Shows error messages for various I/O errors.
     * 
     * @param parent
     *            to attach error dialogs to
     * @param ex
     *            exception that occurred
     * @param path
     *            path that were accessed when exception occurred
     * @param fot
     *            file operation type when error occurred
     */
    public static void handleIOExceptions(WebFrame parent, Exception ex, String path, FileOperationType fot) {
        String errorMessage;
        if (fot.equals(FileOperationType.GUI)) {
            showMessageDialog(parent, LocaleHandler.getString("Util.handleIOExceptions.GUI") + path.concat(".gui"),
                    true);
        } else if (ex instanceof FileNotFoundException) {
            showMessageDialog(parent, LocaleHandler.getString("Util.handleIOExceptions.FileNotFoundException") + path,
                    true);
        } else if (ex instanceof IOException) {
            errorMessage = fot.equals(FileOperationType.OPEN) ? LocaleHandler
                    .getString("Util.handleIOExceptions.open.IOException") + ex.getMessage() : LocaleHandler
                    .getString("Util.handleIOExceptions.save.IOException") + ex.getMessage();
            showMessageDialog(parent, errorMessage, true);
        } else if (ex instanceof TranscoderException) {
            showMessageDialog(parent,
                    LocaleHandler.getString("Util.handleIOExceptions.TranscoderException") + ex.getMessage(), true);
        } else if (ex instanceof XMLStreamException) {
            errorMessage = fot.equals(FileOperationType.OPEN) ? LocaleHandler
                    .getString("Util.handleIOExceptions.open.XMLStreamException") + ex.getMessage() : LocaleHandler
                    .getString("Util.handleIOExceptions.save.XMLStreamException") + ex.getMessage();
            showMessageDialog(parent, errorMessage, true);
        } else {
            errorMessage = fot.equals(FileOperationType.OPEN) ? LocaleHandler
                    .getString("Util.handleIOExceptions.open.otherExceptions") + ex.getMessage() : LocaleHandler
                    .getString("Util.handleIOExceptions.otherExceptions") + ex.getMessage();
            showMessageDialog(parent, errorMessage, true);
        }
        ex.printStackTrace();
    }

    public static void showMessageDialog(WebFrame parent, String message, boolean error) {
        final WebOptionPane pane = new WebOptionPane(message);
        Object[] options = { LocaleHandler.getString("ok") };
        pane.setOptions(options);
        pane.setMessageType(error ? WebOptionPane.ERROR_MESSAGE : WebOptionPane.INFORMATION_MESSAGE);
        final WebDialog dialog = new WebDialog(parent, error ? LocaleHandler.getString("error")
                : LocaleHandler.getString("info"));
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
        // check for null because error messages must be shown even on
        // application start when main frame does not exist already
        if (parent != null) {
            centerDialogInsideMainFrame(parent, dialog);
        }
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
