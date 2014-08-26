package fcatools.conexpng.gui;

import static fcatools.conexpng.Util.loadIcon;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;

import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.KeyStroke;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.undo.UndoManager;

import com.alee.extended.filefilter.FilesFilter;
import com.alee.extended.panel.GridPanel;
import com.alee.extended.panel.GroupPanel;
import com.alee.extended.panel.WebButtonGroup;
import com.alee.extended.panel.WebComponentPanel;
import com.alee.laf.button.WebButton;
import com.alee.laf.filechooser.WebFileChooser;
import com.alee.laf.label.WebLabel;
import com.alee.laf.list.WebList;
import com.alee.laf.optionpane.WebOptionPane;
import com.alee.laf.rootpane.WebDialog;
import com.alee.laf.spinner.WebSpinner;
import com.alee.laf.toolbar.ToolbarStyle;
import com.alee.laf.toolbar.WebToolBar;
import com.alee.managers.popup.PopupWay;
import com.alee.managers.popup.WebButtonPopup;

import de.tudresden.inf.tcs.fcalib.FullObject;
import de.tudresden.inf.tcs.fcalib.action.StartExplorationAction;
import fcatools.conexpng.Conf;
import fcatools.conexpng.Main;
import fcatools.conexpng.Util;
import fcatools.conexpng.gui.MainFrame.OverwritingFileDialog;
import fcatools.conexpng.gui.MainFrame.StillCalculatingDialog;
import fcatools.conexpng.gui.MainFrame.UnsavedChangesDialog;
import fcatools.conexpng.io.CEXReader;
import fcatools.conexpng.io.CEXWriter;
import fcatools.conexpng.io.CSVReader;
import fcatools.conexpng.io.CSVWriter;
import fcatools.conexpng.io.CXTReader;
import fcatools.conexpng.io.CXTWriter;
import fcatools.conexpng.io.OALReader;
import fcatools.conexpng.io.OALWriter;
import fcatools.conexpng.io.locale.LocaleHandler;
import fcatools.conexpng.io.locale.SupportedLanguages;

public class MainToolbar extends WebToolBar {

    private Conf state;

    private MainFrame mainFrame;

    private static WebButton redoButton;
    private static WebButton undoButton;
    private static WebButton saveButton;

    public FilesFilter cexFilter = new FilesFilter() {

        @Override
        public boolean accept(File pathname) {

            return !pathname.isHidden() && !pathname.isDirectory() && pathname.getName().endsWith(".cex");
        }

        @Override
        public String getDescription() {
            return LocaleHandler.getString("MainToolbar.cexFilter.getDescription");
        }
    };

    public FilesFilter otherFilter = new FilesFilter() {

        @Override
        public boolean accept(File pathname) {

            return !pathname.isHidden()
                    && !pathname.isDirectory()
                    && (pathname.getName().endsWith(".cxt") || pathname.getName().endsWith(".oal") || pathname
                            .getName().endsWith(".csv"));
        }

        @Override
        public String getDescription() {
            return LocaleHandler.getString("MainToolbar.otherFilter.getDescription");
        }
    };

    private static final long serialVersionUID = -3495670613141172867L;

