package fcatools.conexpng.gui.lattice;

import java.awt.Color;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.JPanel;

import de.tudresden.inf.tcs.fcalib.utils.ListSet;

/**
 * This class implementes the nodes of the lattice graph. It is the model for
 * the concepts. Each concept are represented as a node which knows his position
 * , the objects and attributes as well as the labels which contained visible
 * objects resp. attributes.
 * 
 */
public class Node extends JPanel implements LatticeGraphElement {

    /**
     *
     */
    private static final long serialVersionUID = 4253192979583459657L;
    private Set<String> objects;
    private Set<String> attributes;
    private int x;
    private int y;
    // list of parent nodes
    private List<Node> parents = new ArrayList<Node>();
    // list of child nodes
    private List<Node> children = new ArrayList<Node>();
    private ListSet<Node> ideal;
    private ListSet<Node> filter;
    private boolean isIdealVisibile;
    // true if this node was clicked on
    private boolean clickedOn;
    private Label visibleObjects;
    private Label visibleAttributes;
    // true if this node and its subgraph shall be moved.
    private boolean moveSubgraph;
    // level of the node in the graph
    private int level;
    // allow node position change only if lattice structure is maintained
    // meaning the top-down-order of the nodes is maintained
    private boolean structureSafeMove = true;

    /**
     * 
     * @param extent
     * @param intent
     * @param x
     * @param y
     */
    public Node(Set<String> extent, Set<String> intent, int x, int y) {
        this.objects = extent;
        this.attributes = intent;
        this.x = x;
        this.y = y;
        this.setBounds(x, y, 15, 15);
        positionLabels();
    }

    /**
     *
     */
    public Node() {
        this.objects = new TreeSet<>();
        this.attributes = new TreeSet<>();
        this.visibleObjects = new Label(new TreeSet<String>(), this);
        this.visibleAttributes = new Label(new TreeSet<String>(), this);
        this.ideal = new ListSet<>();
        this.x = 0;
        this.y = 0;
        this.setBounds(x, y, 15, 15);
        this.setBackground(Color.white);
        this.filter = new ListSet<>();
    }

    @Override
    public void paint(Graphics g) {
    }

    /**
     * 
     * @return
     */
    public int getX() {
        return x;
    }

    /**
     * 
     * @param x
     */
    public void setX(int x) {
        this.x = x;
        this.setBounds(x, y, 15, 15);
    }

    /**
     * 
     * @return
     */
    public int getY() {
        return y;
    }

    /**
     * 
     * @param y
     */
    public void setY(int y) {
        this.y = y;
        this.setBounds(x, y, 15, 15);
    }

    /**
     * 
     * @param extent
     */
    public void addObject(String extent) {
        objects.add(extent);
    }

    /**
     * 
     * @param set
     */
    public void addAttribute(String set) {
        attributes.add(set);
    }

    /**
     * Adds a parent node to this node.
     * 
     * @param n
     *            node to add as a parent
     */
    public void addParentNode(Node n) {
        parents.add(n);
    }

    /**
     * Returns this node's parent nodes.
     * 
     * @return this node's parent nodes
     */
    public List<Node> getParentNodes() {
        return parents;
    }

    /**
     * Adds a child node to this node.
     * 
     * @param n
     *            node to add as a child
     */
    public void addChildNode(Node n) {
        children.add(n);
    }

    /**
     * Returns this node's child nodes.
     * 
     * @return this node's child nodes
     */
    public List<Node> getChildNodes() {
        return children;
    }

    /**
     * Positions attribute and object labels.
     */
    public void positionLabels() {
        // places the object label below and the attribute
        // label above the node
        visibleAttributes.update(x + (int) (LatticeView.radius * 1.5), (int) (y - LatticeView.radius * 5));
        visibleObjects.update(x + (int) (LatticeView.radius * 1.5), y + LatticeView.radius * 5);
    }

