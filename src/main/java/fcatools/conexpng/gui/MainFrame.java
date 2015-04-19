package fcatools.conexpng.gui;

import static fcatools.conexpng.Util.loadIcon;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.KeyStroke;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.alee.laf.label.WebLabel;
import com.alee.laf.optionpane.WebOptionPane;
import com.alee.laf.panel.WebPanel;
import com.alee.laf.rootpane.WebDialog;
import com.alee.laf.rootpane.WebFrame;
import com.alee.laf.tabbedpane.TabbedPaneStyle;
import com.alee.laf.tabbedpane.WebTabbedPane;

import fcatools.conexpng.Conf;
import fcatools.conexpng.GUIConf;
import fcatools.conexpng.Main;
import fcatools.conexpng.Util;
import fcatools.conexpng.gui.actions.CloseAction;
import fcatools.conexpng.gui.contexteditor.ContextEditor;
import fcatools.conexpng.gui.contexteditor.ContextEditorUndoManager;
import fcatools.conexpng.gui.dependencies.DependencyView;
import fcatools.conexpng.gui.lattice.LatticeView;
import fcatools.conexpng.gui.lattice.LatticeViewUndoManager;
import fcatools.conexpng.io.locale.LocaleHandler;

// TODO: The code needs to be tidied up drastically
public class MainFrame extends WebFrame {

    private static final long serialVersionUID = -3768163989667340886L;

    private static final String MARGIN = "                              ";

    // Components
    private WebPanel mainPanel;
    private WebTabbedPane tabPane;
    private WebLabel viewTitleLabel;
    private ContextEditor contextView;
    private LatticeView latticeView;
    private DependencyView associationView;
    private Conf state;
    private StatusBar statusBar;

    @SuppressWarnings({ "serial" })
    public MainFrame(final Conf state) {
        Image img = Toolkit.getDefaultToolkit().getImage("src/main/resources/icons/logo.png");
        setIconImage(img);
        setDefaultCloseOperation(WebFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent we) {
                try {
                    Main.storeOptions(MainFrame.this, state);
                    CloseAction close = new CloseAction(MainFrame.this, state);
                    close.actionPerformed(null);
                    if (!close.canceled())
                        System.exit(0);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        state.addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent arg0) {
                setTitle("ConExp-NG - \"" + state.filePath + "\"");
                revalidate();
                repaint();
            }
        });
        getContentPane().setLayout(new BorderLayout());
        mainPanel = new WebPanel(new BorderLayout());
        this.state = state;

