package com.eugenkiss.conexp2.draw;

import java.util.ArrayList;
import java.util.List;

/**
 * 
 * @author Jan
 *
 */
public class LatticeGraph {
	
	private List<Node> nodes;
	private List<Edge> edges;
	
	/**
	 * 
	 */
	public LatticeGraph(){
		this.nodes = new ArrayList<>();
		this.edges = new ArrayList<>();
	}
	
	/**
	 * 
	 * @param nodes
	 * @param edges
	 */
	public LatticeGraph(List<Node> nodes, List<Edge> edges){
		this.nodes = nodes;
		this.edges = edges;
	}
	
	/**
	 * 
	 * @param i
	 * @return
	 */
	public Node getNode(int i){
		return nodes.get(i);
	}

	/**
	 * 
	 * @return
	 */
	public List<Node> getNodes() {
		return nodes;
	}

	/**
	 * 
	 * @param nodes
	 */
	public void setNodes(List<Node> nodes) {
		this.nodes = nodes;
	}
	
	/**
	 * 
	 * @param i
	 * @return
	 */
	public Edge getEdge(int i){
		return edges.get(i);
	}

	/**
	 * 
	 * @return
	 */
	public List<Edge> getEdges() {
		return edges;
	}

	/**
	 * 
	 * @param edges
	 */
	public void setEdges(List<Edge> edges) {
		this.edges = edges;
	}
	
	

}
