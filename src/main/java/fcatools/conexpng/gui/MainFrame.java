package fcatools.conexpng.gui;

import com.alee.laf.button.WebButton;
import com.alee.laf.label.WebLabel;
import com.alee.laf.menu.WebMenu;
import com.alee.laf.menu.WebMenuBar;
import com.alee.laf.menu.WebMenuItem;
import com.alee.laf.panel.WebPanel;
import com.alee.laf.tabbedpane.TabbedPaneStyle;
import com.alee.laf.tabbedpane.WebTabbedPane;
import com.alee.managers.hotkey.Hotkey;
import fcatools.conexpng.ContextChangeEvents;
import fcatools.conexpng.ProgramState;
import fcatools.conexpng.ProgramState.ContextChangeEvent;
import fcatools.conexpng.Util;
import fcatools.conexpng.gui.contexteditor.ContextEditor;
import fcatools.conexpng.gui.dependencies.DependencyView;
import fcatools.conexpng.gui.lattice.LatticeView;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import static fcatools.conexpng.Util.loadIcon;

public class MainFrame extends JFrame {

    private static final long serialVersionUID = -3768163989667340886L;

    private static final String MARGIN = "                    ";

    // Components
    private WebPanel mainPanel;
    private WebTabbedPane tabPane;
    private WebLabel viewTitleLabel;
    private View contextView;
    private View latticeView;
    private View associationView;
    private ProgramState state;

    private MainToolbar mainToolbar;

    public MainFrame(ProgramState state) {
        getContentPane().setLayout(new BorderLayout());
        mainPanel = new WebPanel(new BorderLayout());
        this.state = state;

        tabPane = new WebTabbedPane () {
            public Dimension getPreferredSize () {
                Dimension ps = super.getPreferredSize ();
                ps.width = 150;
                return ps;
            }
        };
        tabPane.setTabbedPaneStyle(TabbedPaneStyle.attached);
        tabPane.setTabPlacement(JTabbedPane.TOP);
        WebPanel tabPanel = new WebPanel();
        tabPanel.setPreferredSize(new Dimension(105, 30));
        tabPanel.add(tabPane);
//        WebPanel topPanelContainer = new WebPanel();
//        topPanelContainer.setPreferredSize(new Dimension(100, 37));
        WebPanel topPanel = new WebPanel(new BorderLayout());
        topPanel.add(tabPanel, BorderLayout.WEST);
        WebButton b = new WebButton(loadIcon("icons/menu.png"));
        WebPanel menuPanel = new WebPanel();
//        menuPanel.add(b);
        menuPanel.setBorder(BorderFactory.createMatteBorder(0, 1, 0, 0, new Color(140, 140, 140)));
        topPanel.add(menuPanel, BorderLayout.EAST);
        Border margin = new EmptyBorder(0, 0, 4, 0);
        topPanel.setBorder(margin);
        topPanel.setPreferredSize(new Dimension(100, 27+4));
        WebPanel centerPanel = new WebPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.X_AXIS));
        centerPanel.add(Box.createHorizontalGlue());
        viewTitleLabel = new WebLabel();
        centerPanel.add(viewTitleLabel);
        centerPanel.add(Box.createHorizontalGlue());
        centerPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(140, 140, 140)));
        topPanel.add(centerPanel, BorderLayout.CENTER);
        mainPanel.add(topPanel, BorderLayout.NORTH);
//        add(tabPane);

        contextView = new ContextEditor(state);
        latticeView = new LatticeView(state);
        associationView = new DependencyView(state);

        addTab(tabPane, contextView, "icons/tabs/context_editor.png", "Context", "Edit Context (CTRL + E)", 0);
        addTab(tabPane, latticeView, "icons/tabs/lattice_editor.png", "Lattice", "Show Lattice (CTRL + L)", 1);
        addTab(tabPane, associationView, "icons/tabs/dependencies_editor.png", "Dependencies", "Calculate Dependencies (CTRL + D)", 2);