    @SuppressWarnings("serial")
    public MainToolbar(final MainFrame mainFrame, final Conf state) {
        this.mainFrame = mainFrame;
        this.state = state;
        this.setFloatable(false);

        setToolbarStyle(ToolbarStyle.attached);
        setFloatable(false);
        WebButton newContextButton = new WebButton(loadIcon("icons/jlfgr/New24.gif")) {
            {
                setDrawFocus(false);
                setToolTipText(LocaleHandler.getString("MainToolbar.MainToolbar.newContextButton.toolTip"));
                addActionListener(new NewAction());
            }
        };
        add(newContextButton);
        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
                KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.CTRL_DOWN_MASK), "newContext");
        getActionMap().put("newContext", new NewAction());
        WebButton openButtonLeft = new WebButton(loadIcon("icons/jlfgr/Open24.gif")) {
            {
                setDrawFocus(false);
                setToolTipText(LocaleHandler.getString("MainToolbar.MainToolbar.openButtonLeft.toolTip"));
                addActionListener(new OpenAction(true));
            }
        };
        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
                KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_DOWN_MASK), "openContext");
        getActionMap().put("openContext", new OpenAction(true));
        final WebButton openButtonRight = new WebButton(loadIcon("icons/arrow_down.png"));
        openButtonRight.setDrawFocus(false);
        openButtonRight.setToolTipText(LocaleHandler.getString("MainToolbar.MainToolbar.openButtonRight.toolTip"));
        final WebButtonPopup popup = new WebButtonPopup(openButtonRight, PopupWay.downRight);
        WebList list = new WebList(state.lastOpened);
        list.setVisibleRowCount(4);
        list.setEditable(false);
        list.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting() && !((WebList) e.getSource()).isSelectionEmpty()) {
                    popup.hidePopup();
                    if (!state.canBeSaved()) {
                        StillCalculatingDialog scd = mainFrame.new StillCalculatingDialog();
                        if (scd.isYes())
                            return;
                    }
                    String path = String.copyValueOf(((String) ((WebList) e.getSource()).getSelectedValue())
                            .toCharArray());
                    try {
                        new CEXReader(state, path);

                    } catch (Exception e1) {
                        Util.handleIOExceptions(MainToolbar.this.mainFrame, e1, path);
                    }
                    ((WebList) e.getSource()).clearSelection();
                }
            }
        });
        popup.setContent(new GroupPanel(list));
        openButtonRight.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (popup.isShowing()) {
                    popup.hidePopup();
                } else {
                    popup.showPopup(openButtonRight);
                }
            }
        });
        add(new WebButtonGroup(true, openButtonLeft, openButtonRight));

        addSeparator();
        saveButton = new WebButton(loadIcon("icons/jlfgr/Save24.gif")) {
            {
                setDrawFocus(false);
                setEnabled(false);
                addActionListener(new SaveAction(false, true));
                setToolTipText(LocaleHandler.getString("MainToolbar.MainToolbar.saveButton.toolTip"));
            }
        };
        add(saveButton);
        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
                KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK), "saveContext");
        getActionMap().put("saveContext", new SaveAction(false, true));
        WebButton saveAsButton = new WebButton(loadIcon("icons/jlfgr/SaveAs24.gif")) {
            {
                setDrawFocus(false);
                setToolTipText(LocaleHandler.getString("MainToolbar.MainToolbar.saveAsButton.toolTip"));
                addActionListener(new SaveAction(true, true));
            }
        };
        add(saveAsButton);
        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
                KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK),
                "saveAsContext");
        getActionMap().put("saveAsContext", new SaveAction(true, true));

        addSeparator();
        WebButton importButton = new WebButton(loadIcon("icons/jlfgr/Import24.gif")) {
            {
                setDrawFocus(false);
                addActionListener(new OpenAction(false));
                setToolTipText(LocaleHandler.getString("MainToolbar.MainToolbar.importButton.toolTip"));
            }
        };
        add(importButton);
        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
                KeyStroke.getKeyStroke(KeyEvent.VK_I, InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK),
                "importContext");
        getActionMap().put("importContext", new OpenAction(false));
        WebButton exportButton = new WebButton(loadIcon("icons/jlfgr/Export24.gif")) {
            {
                setDrawFocus(false);
                setToolTipText(LocaleHandler.getString("MainToolbar.MainToolbar.exportButton.toolTip"));
                addActionListener(new SaveAction(true, false));
                setEnabled(true);
            }
        };
        add(exportButton);
        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
                KeyStroke.getKeyStroke(KeyEvent.VK_E, InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK),
                "exportContext");
        getActionMap().put("exportContext", new SaveAction(true, false));
        addSeparator();
        undoButton = new WebButton(loadIcon("icons/jlfgr/Undo24.gif")) {
            {
                setEnabled(false);
                setDrawFocus(false);
                setToolTipText(LocaleHandler.getString("MainToolbar.MainToolbar.undoButton.toolTip"));
                addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent arg0) {
                        undo();
                    }
                });
            }
        };
        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
                KeyStroke.getKeyStroke(KeyEvent.VK_Z, InputEvent.CTRL_DOWN_MASK), "undoContext");
        getActionMap().put("undoContext", new AbstractAction() {

            @Override
            public void actionPerformed(ActionEvent arg0) {
                undo();
            }
        });

        redoButton = new WebButton(loadIcon("icons/jlfgr/Redo24.gif")) {
            {
                setEnabled(false);
                setDrawFocus(false);
                setToolTipText(LocaleHandler.getString("MainToolbar.MainToolbar.redoButton.toolTip"));
                addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent arg0) {
                        redo();
                    }
                });
            }
        };
        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
                KeyStroke.getKeyStroke(KeyEvent.VK_Y, InputEvent.CTRL_DOWN_MASK), "redoContext");
        getActionMap().put("redoContext", new AbstractAction() {

            @Override
            public void actionPerformed(ActionEvent arg0) {
                redo();
            }
        });
        add(undoButton);
        add(redoButton);
        addSeparator();
        WebButton showNumberOfConceptsButton = new WebButton(loadIcon("icons/jlfgr/TipOfTheDay24.gif")) {
            {
                setDrawFocus(false);
                setEnabled(true);
                setToolTipText(LocaleHandler.getString("MainToolbar.MainToolbar.showNumberOfConceptsButton.toolTip"));
                addActionListener(new ActionListener() {

                    @Override
                    public void actionPerformed(ActionEvent e) {
                        Util.showMessageDialog(mainFrame,
                                LocaleHandler.getString("MainToolbar.MainToolbar.showNumberOfConceptsButton.action")
                                        + state.getNumberOfConcepts()
                                + ".", false);
                    }
                });
            }
        };
        add(showNumberOfConceptsButton);
        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
                KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK),
                "countContext");
        getActionMap().put("countContext", new AbstractAction() {

            @Override
            public void actionPerformed(ActionEvent arg0) {
                Util.showMessageDialog(
                        mainFrame,
                        LocaleHandler.getString("MainToolbar.MainToolbar.showNumberOfConceptsButton.action")
                                + state.getNumberOfConcepts() + ".",
                        false);
            }
        });
        WebButton attrExplButton = new WebButton(loadIcon("icons/jlfgr/Replace24.gif")) {
            {
                setToolTipText(LocaleHandler.getString("MainToolbar.MainToolbar.attrExplButton.toolTip"));
                setDrawFocus(false);
                addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent arg0) {
                        AttributeExplorationExpert expert = new AttributeExplorationExpert(MainToolbar.this.mainFrame,
                                state);
                        state.context.setExpert(expert);
                        expert.addExpertActionListener(state.context);
                        // Create an expert action for starting attribute
                        // exploration
                        StartExplorationAction<String, String, FullObject<String, String>> action = new StartExplorationAction<String, String, FullObject<String, String>>();
                        action.setContext(state.context);
                        // Fire the action, exploration starts...
                        expert.fireExpertAction(action);
                    }
                });
            }
        };
        add(attrExplButton);
        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
                KeyStroke.getKeyStroke(KeyEvent.VK_A, InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK),
                "aexplorationContext");
        getActionMap().put("aexplorationContext", new AbstractAction() {

            @Override
            public void actionPerformed(ActionEvent arg0) {
                AttributeExplorationExpert expert = new AttributeExplorationExpert(MainToolbar.this.mainFrame, state);
                state.context.setExpert(expert);
                expert.addExpertActionListener(state.context);
                StartExplorationAction<String, String, FullObject<String, String>> action = new StartExplorationAction<String, String, FullObject<String, String>>();
                action.setContext(state.context);
                expert.fireExpertAction(action);
            }
        });

        addSeparator();
        // language selector
        final WebButton languageButton = new WebButton(loadIcon("icons/arrow_down.png"));
        languageButton.setDrawFocus(false);
        languageButton.setToolTipText(LocaleHandler.getString("MainToolbar.MainToolbar.languageButton.toolTip"));
        final WebButtonPopup languageButtonPopup = new WebButtonPopup(languageButton, PopupWay.downCenter);
        WebList languageButtonPopupList = new WebList(SupportedLanguages.values());
        languageButtonPopupList.setEditable(false);
        languageButtonPopupList.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                WebList list = (WebList) e.getSource();
                if (!e.getValueIsAdjusting() && !list.isSelectionEmpty()) {
                    languageButtonPopup.hidePopup();
                    // set the language and store selection in settings
                    LocaleHandler.setSelectedLanguage(list.getSelectedValue().toString());
                    Main.storeOptions(mainFrame, state);
                    list.clearSelection();
                }
            }
        });
        languageButtonPopup.setContent(new GroupPanel(languageButtonPopupList));
        languageButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (languageButtonPopup.isShowing()) {
                    languageButtonPopup.hidePopup();
                } else {
                    languageButtonPopup.showPopup(languageButton);
                }
            }
        });
        add(languageButton);

        WebButton aboutButton = new WebButton(loadIcon("icons/jlfgr/About24.gif")) {
            {
                setDrawFocus(false);
                setEnabled(true);
                setToolTipText(LocaleHandler.getString("MainToolbar.MainToolbar.aboutButton.toolTip"));
                addActionListener(new ActionListener() {

                    @Override
                    public void actionPerformed(ActionEvent arg0) {
                        Util.showMessageDialog(
                                mainFrame,
                                LocaleHandler.getString("MainToolbar.MainToolbar.aboutButton.aboutText"),
                                false);
                    }
                });
            }
        };
        add(aboutButton);
        WebButton helpButton = new WebButton(loadIcon("icons/jlfgr/Help24.gif")) {
            {
                setDrawFocus(false);
                setEnabled(true);
                setToolTipText(LocaleHandler.getString("MainToolbar.MainToolbar.helpButton.toolTip"));
            }
        };
        add(helpButton);
        // getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
        // KeyEvent.VK_F1, "helpContext");
        // getActionMap().put("helpContext", new HelpAction());
    }

    /**
     * Undo changes.
     */
    private void undo() {
        // get current tab to use the right undo manager
        UndoManager currentActiveUndoManager = null;
        if (state.guiConf.lastTab == 0) {
            currentActiveUndoManager = state.getContextEditorUndoManager();
        } else if (state.guiConf.lastTab == 1) {
            currentActiveUndoManager = state.getLatticeViewUndoManager();
        }
        // check if undo is possible
        if (currentActiveUndoManager.canUndo()) {
            // undo the changes in the undo manager
            currentActiveUndoManager.undo();
            // set button states
            getRedoButton().setEnabled(currentActiveUndoManager.canRedo());
            getUndoButton().setEnabled(currentActiveUndoManager.canUndo());
        }
    }

    /**
     * Redo changes.
     */
    private void redo() {
        // get current tab to use the right undo manager
        UndoManager currentActiveUndoManager = null;
        if (state.guiConf.lastTab == 0) {
            currentActiveUndoManager = state.getContextEditorUndoManager();
        } else if (state.guiConf.lastTab == 1) {
            currentActiveUndoManager = state.getLatticeViewUndoManager();
        }
        // check if redo is possible
        if (currentActiveUndoManager.canRedo()) {
            // redo the changes in the undo manager
            currentActiveUndoManager.redo();
            // set button states
            getRedoButton().setEnabled(currentActiveUndoManager.canRedo());
            getUndoButton().setEnabled(currentActiveUndoManager.canUndo());
        }
    }

    public static WebButton getSaveButton() {
        return saveButton;
    }

    public static WebButton getUndoButton() {
        return undoButton;
    }

    public static WebButton getRedoButton() {
        return redoButton;
    }

    // ////////////////////////////////////////////////////////////////////////

    @SuppressWarnings("serial")
    class SaveAction extends AbstractAction {

        private boolean saveAs;
        private boolean cex;

        public SaveAction(boolean saveAs, boolean cex) {
            this.saveAs = saveAs;
            this.cex = cex;
        }

        private void saveFile(String path) {
            try {
                if (!cex) {
                    if (!path.contains(".cxt") && !path.contains(".oal") && !path.contains(".csv"))
                        path = path.concat(".cxt");
                    if (path.endsWith("cxt"))
                        new CXTWriter(state, path);
                    else if (path.endsWith("oal"))
                        new OALWriter(state, path);
                    else if (path.endsWith("csv"))
                        new CSVWriter(state, path);
                } else {
                    if (!path.contains(".cex"))
                        path = path.concat(".cex");

                    new CEXWriter(state, path);
                    state.setNewFile(path);
                    state.unsavedChanges = false;
                    MainToolbar.saveButton.setEnabled(false);
                }
            } catch (Exception e1) {
                Util.handleIOExceptions(mainFrame, e1, path);
            }

        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if (!state.canBeSaved()) {
                StillCalculatingDialog scd = mainFrame.new StillCalculatingDialog();
                if (scd.isYes())
                    return;
            }
            if (state.filePath.endsWith("untitled.cex") || saveAs) {
                final WebFileChooser fc = new WebFileChooser();
                fc.setCurrentDirectory(state.filePath.substring(0,
                        state.filePath.lastIndexOf(System.getProperty("file.separator"))));
                final WebDialog dialog = new WebDialog(mainFrame,
                        LocaleHandler.getString("MainToolbar.SaveAction.actionPerformed.dialog"), true);
                dialog.setContentPane(fc);
                fc.setMultiSelectionEnabled(false);
                fc.setAcceptAllFileFilterUsed(false);
                fc.addChoosableFileFilter(cex ? cexFilter : otherFilter);
                fc.setFileSelectionMode(WebFileChooser.FILES_ONLY);
                fc.setDialogType(WebFileChooser.SAVE_DIALOG);
                fc.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        String state = (String) e.getActionCommand();
                        if ((state.equals(WebFileChooser.APPROVE_SELECTION) && fc.getSelectedFile() != null)) {
                            File file = fc.getSelectedFile();
                            String path = file.getAbsolutePath();
                            if (file.exists()) {
                                OverwritingFileDialog ofd = mainFrame.new OverwritingFileDialog(file);
                                if (ofd.isYes()) {
                                    saveFile(path);
                                    dialog.setVisible(false);
                                }
                            } else {
                                saveFile(path);
                                dialog.setVisible(false);
                            }
                        } else if (state.equals(WebFileChooser.CANCEL_SELECTION)) {
                            dialog.setVisible(false);
                            return;
                        }
                    }
                });
                dialog.pack();
                Util.centerDialogInsideMainFrame(mainFrame, dialog);
                dialog.setVisible(true);

                if (fc.getSelectedFile() == null)
                    return;

            } else {
                saveFile(MainToolbar.this.state.filePath);
            }
        }

    }

    @SuppressWarnings("serial")
    class OpenAction extends AbstractAction {

        private boolean cex;

        public OpenAction(boolean cex) {
            this.cex = cex;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if (!state.canBeSaved()) {
                StillCalculatingDialog scd = mainFrame.new StillCalculatingDialog();
                if (scd.isYes())
                    return;
            }
            if (state.unsavedChanges) {
                UnsavedChangesDialog ucd = mainFrame.new UnsavedChangesDialog();
                if (ucd.isYes()) {
                    new SaveAction(false, true).actionPerformed(e);
                } else if (ucd.isCancel())
                    return;
            }
            final WebFileChooser fc = new WebFileChooser(state.filePath);
            final WebDialog dialog = new WebDialog(mainFrame,
                    LocaleHandler.getString("MainToolbar.OpenAction.actionPerformed.dialog"), true);

            fc.setDialogType(WebFileChooser.OPEN_DIALOG);
            fc.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    String state = (String) e.getActionCommand();
                    if ((state.equals(WebFileChooser.APPROVE_SELECTION) && fc.getSelectedFile() != null)
                            || state.equals(WebFileChooser.CANCEL_SELECTION)) {
                        dialog.setVisible(false);
                    }
                }
            });
            fc.setMultiSelectionEnabled(false);
            fc.setAcceptAllFileFilterUsed(false);
            fc.addChoosableFileFilter(cex ? cexFilter : otherFilter);
            fc.setFileSelectionMode(WebFileChooser.FILES_ONLY);
            fc.setCurrentDirectory(state.filePath.substring(0,
                    state.filePath.lastIndexOf(System.getProperty("file.separator"))));
            dialog.setContentPane(fc);
            dialog.pack();
            Util.centerDialogInsideMainFrame(mainFrame, dialog);
            dialog.setVisible(true);

            if (fc.getSelectedFile() != null) {
                File file = fc.getSelectedFile();
                String path = file.getAbsolutePath();

                try {
                    if (cex && !path.contains("."))
                        path = path.concat(".cex");
                    if (path.endsWith(".cex") && cex)
                        new CEXReader(state, path);
                    else if (!cex && path.endsWith(".cxt"))
                        new CXTReader(state, path);
                    else if (!cex && path.endsWith(".oal"))
                        new OALReader(state, path);
                    else if (!cex && path.endsWith(".csv"))
                        new CSVReader(state, path);
                } catch (Exception e1) {
                    Util.handleIOExceptions(mainFrame, e1, path);
                }
            }
        }
    }

    @SuppressWarnings("serial")
    public class HelpAction extends AbstractAction {

        @Override
        public void actionPerformed(ActionEvent e) {
            // TODO Auto-generated method stub

        }
    }

    @SuppressWarnings("serial")
    public class NewAction extends AbstractAction {

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
                    new SaveAction(false, true).actionPerformed(arg0);
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
                    .getString("MainToolbar.NewAction.actionPerformed.panel.WebLabel.1")), attr));
            panel.addElement(new GridPanel(new WebLabel(LocaleHandler
                    .getString("MainToolbar.NewAction.actionPerformed.panel.WebLabel.2")), obj));
            final WebOptionPane pane = new WebOptionPane(panel, WebOptionPane.OK_OPTION);
            pane.setMessageType(WebOptionPane.PLAIN_MESSAGE);
            final WebDialog dialog = new WebDialog(mainFrame,
                    LocaleHandler.getString("MainToolbar.NewAction.actionPerformed.dialog"), true);
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
}
