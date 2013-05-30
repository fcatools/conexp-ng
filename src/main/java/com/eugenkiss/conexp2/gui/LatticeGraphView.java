package com.eugenkiss.conexp2.gui;

import java.awt.Graphics;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;

import javax.swing.JPanel;

import org.apache.batik.swing.JSVGCanvas;
import org.apache.batik.swing.gvt.Interactor;

import com.eugenkiss.conexp2.draw.LatticeGraph;


/*The JSVGCanvas provides a set of build-in interactors that 
 * let the users manipulate the displayed document, including ones for zooming, 
 * panning and rotating. Interactors catch user input to the JSVGCanvas component 
 * and translate them into behaviour.
 */
public class LatticeGraphView extends JPanel implements Interactor{
	
	private static final long serialVersionUID = -8623872314193862285L;
	private LatticeGraph graph;
	private JSVGCanvas svgCanvas;

	public LatticeGraphView(LatticeGraph graph){
		this.graph = graph;	
		this.svgCanvas = new JSVGCanvas();
		this.add(svgCanvas);
		svgCanvas.getInteractors().add(this);
	}
	
	
	@Override
	public void paint(Graphics g) {
		for(int i = 0; i < graph.getNodes().size(); i++){
			g.drawOval(graph.getNode(i).getX(), graph.getNode(i).getY(), 10, 10);
		}
		super.paint(g);
	}


	@Override
	public void keyPressed(KeyEvent arg0) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void keyReleased(KeyEvent arg0) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void keyTyped(KeyEvent arg0) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void mouseClicked(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void mouseEntered(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void mouseExited(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void mousePressed(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void mouseReleased(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void mouseDragged(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void mouseMoved(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public boolean endInteraction() {
		// TODO Auto-generated method stub
		return false;
	}


	@Override
	public boolean startInteraction(InputEvent arg0) {
		// TODO Auto-generated method stub
		return false;
	}
	
	
	
}
