package fcatools.conexpng.gui;

import de.tudresden.inf.tcs.fcaapi.exception.IllegalObjectException;
import de.tudresden.inf.tcs.fcalib.FullObject;
import de.tudresden.inf.tcs.fcalib.action.StartExplorationAction;
import fcatools.conexpng.Conf;
import fcatools.conexpng.Util;
import fcatools.conexpng.io.BurmeisterReader;
import fcatools.conexpng.io.BurmeisterWriter;
import fcatools.conexpng.io.CEXReader;
import fcatools.conexpng.io.CEXWriter;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.xml.stream.XMLStreamException;

import com.alee.extended.panel.GroupPanel;
import com.alee.extended.panel.WebButtonGroup;
import com.alee.laf.button.WebButton;
import com.alee.laf.list.WebList;
import com.alee.laf.rootpane.WebFrame;
import com.alee.laf.toolbar.ToolbarStyle;
import com.alee.laf.toolbar.WebToolBar;
import com.alee.managers.hotkey.Hotkey;
import com.alee.managers.popup.PopupWay;
import com.alee.managers.popup.WebButtonPopup;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import static fcatools.conexpng.Util.centerDialogInsideMainFrame;
import static fcatools.conexpng.Util.loadIcon;

public class MainToolbar extends WebToolBar {

    private Conf state;

    private WebFrame mainFrame;

    private static WebButton redoButton;
    private static WebButton undoButton;

    private static final long serialVersionUID = -3495670613141172867L;

