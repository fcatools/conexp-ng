package fcatools.conexpng.gui.lattice;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Toolkit;
import java.awt.geom.Rectangle2D;
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
	private String content;
	
	public Label(Set<String> set){
		this.set = set;
		this.setBounds(x, y, 15, 15);
		this.setFont(new Font("Tahoma",Font.PLAIN,15));
   
	}
	
	public void paint(Graphics g) {
		content = elementsToString();
        FontMetrics metrics = Toolkit.getDefaultToolkit().getFontMetrics(this.getFont());

        int i = metrics.stringWidth(content);
		g.setColor(Color.WHITE);
		g.fillRect(x, y, i, 15);
		g.setColor(Color.BLACK);
		g.drawRect(x, y, i, 15);
		g.drawString(content, x + 5, (int) (y + 12) );
		this.setBounds(x, y, i, 15);
		
	}

	public void update(int x, int y, boolean first) {
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
			s = s + t + ", " + "\n";
		}
		s = s.substring(0, s.length()-2);
		return s;
	}

	public void changed() {
		setBounds(x, y, 30, 20);
		
	}
 
}
