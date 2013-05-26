package com.eugenkiss.conexp2.draw;

public class Edge {

	private Node u;
	private Node v;

	/**
	 * An undirected edge connecting the nodes u and v.
	 * @param u
	 * @param v
	 */
	public Edge(Node u, Node v) {
		this.u = u;
		this.v = v;
	}
	
	public Node getU() {
		return u;
	}

	public void setU(Node u) {
		this.u = u;
	}

	public Node getV() {
		return v;
	}

	public void setV(Node v) {
		this.v = v;
	}
	
	

}
