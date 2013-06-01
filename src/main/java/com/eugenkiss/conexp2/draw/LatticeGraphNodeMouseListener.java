package com.eugenkiss.conexp2.draw;

import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import org.apache.batik.swing.gvt.AbstractPanInteractor;
import org.apache.batik.swing.gvt.Interactor;
import org.w3c.dom.views.AbstractView;

import com.eugenkiss.conexp2.gui.LatticeGraphView;

public class LatticeGraphNodeMouseListener implements MouseListener {

	private Node n;
	private int x;
	private int y;

	public LatticeGraphNodeMouseListener(Node n){
		this.n = n;
	}
	
	@Override
	public void mouseClicked(MouseEvent e) {
		
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseExited(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mousePressed(MouseEvent e) {
		x = e.getX();
		y = e.getY();
		
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		x = x - e.getX();
		y = y - e.getY();
		n.update(x,y);
		System.out.println(x+", "+y);
	}
	
	

}
