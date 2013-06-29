package fcatools.conexpng.gui.lattice;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import de.tudresden.inf.tcs.fcalib.FullObject;
import fcatools.conexpng.ProgramState;
import fcatools.conexpng.model.FormalContext;

@SuppressWarnings("serial")
public class AccordionMenue extends JPanel implements ActionListener {

	// private javax.swing.ButtonGroup bg;
	// private List<ExpandableButton> buttons = new
	// ArrayList<ExpandableButton>();
	// private HashMap<ExpandableButton, JComponent> entries = new
	// HashMap<ExpandableButton, JComponent>();
	// AbstractAction click = new Click();
	//
	// public AccordionMenue() {
	// setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
	// bg = new javax.swing.ButtonGroup();
	// }
	//
	// /***
	// * Adds a entry to the menue
	// *
	// * @param title
	// * the name that is shown in the menue
	// * @param entry
	// * an arbitrary JComponent
	// */
	// public void addMenueEntry(String title, JComponent entry) {
	// ExpandableButton newButton = new ExpandableButton();
	// bg.add(newButton);
	// newButton.setAction(click);
	// newButton.setText(title);
	// newButton.setParentComp(this);
	// add(newButton);
	// if (buttons.size() == 0)
	// add(entry);
	// buttons.add(newButton);
	// entries.put(newButton, entry);
	// }
	//
	// /***
	// * With a defaultButton the size is reduced to the space it necassary
	// needs
	// * after a click in the menue
	// *
	// * @author David
	// *
	// */
	// private class ExpandableButton extends JToggleButton {
	//
	// private JComponent parent;
	//
	// @Override
	// public int getWidth() {
	// if (parent != null) {
	// super.setSize(parent.getWidth(), super.getHeight());
	// }
	// return super.getWidth();
	// }
	//
	// public void setParentComp(JComponent parent) {
	// this.parent = parent;
	// }
	// }
	//
	// private class Click extends AbstractAction {
	//
	// @Override
	// public void actionPerformed(ActionEvent e) {
	// removeAll();
	// // Otherwise you see "afterimages"
	// setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
	// setLayout(new BoxLayout(AccordionMenue.this, BoxLayout.Y_AXIS));
	// for (ExpandableButton button : buttons) {
	// add(button);
	// if (e.getSource().equals(button))
	// add(entries.get(button));
	// }
	// revalidate();
	// }
	//
	// }

	private JPanel topPanel = new JPanel(new GridLayout(1, 1));
	/**
	 * The bottom panel: contains the buttons displayed on the bottom of the
	 * JOutlookBar
	 */
	private JPanel bottomPanel = new JPanel(new GridLayout(1, 1));
	/**
	 * A LinkedHashMap of bars: we use a linked hash map to preserve the order
	 * of the bars
	 */
	private Map<String, BarInfo> bars = new LinkedHashMap<String, BarInfo>();
	/**
	 * The currently visible bar (zero-based index)
	 */
	private int visibleBar = 0;
	/**
	 * A place-holder for the currently visible component
	 */
	private JComponent visibleComponent = null;
	private static ProgramState state;
	private static FormalContext context;
	private static List<JCheckBox> attributeCheckBoxes;
	private static List<JCheckBox> objectCheckBoxes;

	/**
	 * Creates a new JOutlookBar; after which you should make repeated calls to
	 * addBar() for each bar
	 */
	public AccordionMenue(ProgramState state) {
		this.setLayout(new BorderLayout());
		this.add(topPanel, BorderLayout.NORTH);
		this.add(bottomPanel, BorderLayout.SOUTH);

		attributeCheckBoxes = new ArrayList<JCheckBox>();
		objectCheckBoxes = new ArrayList<JCheckBox>();
		this.state = state;
		this.context = state.context;
		
		
		
		
		
		this.addBar("Lattice", getLatticePanel());
		this.addBar("Objects", getObjectPanel());
		this.addBar("Attributes", getAttributePanel());
		this.setVisibleBar(2);
	}

