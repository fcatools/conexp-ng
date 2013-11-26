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
import fcatools.conexpng.Main;
import fcatools.conexpng.Util;
import fcatools.conexpng.gui.contexteditor.ContextEditor;
import fcatools.conexpng.gui.dependencies.DependencyView;
import fcatools.conexpng.gui.lattice.LatticeView;

// TODO: The code needs to be tidied up drastically
public class MainFrame extends WebFrame {

    private static final long serialVersionUID = -3768163989667340886L;

    private static final String MARGIN = "                              ";

    // Components
    private WebPanel mainPanel;
    private WebTabbedPane tabPane;
    private WebLabel viewTitleLabel;
    private View contextView;
    private View latticeView;
    private View associationView;
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
                    CloseAction close = new CloseAction();
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

        addTab(tabPane, contextView, "icons/tabs/context_editor.png", "Context", "Edit Context (CTRL + E)", 0);
        addTab(tabPane, latticeView, "icons/tabs/lattice_editor.png", "Lattice", "Show Lattice (CTRL + L)", 1);
        addTab(tabPane, associationView, "icons/tabs/dependencies_editor.png", "Dependencies",
                "Calculate Dependencies (CTRL + D)", 2);

        statusBar = new StatusBar();
        statusBar.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(140, 140, 140)));
        state.setStatusBar(statusBar);
        mainPanel.add(statusBar, BorderLayout.SOUTH);
        add(mainPanel);

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
                    showDependeciesEditor();
                    break;
                }
            }
        });
        tabPane.setSelectedIndex(state.guiConf.lastTab);
        switch (state.guiConf.lastTab) {
        case 0:
            showContextEditor();
            break;
        case 1:
            showLatticeEditor();
            break;
        case 2:
            showDependeciesEditor();
            break;
        }
        add(new MainToolbar(this, state), BorderLayout.PAGE_START);
    }

    public void showContextEditor() {
        state.guiConf.lastTab = 0;
        removeOldView();
        contextView.setVisible(true);
        viewTitleLabel.setText("Context Editor" + MARGIN);
        mainPanel.add(contextView, BorderLayout.CENTER);
        validate();
        revalidate();
        repaint();
    }

    public void showLatticeEditor() {
        state.guiConf.lastTab = 1;
        removeOldView();
        latticeView.setVisible(true);
        viewTitleLabel.setText("Lattice Editor" + MARGIN);
        mainPanel.add(latticeView, BorderLayout.CENTER);
        validate();
        revalidate();
        repaint();
    }

    public void showDependeciesEditor() {
        state.guiConf.lastTab = 2;
        removeOldView();
        associationView.setVisible(true);
        viewTitleLabel.setText("Dependencies Editor" + MARGIN);
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
    public class CloseAction extends AbstractAction {

        private boolean canceled;

        @Override
        public void actionPerformed(ActionEvent arg0) {
            if (!state.canBeSaved()) {
                StillCalculatingDialog scd = new StillCalculatingDialog();
                if (scd.isYes())
                    return;
            }
            if (state.unsavedChanges) {
                UnsavedChangesDialog usd = new UnsavedChangesDialog();
                if (usd.isYes()) {
                    new MainToolbar(MainFrame.this, state).new SaveAction(false, true).actionPerformed(arg0);
                } else if (usd.isCancel()) {
                    canceled = true;
                }
            }
        }

        public boolean canceled() {
            return canceled;
        }
    }

    @SuppressWarnings("serial")
    public class StillCalculatingDialog extends WebDialog {
        private boolean yes;
        private boolean no;

        public StillCalculatingDialog() {
            super(MainFrame.this, "Still calculating", true);
            final WebOptionPane pane = new WebOptionPane("Some calculations haven't finished now. Do you want to wait?");
            Object[] options = { "I will wait", "I don't care" };
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
            if (n.equals("I will wait")) {
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
            super(MainFrame.this, "Overwriting existing file?", true);
            Object[] options = { "Yes", "No" };
            final WebOptionPane optionPane = new WebOptionPane("Do you really want to overwrite " + file.getName()
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
            if (n.equals("Yes")) {
                yes = true;
                no = false;
            } else if (n.equals("No")) {
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
            super(MainFrame.this, "Context was modified", true);
            Object[] options = { "Yes", "No", "Cancel" };
            final WebOptionPane optionPane = new WebOptionPane("Do want to save the changes you made to the context?",
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
            if (n.equals("Yes")) {
                yes = true;
                no = false;
                cancel = false;
            } else if (n.equals("Cancel")) {
                cancel = true;
                no = false;
                yes = false;
            } else if (n.equals("No")) {
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

}
