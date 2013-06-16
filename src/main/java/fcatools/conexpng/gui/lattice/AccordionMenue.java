package fcatools.conexpng.gui.lattice;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@SuppressWarnings("serial")
public class AccordionMenue extends JPanel {

    private javax.swing.ButtonGroup bg;
    private List<ExpandableButton> buttons = new ArrayList<ExpandableButton>();
    private HashMap<ExpandableButton, JComponent> entries = new HashMap<ExpandableButton, JComponent>();
    AbstractAction click = new Click();

    public AccordionMenue() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        bg = new javax.swing.ButtonGroup();
    }

    /***
     * Adds a entry to the menue
     *
     * @param title
     *            the name that is shown in the menue
     * @param entry
     *            an arbitrary JComponent
     */
    public void addMenueEntry(String title, JComponent entry) {
        ExpandableButton newButton = new ExpandableButton();
        bg.add(newButton);
        newButton.setAction(click);
        newButton.setText(title);
        newButton.setParentComp(this);
        add(newButton);
        if (buttons.size() == 0)
            add(entry);
        buttons.add(newButton);
        entries.put(newButton, entry);
    }

    /***
     * With a defaultButton the size is reduced to the space it necassary needs
     * after a click in the menue
     *
     * @author David
     *
     */
    private class ExpandableButton extends JToggleButton {

        private JComponent parent;

        @Override
        public int getWidth() {
            if (parent != null) {
                super.setSize(parent.getWidth(), super.getHeight());
            }
            return super.getWidth();
        }

        public void setParentComp(JComponent parent) {
            this.parent = parent;
        }
    }

    private class Click extends AbstractAction {

        @Override
        public void actionPerformed(ActionEvent e) {
            removeAll();
            // Otherwise you see "afterimages"
            setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
            setLayout(new BoxLayout(AccordionMenue.this, BoxLayout.Y_AXIS));
            for (ExpandableButton button : buttons) {
                add(button);
                if (e.getSource().equals(button))
                    add(entries.get(button));
            }
            revalidate();
        }

    }

}