//        mainToolbar = new MainToolbar(this, state);
//        mainToolbar.disableSaveButton();
//        add(mainToolbar, BorderLayout.PAGE_START);

        setTitle("ConExp-NG - \"" + state.filePath + "\"");
        MainStatusBar statusBar = new MainStatusBar();
        state.addPropertyChangeListener(statusBar);
        mainPanel.add(statusBar, BorderLayout.SOUTH);

        add(mainPanel);

        showContextEditor();

        tabPane.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                JTabbedPane sourceTabbedPane = (JTabbedPane) e.getSource();
                int index = sourceTabbedPane.getSelectedIndex();
                switch (index) {
                    case 0: showContextEditor(); break;
                    case 1: showLatticeEditor(); break;
                    case 2: showDependeciesEditor(); break;
                }
            }
        });

        // TODO: Add icons
        WebMenuBar menuBar = new WebMenuBar();
        menuBar.add(new WebMenu("", loadIcon("icons/menu.png" )) {{
            add(new WebMenuItem("New...") {{
                setHotkey(Hotkey.CTRL_N);
            }});
            add(new WebMenuItem("Open..."));
            add(new WebMenu("Open recent") {{
                add(new WebMenuItem("/tmp/cool.cex"));
                add(new WebMenuItem("/Users/frank/projects/tealady.cex"));
                add(new WebMenuItem("/Users/frank/projects/teaman.cex"));
            }});
            addSeparator();
            add(new WebMenuItem("Save"));
            add(new WebMenuItem("Save as..."));
            addSeparator();
            add(new WebMenuItem("Import..."));
            add(new WebMenuItem("Export..."));
            addSeparator();
            add(new WebMenuItem("Undo"));
            add(new WebMenuItem("Redo"));
            addSeparator();
            add(new WebMenuItem("Count concepts"));
            add(new WebMenuItem("Start exploration"));
            addSeparator();
            add(new WebMenuItem("About"));
            add(new WebMenuItem("Exit") {{
                setHotkey(Hotkey.ALT_F4);
            }});
        }});

        menuPanel.add(menuBar);
    }

    public void showContextEditor() {
        removeOldView();
        viewTitleLabel.setText("Context Editor" + MARGIN);
        mainPanel.add(contextView, BorderLayout.CENTER);
        validate();
        revalidate();
        repaint();
    }

    public void showLatticeEditor() {
        removeOldView();
        viewTitleLabel.setText("Lattice Editor" + MARGIN);
        mainPanel.add(latticeView, BorderLayout.CENTER);
        validate();
        revalidate();
        repaint();
    }

    public void showDependeciesEditor() {
        removeOldView();
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
            mainPanel.remove(component);
        }
    }

    private void addTab(WebTabbedPane t, View v, String iconPath, String title, String toolTip, int i) {
//        t.insertTab(title, null, v, toolTip, i);
        t.addTab("", loadIcon(iconPath), null, toolTip);
        t.addPropertyChangeListener(v);
        KeyStroke shortcut = null;
        switch (i) {
        case 0: {
            shortcut = KeyStroke.getKeyStroke(KeyEvent.VK_E, InputEvent.CTRL_DOWN_MASK); break;
        }
        case 1: {
            shortcut = KeyStroke.getKeyStroke(KeyEvent.VK_L, InputEvent.CTRL_DOWN_MASK); break;
        }
        case 2: {
            shortcut = KeyStroke.getKeyStroke(KeyEvent.VK_D, InputEvent.CTRL_DOWN_MASK); break;
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

        @Override
        public void actionPerformed(ActionEvent arg0) {
            if (state.unsavedChanges) {
                Object[] options = { "Yes", "No", "Cancel" };
                final JOptionPane optionPane = new JOptionPane(
                        "Do want to save the changes you made to the document?",
                        JOptionPane.QUESTION_MESSAGE,
                        JOptionPane.YES_NO_CANCEL_OPTION);
                optionPane.setOptions(options);
                final JDialog dialog = new JDialog(MainFrame.this,
                        "Document was modified", true);

                dialog.setContentPane(optionPane);
                optionPane
                        .addPropertyChangeListener(new PropertyChangeListener() {
                            public void propertyChange(PropertyChangeEvent e) {
                                if (dialog.isVisible()
                                        && (e.getSource() == optionPane)
                                        && (e.getPropertyName()
                                                .equals(JOptionPane.VALUE_PROPERTY))) {
                                    dialog.setVisible(false);
                                }
                            }
                        });
                dialog.pack();
                Util.centerDialogInsideMainFrame(MainFrame.this, dialog);
                dialog.setVisible(true);
                String n = (String) optionPane.getValue();
                if (n.equals("Yes")) {
                    // TODO: Question:
                    // if user selects cancel in the filedialog->exit?
                    mainToolbar.new SaveAction().actionPerformed(arg0);
                } else if (n.equals("Cancel")) {
                    return;
                }
            }
        }
    }

    @SuppressWarnings("serial")
    private class MainStatusBar extends JPanel implements
            PropertyChangeListener {

        public MainStatusBar() {
            setLayout(new BorderLayout());
            setPreferredSize(new Dimension(100, 20));
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if (evt instanceof ContextChangeEvent) {
                ContextChangeEvent cce = (ContextChangeEvent) evt;
                if (cce.getName() == ContextChangeEvents.STARTCALCULATION) {
                    JPanel temp = new JPanel(new BorderLayout());
                    temp.add(new JLabel((String) evt.getNewValue() + "  "),
                            BorderLayout.CENTER);
                    JProgressBar progressbar = new JProgressBar();
                    progressbar.setIndeterminate(true);
                    temp.add(progressbar, BorderLayout.EAST);
                    add(temp, BorderLayout.EAST);
                    revalidate();
                    getParent().setCursor(
                            Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                } else if (cce.getName() == ContextChangeEvents.ENDCALCULATION) {
                    getParent().setCursor(
                            Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                    removeAll();
                    add(new JLabel(""));
                    revalidate();
                }

            }
        }
    }

}


