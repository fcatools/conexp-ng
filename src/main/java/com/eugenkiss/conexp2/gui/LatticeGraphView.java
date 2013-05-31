package com.eugenkiss.conexp2.gui;

import java.awt.Graphics;

import org.apache.batik.swing.JSVGCanvas;

import com.eugenkiss.conexp2.draw.LatticeGraph;
import com.eugenkiss.conexp2.draw.Node;


/*The JSVGCanvas provides a set of build-in interactors that 
 * let the users manipulate the displayed document, including ones for zooming, 
 * panning and rotating. Interactors catch user input to the JSVGCanvas component 
 * and translate them into behaviour.
 */
public class LatticeGraphView extends JSVGCanvas{
	
	private static final long serialVersionUID = -8623872314193862285L;
	private LatticeGraph graph;
	

	public LatticeGraphView(LatticeGraph graph){
		this.graph = graph;
		for(Node n : graph.getNodes()){
			this.add(n);
		}
		
	}
	
	@Override
	public void paintComponents(Graphics g) {
		// TODO Auto-generated method stub
		super.paintComponents(g);
	}
}