    /**
     * Update the node and associated label positions. Maintains correct
     * top-down-order of nodes. Moves subgraph if selected.
     * 
     * @param x
     *            position the node shall be moved to
     * @param y
     *            position the node shall be moved to
     */
    public void update(int x, int y) {
        // check if node can be moved with respect to node order
        if (isUpdatePossible(y)) {
            // move subgraph
            if (moveSubgraph) {
                // calculate offset the node is moved
                int offsetX = (int) ((x - this.x) / LatticeView.zoomFactor);
                int offsetY = (int) ((y - this.y) / LatticeView.zoomFactor);
                // check if subgraph can be moved with respect to node order
                for (Node n : ideal) {
                    if (!n.isUpdatePossible(n.y + offsetY)) {
                        structureSafeMove = false;
                    }
                }
                if (structureSafeMove) {
                    // move subgraph
                    for (Node n : ideal) {
                        n.updatePosition(n.x + offsetX, n.y + offsetY);
                    }
                    // move node
                    updatePosition(x, y);
                }
            } else {
                // move node
                updatePosition(x, y);
            }
            structureSafeMove = true;

            if (getParent() != null) {
                getParent().repaint();
            }
        }
    }

    /**
     * Helper method to update node and label positions.
     * 
     * @param x
     *            position the node shall be moved to
     * @param y
     *            position the node shall be moved to
     */
    private void updatePosition(int x, int y) {
        this.setBounds(x, y, 15, 15);
        this.x = x;
        this.y = y;
        positionLabels();
    }

    /**
     * Checks if an update of this node is possible without changing the
     * top-down-order of the nodes in the lattice.
     * 
     * @param y
     *            position the node shall be moved to
     * @return true if update is possible, false if not
     */
    public boolean isUpdatePossible(int y) {
        // check if parents are still above this node after position update
        for (Node n : getParentNodes()) {
            if (n.getY() + LatticeView.radius * 2 > y) {
                return false;
            }
        }
        // check if children are still below this node after position update
        for (Node n : getChildNodes()) {
            if (n.getY() - LatticeView.radius * 2 < y) {
                return false;
            }
        }
        return true;
    }

    public ListSet<Node> getIdeal() {
        return ideal;
    }

    public void addObjects(Set<String> extent) {
        objects.addAll(extent);

    }

    public void addAttributs(Set<String> intent) {
        attributes.addAll(intent);

    }

    public Set<String> getObjects() {
        return objects;
    }

    public Set<String> getAttributes() {
        return attributes;
    }

    public void setLevel(int level) {
        this.level = level;

    }

    public int getLevel() {
        return level;
    }

    public void setVisibleObject(String object) {
        visibleObjects.getSet().add(object);
    }

    public void setVisibleObjects(Set<String> objects) {
        visibleObjects.getSet().clear();
        visibleObjects.getSet().addAll(objects);
    }

    public Set<String> getVisibleObjects() {
        return this.visibleObjects.getSet();
    }

    public void setVisibleAttribute(String attribute) {
        this.visibleAttributes.getSet().add(attribute);
    }

    public void setVisibleAttributes(Set<String> attributes) {
        visibleAttributes.getSet().clear();
        visibleAttributes.getSet().addAll(attributes);
    }

    public Set<String> getVisibleAttributes() {
        return this.visibleAttributes.getSet();

    }

    public Label getObjectsLabel() {
        return this.visibleObjects;
    }

    public Label getAttributesLabel() {
        return this.visibleAttributes;
    }

    public void moveSubgraph(boolean b) {
        this.moveSubgraph = b;
    }

    public void toggleIdealVisibility() {
        ((LatticeGraphView) getParent()).resetHighlighting();
        this.isIdealVisibile = !this.isIdealVisibile;
        // this node was clicked on, necessary for red highlight circle
        this.setClickedOn(true);
        for (Node n : ideal) {
            n.setPartOfAnIdeal(isIdealVisibile);
        }
        if (getParent() != null) {
            getParent().repaint();
        }
    }

    /**
     * Returns true if this is the node that was clicked on. Needed to highlight
     * the node red.
     * 
     * @return true if this is the node that was clicked on
     */
    public boolean isClickedOn() {
        return this.clickedOn;
    }

    /**
     * Set to true if this node was clicked on. Results in a red circle around
     * the node to highlight it.
     * 
     * @param b
     */
    public void setClickedOn(boolean b) {
        this.clickedOn = b;
    }

    public boolean isPartOfAnIdeal() {
        return this.isIdealVisibile;
    }

    public void setPartOfAnIdeal(boolean b) {
        this.isIdealVisibile = b;
    }

    public ListSet<Node> getFilter() {
        return this.filter;
    }

}
