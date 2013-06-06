package com.eugenkiss.conexp2.draw;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.JPanel;

public class Node extends JPanel {

	/**
     *
     */
	private static final long serialVersionUID = 4253192979583459657L;
	private Set<String> objects;
	private Set<String> attributes;
	private int x;
	private int y;
	private List<Node> below;
	private boolean moveSubgraph;

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
		this.x = 0;
		this.y = 0;
		this.setBounds(x, y, 10, 10);
		this.setBackground(Color.RED);
		this.below = new ArrayList<>();
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

	public void update(int x, int y) {
		int updateX = this.x + x;
		int updateY = this.y + y;
		if (moveSubgraph) {
			for (Node n : below) {
				n.update(x, y);
			}
		}

		this.setBounds(updateX, updateY, 15, 15);
		this.x = updateX;
		this.y = updateY;

		if (getParent() != null) {
			getParent().repaint();
		}
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

}
