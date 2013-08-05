package fcatools.conexpng.gui;

import de.tudresden.inf.tcs.fcalib.FullObject;
import de.tudresden.inf.tcs.fcalib.action.StartExplorationAction;
import fcatools.conexpng.Conf;
import fcatools.conexpng.Util;
import fcatools.conexpng.gui.MainFrame.OverwritingFileDiaolog;
import fcatools.conexpng.gui.MainFrame.UnsavedChangesDiaolog;
import fcatools.conexpng.io.CSVWriter;
import fcatools.conexpng.io.CXTReader;
import fcatools.conexpng.io.CXTWriter;
import fcatools.conexpng.io.CEXReader;
import fcatools.conexpng.io.CEXWriter;
import fcatools.conexpng.io.CSVReader;
import fcatools.conexpng.io.OALReader;
import fcatools.conexpng.io.OALWriter;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.alee.laf.filechooser.WebFileChooser;
import com.alee.extended.filefilter.FilesFilter;
import com.alee.extended.panel.GridPanel;
import com.alee.extended.panel.GroupPanel;
import com.alee.extended.panel.WebButtonGroup;
import com.alee.extended.panel.WebComponentPanel;
import com.alee.laf.button.WebButton;
import com.alee.laf.label.WebLabel;
import com.alee.laf.list.WebList;
import com.alee.laf.optionpane.WebOptionPane;
import com.alee.laf.rootpane.WebDialog;
import com.alee.laf.spinner.WebSpinner;
import com.alee.laf.toolbar.ToolbarStyle;
import com.alee.laf.toolbar.WebToolBar;
import com.alee.managers.hotkey.Hotkey;
import com.alee.managers.popup.PopupWay;
import com.alee.managers.popup.WebButtonPopup;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import static fcatools.conexpng.Util.loadIcon;

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
			return "Cex-Files";
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
			return "Context-Files";
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
		add(new WebButton(loadIcon("icons/jlfgr/New24.gif")) {
			{
				setDrawFocus(false);
				addHotkey(Hotkey.CTRL_N);
				setToolTipText("New context...");
				addActionListener(new ActionListener() {

					@Override
					public void actionPerformed(ActionEvent arg0) {
						if (!state.canBeSaved()) {
							final WebOptionPane pane = new WebOptionPane(
									"Some calculations haven't finished now. Do you want to wait?");
							Object[] options = { "I will wait", "I don't care" };
							pane.setOptions(options);
							pane.setMessageType(WebOptionPane.INFORMATION_MESSAGE);
							final WebDialog dialog = new WebDialog(mainFrame, " Still calculating");
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
							Util.centerDialogInsideMainFrame(mainFrame, dialog);
							dialog.setVisible(true);
							String n = (String) pane.getValue();
							if (n.equals("I will wait"))
								return;
						}
						if (state.unsavedChanges) {
							UnsavedChangesDiaolog ucd = mainFrame.new UnsavedChangesDiaolog();
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
						panel.addElement(new GridPanel(new WebLabel("#Attributes:"), attr));
						panel.addElement(new GridPanel(new WebLabel("#Objects:"), obj));
						final WebOptionPane pane = new WebOptionPane(panel, WebOptionPane.OK_OPTION);
						pane.setMessageType(WebOptionPane.PLAIN_MESSAGE);
						final WebDialog dialog = new WebDialog(mainFrame, "New Context", true);
						pane.addPropertyChangeListener(new PropertyChangeListener() {
							public void propertyChange(PropertyChangeEvent e) {
								if (dialog.isVisible() && (e.getSource() == pane)
										&& (e.getPropertyName().equals(WebOptionPane.VALUE_PROPERTY))) {
									dialog.setVisible(false);
								}
							}
						});
						dialog.setContentPane(pane);
						Object[] options = { "Okay" };
						pane.setOptions(options);
						dialog.pack();
						Util.centerDialogInsideMainFrame(mainFrame, dialog);
						dialog.setVisible(true);
						state.init((int) obj.getValue(), (int) attr.getValue());
					}
				});
			}
		});

		WebButton left = new WebButton(loadIcon("icons/jlfgr/Open24.gif")) {
			{
				setDrawFocus(false);
				setToolTipText("Open a CEX-file");
				addActionListener(new OpenAction(true));
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
						new CEXReader(state, state.filePath);

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
		saveButton = new WebButton(loadIcon("icons/jlfgr/Save24.gif")) {
			{
				setDrawFocus(false);
				setEnabled(false);
				addActionListener(new SaveAction(false, true));
				setToolTipText("Save the context in a CEX-file");
			}
		};
		add(saveButton);
		add(new WebButton(loadIcon("icons/jlfgr/SaveAs24.gif")) {
			{
				setDrawFocus(false);
				setToolTipText("Save as a CEX-file");
				addActionListener(new SaveAction(true, true));
			}
		});
		addSeparator();
		add(new WebButton(loadIcon("icons/jlfgr/Import24.gif")) {
			{
				setDrawFocus(false);
				addActionListener(new OpenAction(false));
				setToolTipText("Import a context");
			}
		});
		add(new WebButton(loadIcon("icons/jlfgr/Export24.gif")) {
			{
				setDrawFocus(false);
				setToolTipText("Export this context");
				addActionListener(new SaveAction(true, false));
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
						Util.showMessageDialog(mainFrame, "The number of concepts is " + state.getNumberOfConcepts()
								+ ".", false);
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
				addActionListener(new ActionListener() {

					@Override
					public void actionPerformed(ActionEvent arg0) {

					}
				});
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
					if (!path.contains("."))
						path = path.concat(".cxt");
					if (path.endsWith("cxt"))
						new CXTWriter(state, path);
					else if (path.endsWith("oal"))
						new OALWriter(state, path);
					else if (path.endsWith("csv"))
						new CSVWriter(state, path);
				} else {
					if (!path.contains("."))
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
			if (state.filePath.endsWith("untitled.cex") || saveAs) {
				final WebFileChooser fc = new WebFileChooser();
				fc.setCurrentDirectory(state.filePath.substring(0,
						state.filePath.lastIndexOf(System.getProperty("file.separator"))));
				final WebDialog dialog = new WebDialog(mainFrame, "Save file as", true);
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
								OverwritingFileDiaolog ofd = mainFrame.new OverwritingFileDiaolog(file);
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
			if (state.unsavedChanges) {
				UnsavedChangesDiaolog ucd = mainFrame.new UnsavedChangesDiaolog();
				if (ucd.isYes()) {
					new SaveAction(false, true).actionPerformed(e);
				} else if (ucd.isCancel())
					return;
			}
			final WebFileChooser fc = new WebFileChooser(state.filePath);
			final WebDialog dialog = new WebDialog(mainFrame, "Open file", true);

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
			// Doesn't work...
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
					else {
						Util.showMessageDialog(mainFrame, "This fileformat is unfortunality not supported", true);
						return;
					}

				} catch (Exception e1) {
					Util.handleIOExceptions(mainFrame, e1, path);
				}
			}
		}
	}
}