	/**
	 * Adds the specified component to the JOutlookBar and sets the bar's name
	 * 
	 * @param name
	 *            The name of the outlook bar
	 * @param componenet
	 *            The component to add to the bar
	 */
	public void addBar(String name, JComponent component) {
		BarInfo barInfo = new BarInfo(name, component);
		barInfo.getButton().addActionListener(this);
		this.bars.put(name, barInfo);
		render();
	}

	/**
	 * Adds the specified component to the JOutlookBar and sets the bar's name
	 * 
	 * @param name
	 *            The name of the outlook bar
	 * @param icon
	 *            An icon to display in the outlook bar
	 * @param componenet
	 *            The component to add to the bar
	 */
	public void addBar(String name, Icon icon, JComponent component) {
		BarInfo barInfo = new BarInfo(name, icon, component);
		barInfo.getButton().addActionListener(this);
		this.bars.put(name, barInfo);
		render();
	}

	/**
	 * Removes the specified bar from the JOutlookBar
	 * 
	 * @param name
	 *            The name of the bar to remove
	 */
	public void removeBar(String name) {
		this.bars.remove(name);
		render();
	}

	/**
	 * Returns the index of the currently visible bar (zero-based)
	 * 
	 * @return The index of the currently visible bar
	 */
	public int getVisibleBar() {
		return this.visibleBar;
	}

	/**
	 * Programmatically sets the currently visible bar; the visible bar index
	 * must be in the range of 0 to size() - 1
	 * 
	 * @param visibleBar
	 *            The zero-based index of the component to make visible
	 */
	public void setVisibleBar(int visibleBar) {
		if (visibleBar > 0 && visibleBar < this.bars.size() - 1) {
			this.visibleBar = visibleBar;
			render();
		}
	}

	/**
	 * Causes the outlook bar component to rebuild itself; this means that it
	 * rebuilds the top and bottom panels of bars as well as making the
	 * currently selected bar's panel visible
	 */
	public void render() {
		// Compute how many bars we are going to have where
		int totalBars = this.bars.size();
		int topBars = this.visibleBar + 1;
		int bottomBars = totalBars - topBars;
		// Get an iterator to walk through out bars with
		Iterator<String> itr = this.bars.keySet().iterator();
		// Render the top bars: remove all components, reset the GridLayout to
		// hold to correct number of bars, add the bars, and "validate" it to
		// cause it to re-layout its components
		this.topPanel.removeAll();
		GridLayout topLayout = (GridLayout) this.topPanel.getLayout();
		topLayout.setRows(topBars);
		BarInfo barInfo = null;
		for (int i = 0; i < topBars; i++) {
			if(itr.hasNext()){
				String barName = (String) itr.next();
				barInfo = (BarInfo) this.bars.get(barName);
				this.topPanel.add(barInfo.getButton());
			}

		}
		this.topPanel.validate();
		// Render the center component: remove the current component (if there
		// is one) and then put the visible component in the center of this
		// panel
		if (this.visibleComponent != null) {
			this.remove(this.visibleComponent);
		}
		this.visibleComponent = barInfo.getComponent();
		this.add(visibleComponent, BorderLayout.CENTER);
		// Render the bottom bars: remove all components, reset the GridLayout
		// to
		// hold to correct number of bars, add the bars, and "validate" it to
		// cause it to re-layout its components
		this.bottomPanel.removeAll();
		GridLayout bottomLayout = (GridLayout) this.bottomPanel.getLayout();
		bottomLayout.setRows(bottomBars);
		for (int i = 0; i < bottomBars; i++) {
			String barName = (String) itr.next();
			barInfo = (BarInfo) this.bars.get(barName);
			this.bottomPanel.add(barInfo.getButton());
		}
		this.bottomPanel.validate();
		// Validate all of our components: cause this container to re-layout its
		// subcomponents
		validate();
	}

