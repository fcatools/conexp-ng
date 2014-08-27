package fcatools.conexpng.gui.actions;

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.AbstractAction;
import javax.swing.SpinnerNumberModel;

import com.alee.extended.panel.GridPanel;
import com.alee.extended.panel.WebComponentPanel;
import com.alee.laf.label.WebLabel;
import com.alee.laf.optionpane.WebOptionPane;
import com.alee.laf.rootpane.WebDialog;
import com.alee.laf.spinner.WebSpinner;

import fcatools.conexpng.Conf;
import fcatools.conexpng.Util;
import fcatools.conexpng.gui.MainFrame;
import fcatools.conexpng.gui.MainFrame.StillCalculatingDialog;
import fcatools.conexpng.gui.MainFrame.UnsavedChangesDialog;
import fcatools.conexpng.io.locale.LocaleHandler;

/**
 * Action to create a new context.
 * 
 * @author Torsten Casselt
 */
public class NewAction extends AbstractAction {

    /**
     * 
     */
    private static final long serialVersionUID = -2743254372541826173L;
    private MainFrame mainFrame;
    private Conf state;
    
    /**
     * @param mainFrame
     *            to attach dialogs to
     * @param state
     *            to initialize new context
     */
    public NewAction(MainFrame mainFrame, Conf state) {
        this.mainFrame = mainFrame;
        this.state = state;
    }

    @Override
    public void actionPerformed(ActionEvent arg0) {
        if (!state.canBeSaved()) {
            StillCalculatingDialog scd = mainFrame.new StillCalculatingDialog();
            if (scd.isYes())
                return;
        }
        if (state.unsavedChanges) {
            UnsavedChangesDialog ucd = mainFrame.new UnsavedChangesDialog();
            if (ucd.isYes()) {
                new OpenSaveExportAction(mainFrame, state, false, false).actionPerformed(arg0);
            } else if (ucd.isCancel())
                return;
        }
        WebSpinner attr = new WebSpinner();
        attr.setModel(new SpinnerNumberModel(1, 1, 100, 1));
        attr.setValue(4);
        WebSpinner obj = new WebSpinner();
        obj.setModel(new SpinnerNumberModel(1, 1, 100, 1));
        obj.setValue(4);

        WebComponentPanel panel = new WebComponentPanel();
        panel.addElement(new GridPanel(new WebLabel(LocaleHandler
                .getString("NewAction.actionPerformed.panel.WebLabel.1")), attr));
        panel.addElement(new GridPanel(new WebLabel(LocaleHandler
                .getString("NewAction.actionPerformed.panel.WebLabel.2")), obj));
        final WebOptionPane pane = new WebOptionPane(panel, WebOptionPane.OK_OPTION);
        pane.setMessageType(WebOptionPane.PLAIN_MESSAGE);
        final WebDialog dialog = new WebDialog(mainFrame, LocaleHandler.getString("NewAction.actionPerformed.dialog"),
                true);
        pane.addPropertyChangeListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent e) {
                if (dialog.isVisible() && (e.getSource() == pane)
                        && (e.getPropertyName().equals(WebOptionPane.VALUE_PROPERTY))) {
                    dialog.setVisible(false);
                }
            }
        });
        dialog.setContentPane(pane);
        Object[] options = { LocaleHandler.getString("ok") };
        pane.setOptions(options);
        dialog.pack();
        Util.centerDialogInsideMainFrame(mainFrame, dialog);
        dialog.setVisible(true);
        state.init((int) obj.getValue(), (int) attr.getValue());
    }
}
