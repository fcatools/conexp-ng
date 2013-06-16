package fcatools.conexpng.gui.lattice;

import java.awt.Graphics;
import java.util.Set;

import javax.swing.JPanel;

public class Label extends JPanel implements LatticeGraphElement{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -2090868180188362366L;
	private int x;
	private int y;
	private Set<String> set;
	
	public Label(Set<String> set){
		this.set = set;
		this.setBounds(x, y, 15, 15);
	}
	
	public void paint(Graphics g) {
	}

	public void update(int x, int y) {
		int updateX = this.x + x;
		int updateY = this.y + y;

		this.setBounds(updateX, updateY, 15, 15);
		this.x = updateX;
		this.y = updateY;

		if (getParent() != null) {
			getParent().repaint();
		}
	}
	
	public void setSet(Set<String> set){
		this.set = set;
	}
	
	public Set<String> getSet(){
		return set;
	}
	
	public int getX(){
		return x;
	}
	
	public int getY(){
		return y;
	}
	
	public String elementsToString(){
		String s = "";
		for(String t : set){
			s = s + t + ", ";
		}
		return s;
	}

	public void changed() {
		setBounds(x, y, 30, 20);
		
	}
 
}