        tabPane = new WebTabbedPane() {
            public Dimension getPreferredSize() {
                Dimension ps = super.getPreferredSize();
                ps.width = 150;
                return ps;
            }
        };
        tabPane.setTabbedPaneStyle(TabbedPaneStyle.attached);
        tabPane.setTabPlacement(WebTabbedPane.TOP);
        WebPanel tabPanel = new WebPanel();
        tabPanel.setPreferredSize(new Dimension(105, 30));
        tabPanel.add(tabPane);
        // WebPanel topPanelContainer = new WebPanel();
        // topPanelContainer.setPreferredSize(new Dimension(100, 37));
        WebPanel topPanel = new WebPanel(new BorderLayout());
        topPanel.add(tabPanel, BorderLayout.WEST);
        topPanel.setPreferredSize(new Dimension(100, 27));
        WebPanel centerPanel = new WebPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.X_AXIS));
        centerPanel.add(Box.createHorizontalGlue());
        viewTitleLabel = new WebLabel();
        centerPanel.add(viewTitleLabel);
        centerPanel.add(Box.createHorizontalGlue());
        centerPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(140, 140, 140)));
        topPanel.add(centerPanel, BorderLayout.CENTER);
        mainPanel.add(topPanel, BorderLayout.NORTH);
        // add(tabPane);

        contextView = new ContextEditor(state);
        contextView.setVisible(false);
        latticeView = new LatticeView(state, this);
        latticeView.setVisible(false);
        associationView = new DependencyView(state);
        associationView.setVisible(false);

        addTab(tabPane, contextView, "icons/tabs/context_editor.png",
                LocaleHandler.getString("MainFrame.MainFrame.tab.0.title"),
                LocaleHandler.getString("MainFrame.MainFrame.tab.0.toolTip"), 0);
        addTab(tabPane, latticeView, "icons/tabs/lattice_editor.png",
                LocaleHandler.getString("MainFrame.MainFrame.tab.1.title"),
                LocaleHandler.getString("MainFrame.MainFrame.tab.1.toolTip"), 1);
        addTab(tabPane, associationView, "icons/tabs/dependencies_editor.png",
                LocaleHandler.getString("MainFrame.MainFrame.tab.2.title"),
                LocaleHandler.getString("MainFrame.MainFrame.tab.2.toolTip"), 2);

        statusBar = StatusBar.getInstance();
        statusBar.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(140, 140, 140)));
        state.setStatusBar(statusBar);
        mainPanel.add(statusBar, BorderLayout.SOUTH);
        add(mainPanel);
        add(new MainToolbar(this, state), BorderLayout.PAGE_START);

        tabPane.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                WebTabbedPane sourceTabbedPane = (WebTabbedPane) e.getSource();
                int index = sourceTabbedPane.getSelectedIndex();
                switch (index) {
                case 0:
                    showContextEditor();
                    break;
                case 1:
                    showLatticeEditor();
                    break;
                case 2:
                    showDependenciesEditor();
                    break;
                }
            }
        });
        selectLastUsedTab();
    }

    /**
     * Selects the last used/opened tab given by {@link GUIConf#lastTab}.
     */
    private void selectLastUsedTab() {
        tabPane.setSelectedIndex(state.guiConf.lastTab);
        switch (state.guiConf.lastTab) {
        case 0:
            showContextEditor();
            break;
        case 1:
            showLatticeEditor();
            break;
        case 2:
            showDependenciesEditor();
            break;
        }
    }

    public void showContextEditor() {
        // change undo/redo button state
        ContextEditorUndoManager undoManager = state.getContextEditorUndoManager();
        MainToolbar.getRedoButton().setEnabled(undoManager.canRedo());
        MainToolbar.getUndoButton().setEnabled(undoManager.canUndo());
        // show context editor
        state.guiConf.lastTab = 0;
        removeOldView();
        contextView.setVisible(true);
        viewTitleLabel.setText(LocaleHandler.getString("MainFrame.showContextEditor.viewTitleLabel") + MARGIN);
        mainPanel.add(contextView, BorderLayout.CENTER);
        validate();
        revalidate();
        repaint();
    }

    public void showLatticeEditor() {
        // change undo/redo button state
        LatticeViewUndoManager undoManager = state.getLatticeViewUndoManager();
        MainToolbar.getRedoButton().setEnabled(undoManager.canRedo());
        MainToolbar.getUndoButton().setEnabled(undoManager.canUndo());
        // show lattice view
        state.guiConf.lastTab = 1;
        removeOldView();
        latticeView.setVisible(true);
        viewTitleLabel.setText(LocaleHandler.getString("MainFrame.showLatticeEditor.viewTitleLabel") + MARGIN);
        mainPanel.add(latticeView, BorderLayout.CENTER);
        validate();
        revalidate();
        repaint();
    }

    public void showDependenciesEditor() {
        state.guiConf.lastTab = 2;
        removeOldView();
        associationView.setVisible(true);
        viewTitleLabel.setText(LocaleHandler.getString("MainFrame.showDependenciesEditor.viewTitleLabel") + MARGIN);
        mainPanel.add(associationView, BorderLayout.CENTER);
        validate();
        revalidate();
        repaint();
    }

    private void removeOldView() {
        BorderLayout layout = (BorderLayout) mainPanel.getLayout();
        Component component = layout.getLayoutComponent(BorderLayout.CENTER);
        if (component != null) {
            component.setVisible(false);
            mainPanel.remove(component);
        }
    }

    private void addTab(WebTabbedPane t, View v, String iconPath, String title, String toolTip, int i) {
        // t.insertTab(title, null, v, toolTip, i);
        t.addTab("", loadIcon(iconPath), null, toolTip);
        KeyStroke shortcut = null;
        switch (i) {
        case 0: {
            shortcut = KeyStroke.getKeyStroke(KeyEvent.VK_E, InputEvent.CTRL_DOWN_MASK);
            break;
        }
        case 1: {
            shortcut = KeyStroke.getKeyStroke(KeyEvent.VK_L, InputEvent.CTRL_DOWN_MASK);
            break;
        }
        case 2: {
            shortcut = KeyStroke.getKeyStroke(KeyEvent.VK_D, InputEvent.CTRL_DOWN_MASK);
            break;
        }
        }
        t.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(shortcut, title);
        t.getActionMap().put(title, new SwitchTab(i, t));

    }

    @SuppressWarnings("serial")
    private class SwitchTab extends AbstractAction {

        private int tabnr;
        private WebTabbedPane tabPane;

        public SwitchTab(int i, WebTabbedPane tabPane) {
            tabnr = i;
            this.tabPane = tabPane;
        }

        @Override
        public void actionPerformed(ActionEvent arg0) {
            tabPane.setSelectedIndex(tabnr);
        }

    }

    @SuppressWarnings("serial")
    public class StillCalculatingDialog extends WebDialog {
        private boolean yes;
        private boolean no;

        public StillCalculatingDialog() {
            super(MainFrame.this, LocaleHandler
                    .getString("MainFrame.StillCalculatingDialog.StillCalculatingDialog.title"), true);
            final WebOptionPane pane = new WebOptionPane(
                    LocaleHandler.getString("MainFrame.StillCalculatingDialog.StillCalculatingDialog.pane"));
            Object[] options = {
                    LocaleHandler.getString("MainFrame.StillCalculatingDialog.StillCalculatingDialog.options.wait"),
                    LocaleHandler.getString("MainFrame.StillCalculatingDialog.StillCalculatingDialog.options.dontCare") };
            pane.setOptions(options);
            pane.setMessageType(WebOptionPane.INFORMATION_MESSAGE);
            pane.addPropertyChangeListener(new PropertyChangeListener() {
                public void propertyChange(PropertyChangeEvent e) {
                    if (isVisible() && (e.getSource() == pane)
                            && (e.getPropertyName().equals(WebOptionPane.VALUE_PROPERTY))) {
                        setVisible(false);
                    }
                }
            });
            setModal(true);
            setContentPane(pane);
            pack();
            setDefaultCloseOperation(WebDialog.DO_NOTHING_ON_CLOSE);
            Util.centerDialogInsideMainFrame(MainFrame.this, this);
            setVisible(true);
            String n = (String) pane.getValue();
            if (n.equals(LocaleHandler
                    .getString("MainFrame.StillCalculatingDialog.StillCalculatingDialog.options.wait"))) {
                yes = true;
                no = false;
            } else {
                no = true;
                yes = false;
            }
        }

        public boolean isYes() {
            return yes;
        }

        public boolean isNo() {
            return no;
        }
    }

    @SuppressWarnings("serial")
    public class OverwritingFileDialog extends WebDialog {
        private boolean yes;
        private boolean no;

        public OverwritingFileDialog(File file) {
            super(MainFrame.this, LocaleHandler.getString("MainFrame.OverwritingFileDialog.title"), true);
            Object[] options = { LocaleHandler.getString("yes"), LocaleHandler.getString("no") };
            final WebOptionPane optionPane = new WebOptionPane(
                    LocaleHandler.getString("MainFrame.OverwritingFileDialog.optionPane") + file.getName()
                    + "?", WebOptionPane.QUESTION_MESSAGE, WebOptionPane.YES_NO_OPTION);
            optionPane.setOptions(options);

            setContentPane(optionPane);
            optionPane.addPropertyChangeListener(new PropertyChangeListener() {
                public void propertyChange(PropertyChangeEvent e) {
                    if (isVisible() && (e.getSource() == optionPane)
                            && (e.getPropertyName().equals(WebOptionPane.VALUE_PROPERTY))) {
                        setVisible(false);
                    }
                }
            });
            pack();
            setDefaultCloseOperation(WebDialog.DO_NOTHING_ON_CLOSE);
            Util.centerDialogInsideMainFrame(MainFrame.this, this);
            setVisible(true);
            String n = (String) optionPane.getValue();
            if (n.equals(LocaleHandler.getString("yes"))) {
                yes = true;
                no = false;
            } else if (n.equals(LocaleHandler.getString("no"))) {
                no = true;
                yes = false;
            }

        }

        public boolean isYes() {
            return yes;
        }

        public boolean isNo() {
            return no;
        }
    }

    @SuppressWarnings("serial")
    public class UnsavedChangesDialog extends WebDialog {
        private boolean cancel;
        private boolean yes;
        private boolean no;

        public UnsavedChangesDialog() {
            super(MainFrame.this, LocaleHandler.getString("MainFrame.UnsavedChangesDialog.UnsavedChangesDialog.title"),
                    true);
            Object[] options = { LocaleHandler.getString("yes"), LocaleHandler.getString("no"),
                    LocaleHandler.getString("cancel") };
            final WebOptionPane optionPane = new WebOptionPane(
                    LocaleHandler.getString("MainFrame.UnsavedChangesDialog.UnsavedChangesDialog.optionPane"),
                    WebOptionPane.QUESTION_MESSAGE, WebOptionPane.YES_NO_CANCEL_OPTION);
            optionPane.setOptions(options);

            setContentPane(optionPane);
            optionPane.addPropertyChangeListener(new PropertyChangeListener() {
                public void propertyChange(PropertyChangeEvent e) {
                    if (isVisible() && (e.getSource() == optionPane)
                            && (e.getPropertyName().equals(WebOptionPane.VALUE_PROPERTY))) {
                        setVisible(false);
                    }
                }
            });
            pack();
            Util.centerDialogInsideMainFrame(MainFrame.this, this);
            setVisible(true);
            String n = (String) optionPane.getValue();
            if (n.equals(LocaleHandler.getString("yes"))) {
                yes = true;
                no = false;
                cancel = false;
            } else if (n.equals(LocaleHandler.getString("cancel"))) {
                cancel = true;
                no = false;
                yes = false;
            } else if (n.equals(LocaleHandler.getString("no"))) {
                cancel = false;
                no = true;
                yes = false;
            }

        }

        public boolean isYes() {
            return yes;
        }

        public boolean isNo() {
            return no;
        }

        public boolean isCancel() {
            return cancel;
        }

    }

    /**
     * Updates the GUI after a new GUIConf is loaded.
     */
    public void updateGUI() {
        // general
        selectLastUsedTab();

        // dependencies
        associationView.updateGUI();

        // context
        contextView.updateButtonSelection();

        // lattice
        latticeView.updateGUI();
    }

}
