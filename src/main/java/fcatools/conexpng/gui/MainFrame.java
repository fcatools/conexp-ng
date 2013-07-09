package fcatools.conexpng.gui;

import com.alee.extended.filechooser.SelectionMode;
import com.alee.extended.filechooser.WebFileChooser;
import com.alee.laf.StyleConstants;
import com.alee.laf.button.WebButton;
import com.alee.laf.label.WebLabel;
import com.alee.laf.panel.WebPanel;
import com.alee.laf.tabbedpane.TabbedPaneStyle;
import com.alee.laf.tabbedpane.WebTabbedPane;
import com.alee.laf.toolbar.ToolbarStyle;
import com.alee.laf.toolbar.WebToolBar;
import de.tudresden.inf.tcs.fcaapi.exception.IllegalObjectException;
import de.tudresden.inf.tcs.fcalib.FullObject;
import de.tudresden.inf.tcs.fcalib.action.StartExplorationAction;
import fcatools.conexpng.ContextChangeEvents;
import fcatools.conexpng.ProgramState;
import fcatools.conexpng.ProgramState.ContextChangeEvent;
import fcatools.conexpng.Util;
import fcatools.conexpng.gui.contexteditor.ContextEditor;
import fcatools.conexpng.gui.dependencies.DependencyView;
import fcatools.conexpng.gui.lattice.LatticeView;
import fcatools.conexpng.io.BurmeisterReader;
import fcatools.conexpng.io.BurmeisterWriter;
import fcatools.conexpng.io.CEXReader;
import fcatools.conexpng.io.CEXWriter;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.xml.stream.XMLStreamException;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import static fcatools.conexpng.Util.centerDialogInsideMainFrame;
import static fcatools.conexpng.Util.loadIcon;

public class MainFrame extends JFrame {

    private static final long serialVersionUID = -3768163989667340886L;

    private static final String MARGIN = "                              ";

    // Components
    private WebPanel mainPanel;
    private WebTabbedPane tabPane;
    private WebLabel viewTitleLabel;
    private View contextView;
    private View latticeView;
    private View associationView;
    private ProgramState state;

    private MainToolbar mainToolbar;

    public MainFrame(final ProgramState state) {
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
        Border margin = new EmptyBorder(0, 0, 1, 0);
        topPanel.setBorder(margin);
        topPanel.setPreferredSize(new Dimension(100, 27+1));
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
        statusBar.setPreferredSize(new Dimension(0,0));
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
        // toolbar.add ( WebButton.createIconWebButton ( loadIcon ( "toolbar/save.png" ), StyleConstants.smallRound, true ) );
        WebToolBar menuBar = new WebToolBar();
        menuBar.setToolbarStyle(ToolbarStyle.attached);
        menuBar.setFloatable(false);
        menuBar.add(new WebButton("New...") {{
//            setHotkey(Hotkey.CTRL_N);
            setEnabled(false);
        }});
        menuBar.add(new WebButton("Open...") {{
            addActionListener(new OpenAction(MainFrame.this, state));
        }});
        // TODO: open recent
//        menuBar.add(new WebMenu("Open recent") {{
//            add(new WebMenuItem("/tmp/cool.cex"));
//            add(new WebMenuItem("/Users/frank/projects/tealady.cex"));
//            add(new WebMenuItem("/Users/frank/projects/teaman.cex"));
//            setEnabled(false);
//        }});
        menuBar.addSeparator();
        menuBar.add(new WebButton("Save") {{
            setEnabled(false);
        }});
        menuBar.add(new WebButton("Save as...") {{
            addActionListener(new SaveAction(MainFrame.this, state));
        }});
        menuBar.addSeparator();
        menuBar.add(new WebButton("Import...") {{
            setEnabled(false);
        }});
        menuBar.add(new WebButton("Export...") {{
            setEnabled(false);
        }});
        menuBar.addSeparator();
        menuBar.add(new WebButton("Undo") {{
            setEnabled(false);
        }});
        menuBar.add(new WebButton("Redo") {{
            setEnabled(false);
        }});
        menuBar.addSeparator();
        menuBar.add(new WebButton("Count concepts") {{
            setEnabled(false);
        }});
        menuBar.add(new WebButton("Start exploration") {{
            addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent arg0) {
                    MyExpert expert = new MyExpert(MainFrame.this, state);
                    state.context.setExpert(expert);
                    expert.addExpertActionListener(state.context);
                    // Create an expert action for starting attribute exploration
                    StartExplorationAction<String, String, FullObject<String, String>> action = new StartExplorationAction<String, String, FullObject<String, String>>();
                    action.setContext(state.context);
                    // Fire the action, exploration starts...
                    expert.fireExpertAction(action);
                }
            });
        }});
        menuBar.addSeparator();
        menuBar.add(new WebButton("About") {{
            setEnabled(false);
        }});
        menuBar.add(new WebButton("Help") {{
//            setHotkey(Hotkey.ALT_F4);
            setEnabled(false);
        }});

