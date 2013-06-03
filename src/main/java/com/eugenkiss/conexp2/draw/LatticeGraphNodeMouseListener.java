package com.eugenkiss.conexp2.draw;

import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;

public class LatticeGraphNodeMouseListener implements MouseMotionListener {

    private Node n;
    
    public LatticeGraphNodeMouseListener(Node n){
    	this.n = n;
    }
    
	@Override
	public void mouseDragged(MouseEvent e) {
		int newX = e.getX();
		int newY = e.getY();
        n.update(newX, newY);
	}
	
	@Override
	public void mouseMoved(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}
    
    


}
