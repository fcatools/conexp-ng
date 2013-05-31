package com.eugenkiss.conexp2.draw;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;

public class Node extends JPanel{

	/**
	 * 
	 */
	private static final long serialVersionUID = 4253192979583459657L;
	private List<String> objects;
    private List<String> attributes;
    private int x;
    private int y;

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
        this.setBounds(x, y, 10, 10);
        this.setBackground(Color.RED);

    }

    /**
     * 
     */
    public Node(){
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
     * @param s
     */
    public void addObject(String s){
        objects.add(s);
    }

    /**
     * 
     * @param s
     */
    public void addAttribut(String s){
        attributes.add(s);
    }

}
