package com.eugenkiss.conexp2.draw;

public class Node {

	private String[] objects;
	private String[] attributes;
	private int x;
	private int y;

	/**
	 * 
	 * @param objects
	 * @param attributes
	 * @param x
	 * @param y
	 */
	public Node(String[] objects, String[] attributes, int x, int y) {
		this.objects = objects;
		this.attributes = attributes;
		this.x = x;
		this.y = y;
	}

	public int getX() {
		return x;
	}

	public void setX(int x) {
		this.x = x;
	}

	public int getY() {
		return y;
	}

	public void setY(int y) {
		this.y = y;
	}

	public String[] getObjects() {
		return objects;
	}

	public void setObjects(String[] objects) {
		this.objects = objects;
	}

	public String[] getAttributes() {
		return attributes;
	}

	public void setAttributes(String[] attributes) {
		this.attributes = attributes;
	}

}
