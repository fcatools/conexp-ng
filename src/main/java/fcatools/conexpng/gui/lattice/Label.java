package fcatools.conexpng.gui.lattice;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.util.Set;

import javax.swing.JPanel;

public class Label extends JPanel implements LatticeGraphElement {

	/**
     *
     */
	private static final long serialVersionUID = -2090868180188362366L;
	private int x;
	private int y;
	private Set<String> set;
	private String content;
	private Node node;
	private boolean isObjectLabel;
	private static Font font = new Font("Monospaced", Font.PLAIN, 12);

	public Label(Set<String> set, Node node, boolean isObjectLabel) {
		this.set = set;
		this.node = node;
		this.isObjectLabel = isObjectLabel;
		this.setBounds(x, y, 15, 15);

	}

	public void paint(Graphics g) {
		content = elementsToString();
		g.setFont(font);
		FontMetrics fm = g.getFontMetrics();
		Rectangle r = fm.getStringBounds(content, g).getBounds();

		g.setColor(Color.WHITE);
		g.fillRect(r.x + x, r.y + y, r.width, r.height);

		if (isObjectLabel) {
			g.setColor(Color.MAGENTA);
		} else {
			g.setColor(Color.GREEN);
		}
		g.drawString(content, x, y);

		g.setColor(Color.BLACK);
		g.drawRect(r.x + x, r.y + y, r.width, r.height);

		this.setBounds(r.x + x, r.y + y, r.width, r.height);

	}

	public void update(int x, int y, boolean first) {
		int updateX = this.x + x;
		int updateY = this.y + y;

		this.setBounds(updateX, updateY, getBounds().width, getBounds().height);
		this.x = updateX;
		this.y = updateY;

		if (getParent() != null) {
			getParent().repaint();
		}
	}

	public void setXY(int x, int y) {
		this.x = x;
		this.y = y;
		setBounds(x, y, 15, 15);
	}
	
	/**
	 * Set x and y coordiantes with respect to the label type.
	 * @param x
	 * @param y
	 */
	public void setXYWRTLabelType(int x, int y){
		if(isObjectLabel){
			this.x = x;
			this.y = y + 5*LatticeView.radius;
		}else{
			this.x = x;
			this.y = y - LatticeView.radius;
		}
		setBounds(x, y, 15, 15);
	}

	public void setSet(Set<String> set) {
		this.set = set;
	}

	public Set<String> getSet() {
		return set;
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}

	public String elementsToString() {
		String s = "";
		for (String t : set) {
			s = s + t + ", ";
		}
		if (!s.isEmpty())
			s = " " + s.substring(0, s.length() - 2) + " ";
		return s;
	}

	// public void changed() {
	// setBounds(x, y, 30, 20);
	//
	// }
}
