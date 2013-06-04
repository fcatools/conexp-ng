package com.eugenkiss.conexp2.draw;

import java.awt.Color;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.batik.swing.JSVGCanvas;

public class Node extends JSVGCanvas {

    /**
     *
     */
    private static final long serialVersionUID = 4253192979583459657L;
    private List<String> objects;
    private List<String> attributes;
    private int x;
    private int y;
	private List<Node> below;
	private boolean moveSubgraph;

    /**
     *
     * @param objects
     * @param attributes
     * @param x
     * @param y
     */
    public Node(List<String> objects, List<String> attributes, int x, int y) {
        this.objects = objects;
        this.attributes = attributes;
        this.x = x;
        this.y = y;
        this.setBounds(x, y, 15, 15);
        this.below = new ArrayList<>();
        //this.setBackground(Color.GRAY);

    }

    /**
     *
     */
    public Node() {
        this.objects = new ArrayList<>();
        this.attributes = new ArrayList<>();
        this.x = 0;
        this.y = 0;
        this.setBounds(x, y, 10, 10);
        this.setBackground(Color.RED);
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
    public void addObject(Set<String> extent) {
        objects.addAll(extent);
    }

    /**
     *
     * @param set
     */
    public void addAttribut(Set<String> set) {
        attributes.addAll(set);
    }

    public void addBelowNode(Node n){
    	below.add(n);
    }
    
    public void update(int x, int y) {
        int updateX = this.x + x;
        int updateY = this.y + y;
        if(moveSubgraph){
        	for(Node n : below){
            	n.update(x, y);
            }
        }
        
        this.setBounds(updateX, updateY, 10, 10);
        this.x = updateX;
        this.y = updateY;

        getParent().repaint();
    }

}