	/**
	 * Invoked when one of our bars is selected
	 */
	public void actionPerformed(ActionEvent e) {
		int currentBar = 0;
		for (Iterator<String> i = this.bars.keySet().iterator(); i.hasNext();) {
			String barName = (String) i.next();
			BarInfo barInfo = (BarInfo) this.bars.get(barName);
			if (barInfo.getButton() == e.getSource()) {
				// Found the selected button
				this.visibleBar = currentBar;
				render();
				return;
			}
			currentBar++;
		}
	}

	/**
	 * Debug, dummy method
	 */

	/**
	 * Internal class that maintains information about individual Outlook bars;
	 * specifically it maintains the following information:
	 * 
	 * name The name of the bar button The associated JButton for the bar
	 * component The component maintained in the Outlook bar
	 */
	class BarInfo {
		/**
		 * The name of this bar
		 */
		private String name;
		/**
		 * The JButton that implements the Outlook bar itself
		 */
		private JButton button;
		/**
		 * The component that is the body of the Outlook bar
		 */
		private JComponent component;

		/**
		 * Creates a new BarInfo
		 * 
		 * @param name
		 *            The name of the bar
		 * @param component
		 *            The component that is the body of the Outlook Bar
		 */
		public BarInfo(String name, JComponent component) {
			this.name = name;
			this.component = component;
			this.button = new JButton(name);
		}

		/**
		 * Creates a new BarInfo
		 * 
		 * @param name
		 *            The name of the bar
		 * @param icon
		 *            JButton icon
		 * @param component
		 *            The component that is the body of the Outlook Bar
		 */
		public BarInfo(String name, Icon icon, JComponent component) {
			this.name = name;
			this.component = component;
			this.button = new JButton(name, icon);
		}

		/**
		 * Returns the name of the bar
		 * 
		 * @return The name of the bar
		 */
		public String getName() {
			return this.name;
		}

		/**
		 * Sets the name of the bar
		 * 
		 * @param The
		 *            name of the bar
		 */
		public void setName(String name) {
			this.name = name;
		}

		/**
		 * Returns the outlook bar JButton implementation
		 * 
		 * @return The Outlook Bar JButton implementation
		 */
		public JButton getButton() {
			return this.button;
		}

		/**
		 * Returns the component that implements the body of this Outlook Bar
		 * 
		 * @return The component that implements the body of this Outlook Bar
		 */
		public JComponent getComponent() {
			return this.component;
		}
	}
	
