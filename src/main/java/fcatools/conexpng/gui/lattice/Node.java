package fcatools.conexpng.gui.lattice;

import javax.swing.*;

import de.tudresden.inf.tcs.fcalib.utils.ListSet;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class Node extends JPanel implements LatticeGraphElement {

    /**
     *
     */
    private static final long serialVersionUID = 4253192979583459657L;
    private Set<String> objects;
    private Set<String> attributes;
    private int x;
    private int y;
    private List<Node> below;
    private ListSet<Node> ideal;
    private boolean isIdealVisibile;
    private Label visibleObjects;
    private Label visibleAttributes;
    private boolean moveSubgraph;
    private int level;

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
        this.below = new ArrayList<>();

    }

    /**
     *
     */
    public Node() {
        this.objects = new TreeSet<>();
        this.attributes = new TreeSet<>();
        this.visibleObjects = new Label(new TreeSet<String>());
        this.visibleAttributes = new Label(new TreeSet<String>());
        this.x = 0;
        this.y = 0;
        this.setBounds(x, y, 15, 15);
        this.setBackground(Color.white);
        this.below = new ArrayList<>();
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
    public void addAttribut(String set) {
        attributes.add(set);
    }

    public void addBelowNode(Node n) {
        below.add(n);
    }

    


    public List<Node> getBelow() {
		return below;
	}

	public void update(int x, int y, boolean first) {
        int updateX;
        int updateY;
        if(this.x + x >= 0) updateX = this.x + x;
        else updateX = 0;
        if(this.y + y >= 0) updateY = this.y + y;
        else updateY = 0;

        visibleAttributes.update(x, y, false);
        visibleObjects.update(x, y, true);
        if (moveSubgraph && first) {
            for(Node n : ideal){
                n.update(x, y, false);
            }
        }

        this.setBounds(updateX, updateY, 15, 15);
        this.x = updateX;
        this.y = updateY;

        if (getParent() != null) {
            getParent().repaint();
        }
    }

    public void computeIdeal(){
    	ListSet<Node> temp = new ListSet<>();
    	for(Node n : below){
    		temp.add(n);
    		if(n.getIdeal() != null){
    			temp.addAll(n.getIdeal());
    		}
    		
    	}
        ideal = temp;
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

    public int getLevel(){
        return level;
    }

    public void setVisibleObject(String object){
        visibleObjects.getSet().add(object);
    }

    public Set<String> getVisibleObjects(){
        return this.visibleObjects.getSet();
    }

    public void setVisibleAttribute(String attribute){
        this.visibleAttributes.getSet().add(attribute);
    }

    public Set<String> getVisibleAttributes(){
        return this.visibleAttributes.getSet();

    }

    public Label getObjectsLabel(){
        return this.visibleObjects;
    }

    public Label getAttributesLabel(){
        return this.visibleAttributes;
    }

    public void moveSubgraph(boolean b){
        this.moveSubgraph = b;
    }

    public void toggleIdealVisibility(){
        this.isIdealVisibile = !this.isIdealVisibile;
        for(Node n : ideal){
            n.setPartOfAnIdeal(isIdealVisibile);
        }
        if (getParent() != null) {
            getParent().repaint();
        }
    }

    public boolean isPartOfAnIdeal(){
        return this.isIdealVisibile;
    }

    public void setPartOfAnIdeal(boolean b){
        this.isIdealVisibile = b;
    }



}