//        menuPanel.add(menuBar);
        add(menuBar, BorderLayout.PAGE_START);
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


class OpenAction extends AbstractAction {
    MainFrame mainFrame;
    ProgramState state;

    public OpenAction(MainFrame mainFrame, ProgramState state) {
        this.mainFrame = mainFrame;
        this.state = state;
    }

    @Override
    public void actionPerformed(ActionEvent e) {

        final WebFileChooser fc = new WebFileChooser(mainFrame, "Open context");
        fc.setSelectionMode(SelectionMode.SINGLE_SELECTION);
        fc.setCurrentDirectory(state.lastOpened);
        fc.setVisible(true);

        if (fc.getResult() == StyleConstants.OK_OPTION) {
            File file = fc.getSelectedFile();
            String path = file.getAbsolutePath();

            state.lastOpened = path.substring(0, path.lastIndexOf(System.getProperty("file.separator")));
            state.filePath = path;
            mainFrame.setTitle("ConExp-NG - \"" + path + "\"");

            try {
                if (path.endsWith(".cex"))
                    new CEXReader(state);
                else
                    new BurmeisterReader(state);
            } catch (FileNotFoundException e1) {
                showMessageDialog("Can not find this file: " + path, true);
            } catch (IllegalObjectException | IOException
                    | XMLStreamException e1) {
                showMessageDialog(
                        "The file seems to be corrupt: " + e1.getMessage(),
                        true);
            }
        }
    }

    private void showMessageDialog(String message, boolean error) {
        JOptionPane pane = new JOptionPane(message);
        pane.setMessageType(error ? JOptionPane.ERROR_MESSAGE
                : JOptionPane.INFORMATION_MESSAGE);
        JDialog dialog = pane.createDialog(mainFrame, "Error");
        dialog.pack();
        centerDialogInsideMainFrame(mainFrame, dialog);
        dialog.setVisible(true);
    }
}

class SaveAction extends AbstractAction {
    MainFrame mainFrame;
    ProgramState state;

    public SaveAction(MainFrame mainFrame, ProgramState state) {
        this.mainFrame = mainFrame;
        this.state = state;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        state.filePath = "";
        if (state.filePath.isEmpty()) {
            final WebFileChooser fc = new WebFileChooser(mainFrame, "Save context");
            fc.setSelectionMode(SelectionMode.SINGLE_SELECTION);
            fc.setCurrentDirectory(state.lastOpened);
            fc.setVisible(true);

            if (fc.getResult() == StyleConstants.OK_OPTION) {
                File file = fc.getSelectedFile();
                String path = file.getAbsolutePath();
                state.lastOpened = path.substring(0, path.lastIndexOf(System.getProperty("file.separator")));
                state.filePath = path;
                mainFrame.setTitle("ConExp-NG - \"" + path + "\"");
            }
        }
        try {
            if (state.filePath.endsWith(".cex"))

                new CEXWriter(state);

            else
                new BurmeisterWriter(state);
        } catch (FileNotFoundException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        } catch (XMLStreamException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        } catch (IOException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
    }

}