	private static JPanel getObjectPanel(){
		JPanel panel = new JPanel(new BorderLayout());
		panel.setLayout(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.WEST;
		gbc.gridx = 0;
		gbc.gridy = 0;
		for(FullObject<String, String> s : context.getObjects()){
			gbc.gridy++;
			JCheckBox box = new JCheckBox(s.getIdentifier());
			box.setSelected(true);
			panel.add(box, gbc);
			objectCheckBoxes.add(box);
		}
		return panel;
	}
	
	private static JPanel getAttributePanel(){
		JPanel panel = new JPanel(new BorderLayout());
		panel.setLayout(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.WEST;
        gbc.gridx = 0;
		gbc.gridy = 0;
		for(String s : context.getAttributes()){
			gbc.gridy++;
			JCheckBox box = new JCheckBox(s);
			box.addActionListener(new ActionListener() {
				
				@Override
				public void actionPerformed(ActionEvent arg0) {
					
				}
			});
			box.setSelected(true);
			panel.add(box, gbc);
			attributeCheckBoxes.add(box);
		}
		return panel;
	}
	
	private static JPanel getLatticePanel(){
		JPanel panel = new JPanel(new BorderLayout());
		panel.setLayout(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.WEST;
        gbc.gridx = 0;
		gbc.gridy = 0;
		
		
		
		panel.add(getLatticeObjectPanel(), gbc);
		
		gbc.gridx = 1;
	
		
		panel.add(getLatticeAttrPanel(), gbc);
		
		gbc.gridx = 0;
		gbc.gridy++;
		panel.add(new JLabel("Edges:"), gbc);
		final JRadioButton noneEdges = new JRadioButton("none");
		gbc.gridy++;
		final JRadioButton showEdges = new JRadioButton("show");
		showEdges.setSelected(true);
		noneEdges.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				state.showEdges = false;
				noneEdges.setSelected(true);
				showEdges.setSelected(false);
				state.showLabelsChanged();
			}
		});		
		showEdges.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				state.showEdges = true;
				showEdges.setSelected(true);
				noneEdges.setSelected(false);
				state.showLabelsChanged();
			}
		});
		
		panel.add(noneEdges, gbc);
		gbc.gridx = 1;
		panel.add(showEdges, gbc);

		
		
		
		
		return panel;
	}
	
	private static JPanel getLatticeObjectPanel(){
		JPanel panelObjects = new JPanel(new BorderLayout());
		panelObjects.setLayout(new GridBagLayout());
		GridBagConstraints gbo = new GridBagConstraints();
		gbo.anchor = GridBagConstraints.WEST;
		gbo.gridx = 0;
		gbo.gridy = 1;
		panelObjects.add(new JLabel("Objects:"), gbo);
		gbo.gridy = 2;
		final JRadioButton noneObjects = new JRadioButton();
		noneObjects.setText("none");
		noneObjects.setSelected(true);
		
		final JRadioButton labelsObjects = new JRadioButton();
		labelsObjects.setText("labels");
		
		noneObjects.addActionListener(new ActionListener() {		
			@Override
			public void actionPerformed(ActionEvent e) {
				state.showObjectLabel = false;
				noneObjects.setSelected(true);
				labelsObjects.setSelected(false);
				state.showLabelsChanged();
			}
		});
		labelsObjects.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				state.showObjectLabel = true;
				labelsObjects.setSelected(true);
				noneObjects.setSelected(false);
				state.showLabelsChanged();
			}
		});
		panelObjects.add(noneObjects, gbo);
		gbo.gridy = 3;
		panelObjects.add(labelsObjects, gbo);
		return panelObjects;
	}
	
	
	private static JPanel getLatticeAttrPanel(){
		JPanel panelAttributes = new JPanel(new BorderLayout());
		panelAttributes.setLayout(new GridBagLayout());
		GridBagConstraints gba = new GridBagConstraints();
		gba.anchor = GridBagConstraints.WEST;
		gba.gridx = 0;
		gba.gridy = 1;
		panelAttributes.add(new JLabel("Attributes:"), gba);
		gba.gridy = 2;
		final JRadioButton noneAttributes = new JRadioButton();
		noneAttributes.setText("none");
		noneAttributes.setSelected(true);
		final JRadioButton labelsAttributes = new JRadioButton();
		labelsAttributes.setText("labels");
		
		noneAttributes.addActionListener(new ActionListener() {		
			@Override
			public void actionPerformed(ActionEvent e) {
				state.showAttributLabel = false;
				noneAttributes.setSelected(true);
				labelsAttributes.setSelected(false);
				state.showLabelsChanged();
			}
		});
		labelsAttributes.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				state.showAttributLabel = true;
				labelsAttributes.setSelected(true);
				noneAttributes.setSelected(false);
				state.showLabelsChanged();
			}
		});
		panelAttributes.add(noneAttributes, gba);
		gba.gridy = 3;	
		panelAttributes.add(labelsAttributes, gba);
		return panelAttributes;
	}
	
	public void update(){
		this.removeBar("Objects");
		objectCheckBoxes.clear();
		this.addBar("Objects", getObjectPanel());
		this.removeBar("Attributes");
		attributeCheckBoxes.clear();
		this.addBar("Attributes", getAttributePanel());
	}
}