    @SuppressWarnings("serial")
    public MainToolbar(final WebFrame mainFrame, final Conf state) {
        this.mainFrame = mainFrame;
        this.state = state;
        this.setFloatable(false);

        setToolbarStyle(ToolbarStyle.attached);
        setFloatable(false);
        add(new WebButton(loadIcon("icons/jlfgr/New24.gif")) {
            {
                setDrawFocus(false);
                addHotkey(Hotkey.CTRL_N);
                setToolTipText("New context...");
                addActionListener(new ActionListener() {

                    @Override
                    public void actionPerformed(ActionEvent arg0) {
                        Conf.init();
                    }
                });
            }
        });

        WebButton left = new WebButton(loadIcon("icons/jlfgr/Open24.gif")) {
            {
                setDrawFocus(false);
                setToolTipText("Open a CEX-file");
                addActionListener(new OpenAction());
            }
        };

        final WebButton right = new WebButton(loadIcon("icons/arrow_down.png"));
        right.setDrawFocus(false);
        right.setToolTipText("Open a previous CEX-file");
        final WebButtonPopup popup = new WebButtonPopup(right, PopupWay.downRight);

        WebList list = new WebList(state.lastOpened);
        list.setVisibleRowCount(4);
        list.setEditable(false);
        list.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting() && !((WebList) e.getSource()).isSelectionEmpty()) {
                    popup.hidePopup();

                    state.setNewFile(String.copyValueOf(((String) ((WebList) e.getSource()).getSelectedValue())
                            .toCharArray()));
                    try {
                        if (state.filePath.endsWith(".cex"))

                            new CEXReader(state);

                        else
                            new BurmeisterReader(state);
                    } catch (Exception e1) {
                        Util.handleIOExceptions(MainToolbar.this.mainFrame, e1, state.filePath);
                    }
                    ((WebList) e.getSource()).clearSelection();
                }
            }
        });
        popup.setContent(new GroupPanel(list));

        add(new WebButtonGroup(true, left, right));

        addSeparator();
        add(new WebButton(loadIcon("icons/jlfgr/Save24.gif")) {
            {
                setDrawFocus(false);
                setEnabled(true);
                setToolTipText("Save the context in a CEX-file");
            }
        });
        add(new WebButton(loadIcon("icons/jlfgr/SaveAs24.gif")) {
            {
                setDrawFocus(false);
                setToolTipText("Save as a CEX-file");
                addActionListener(new SaveAction(true));
            }
        });
        addSeparator();
        add(new WebButton(loadIcon("icons/jlfgr/Import24.gif")) {
            {
                setDrawFocus(false);
                setEnabled(true);
                setToolTipText("Import a context");
            }
        });
        add(new WebButton(loadIcon("icons/jlfgr/Export24.gif")) {
            {
                setDrawFocus(false);
                setToolTipText("Export this context");
                setEnabled(true);
            }
        });
        addSeparator();
        undoButton = new WebButton(loadIcon("icons/jlfgr/Undo24.gif")) {
            {
                setEnabled(false);
                setDrawFocus(false);
                setToolTipText("Undo");
                addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent arg0) {
                        state.undo();
                        getRedoButton().setEnabled(state.canRedo());
                        getUndoButton().setEnabled(state.canUndo());
                    }
                });
            }
        };
        redoButton = new WebButton(loadIcon("icons/jlfgr/Redo24.gif")) {
            {
                setEnabled(false);
                setDrawFocus(false);
                setToolTipText("Redo");
                addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent arg0) {
                        state.redo();
                        getRedoButton().setEnabled(state.canRedo());
                        getUndoButton().setEnabled(state.canUndo());
                    }
                });
            }
        };
        add(undoButton);
        add(redoButton);
        addSeparator();
        add(new WebButton(loadIcon("icons/jlfgr/TipOfTheDay24.gif")) {
            {
                setDrawFocus(false);
                setEnabled(true);
                setToolTipText("Show the number of concepts");
                addActionListener(new ActionListener() {

                    @Override
                    public void actionPerformed(ActionEvent e) {
                        System.out.println(state.concepts);
                        showMessageDialog("The number of concepts is " + state.getNumberOfConcepts() + ".", false);
                    }
                });
            }
        });
        add(new WebButton(loadIcon("icons/jlfgr/Replace24.gif")) {
            {
                setToolTipText("Start Attribute Exploration");
                setDrawFocus(false);
                addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent arg0) {
                        MyExpert expert = new MyExpert(MainToolbar.this.mainFrame, state);
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
        });
        addSeparator();
        add(new WebButton(loadIcon("icons/jlfgr/About24.gif")) {
            {
                setDrawFocus(false);
                setEnabled(true);
                setToolTipText("About");
            }
        });
        add(new WebButton(loadIcon("icons/jlfgr/Help24.gif")) {
            {
                setDrawFocus(false);
                addHotkey(Hotkey.ALT_F4);
                setEnabled(true);
                setToolTipText("Help");
            }
        });
    }

    public static WebButton getUndoButton() {
        return undoButton;
    }

    public static WebButton getRedoButton() {
        return redoButton;
    }

    // TODO: Needs to be improved
    private void showMessageDialog(String message, boolean error) {
        JOptionPane pane = new JOptionPane(message);
        pane.setMessageType(error ? JOptionPane.ERROR_MESSAGE : JOptionPane.INFORMATION_MESSAGE);
        JDialog dialog = pane.createDialog(mainFrame, error ? "Error" : "Message");
        dialog.pack();
        centerDialogInsideMainFrame(mainFrame, dialog);
        dialog.setVisible(true);
    }

    // ////////////////////////////////////////////////////////////////////////

    @SuppressWarnings("serial")
    class SaveAction extends AbstractAction {

        private boolean saveAs;

        public SaveAction(boolean saveAS) {
            this.saveAs = saveAS;
        }

        private void saveFile(String path) {
            try {
                MainToolbar.this.state.filePath = path;
                if (MainToolbar.this.state.filePath.endsWith(".cex"))

                    new CEXWriter(MainToolbar.this.state);

                else
                    new BurmeisterWriter(MainToolbar.this.state);
                System.out.println(path);
                MainToolbar.this.state.setNewFile(path);

                mainFrame.setTitle("ConExp-NG - \"" + path + "\"");
            } catch (FileNotFoundException e1) {
                showMessageDialog("Can not find this file: " + path, true);
            } catch (IOException | XMLStreamException e1) {
                showMessageDialog("The file seems to be corrupt: " + e1.getMessage(), true);
            }

        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if (state.filePath.equals("untitled.cex") || saveAs) {
                final JFileChooser fc = new JFileChooser(state.filePath);
                final JDialog dialog = new JDialog(mainFrame, "Save file as", true);
                dialog.setContentPane(fc);
                fc.setDialogType(JFileChooser.SAVE_DIALOG);
                fc.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        String state = (String) e.getActionCommand();
                        if ((state.equals(JFileChooser.APPROVE_SELECTION) && fc.getSelectedFile() != null)) {
                            File file = fc.getSelectedFile();
                            String path = file.getAbsolutePath();
                            if (file.exists()) {
                                JOptionPane pane = new JOptionPane(new JLabel("Do you really want to overwrite "
                                        + file.getName() + "?"), JOptionPane.YES_NO_OPTION);
                                pane.setMessageType(JOptionPane.QUESTION_MESSAGE);
                                JDialog dialog2 = pane.createDialog(mainFrame, "Overwriting existing file?");
                                Object[] options = { "Yes", "No" };
                                pane.setOptions(options);
                                dialog2.pack();
                                dialog2.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
                                Util.centerDialogInsideMainFrame(mainFrame, dialog2);
                                dialog2.setVisible(true);
                                String n = (String) pane.getValue();
                                if (n != null)
                                    if (n.equals("Yes")) {
                                        saveFile(path);
                                        dialog.setVisible(false);
                                    }
                            } else {
                                MainToolbar.this.state.filePath = path;
                                saveFile(path);
                                dialog.setVisible(false);
                            }
                        } else if (state.equals(JFileChooser.CANCEL_SELECTION)) {
                            dialog.setVisible(false);
                            return;
                        }
                    }
                });
                dialog.pack();
                Util.centerDialogInsideMainFrame(mainFrame, dialog);
                dialog.setVisible(true);

                if (fc.getSelectedFile() != null) {

                } else
                    return;
            } else {
                saveFile(MainToolbar.this.state.filePath);
            }
        }

    }

    @SuppressWarnings("serial")
    class OpenAction extends AbstractAction {

        @Override
        public void actionPerformed(ActionEvent e) {

            final JFileChooser fc = new JFileChooser(state.filePath);
            final JDialog dialog = new JDialog(mainFrame, "Open file", true);

            dialog.setContentPane(fc);
            fc.setDialogType(JFileChooser.OPEN_DIALOG);
            fc.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    String state = (String) e.getActionCommand();
                    if ((state.equals(JFileChooser.APPROVE_SELECTION) && fc.getSelectedFile() != null)
                            || state.equals(JFileChooser.CANCEL_SELECTION)) {
                        dialog.setVisible(false);
                    }
                }
            });
            dialog.pack();
            Util.centerDialogInsideMainFrame(mainFrame, dialog);
            dialog.setVisible(true);

            if (fc.getSelectedFile() != null) {
                File file = fc.getSelectedFile();
                String path = file.getAbsolutePath();
                state.setNewFile(path);
                mainFrame.setTitle("ConExp-NG - \"" + path + "\"");

                try {
                    if (path.endsWith(".cex"))

                        new CEXReader(state);

                    else
                        new BurmeisterReader(state);
                } catch (FileNotFoundException e1) {
                    showMessageDialog("Can not find this file: " + path, true);
                } catch (IllegalObjectException | IOException | XMLStreamException | IllegalArgumentException
                        | IllegalAccessException | NoSuchFieldException | SecurityException e1) {
                    showMessageDialog("The file seems to be corrupt: " + e1.getMessage(), true);
                }
            }
        }
    }
}
