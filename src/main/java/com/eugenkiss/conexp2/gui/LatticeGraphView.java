package com.eugenkiss.conexp2.gui;

import com.eugenkiss.conexp2.draw.Edge;
import com.eugenkiss.conexp2.draw.LatticeGraph;
import com.eugenkiss.conexp2.draw.LatticeGraphNodeMouseMotionListener;
import com.eugenkiss.conexp2.draw.Node;
import org.apache.batik.swing.JSVGCanvas;

import java.awt.*;
import java.util.ArrayList;
import java.util.Queue;
import java.util.Set;
import java.util.TreeSet;

/*The JSVGCanvas provides a set of build-in interactors that
 * let the users manipulate the displayed document, including ones for zooming,
 * panning and rotating. Interactors catch user input to the JSVGCanvas component
 * and translate them into behaviour.
 */
public class LatticeGraphView extends JSVGCanvas {

	private static final long serialVersionUID = -8623872314193862285L;
	private LatticeGraph graph;

	public LatticeGraphView(LatticeGraph graph) {
		this.graph = graph;
		this.init();
	}

	private void init() {
		ArrayList<Node> q = new ArrayList<>();
		for (Node n : graph.getNodes()) {
			this.add(n);
			n.addMouseMotionListener(new LatticeGraphNodeMouseMotionListener(n));

			// topological order
			if (q.size() == 0) {
				q.add(n);
			}
			for (int i = 0; i < q.size(); i++) {
				if (q.get(i).getLevel() > n.getLevel()) {
					q.add(i, n);
					break;
				}
			}
		}
		
		//calc which obj/attr has to be shown
		Set<String> usedObj = new TreeSet<>();
		Set<String> usedAttr = new TreeSet<>();
		for(int i = q.size()-1; i>= 0; i--){
			Node n = q.get(i);
			for(String s : n.getObjects()){
				if(!usedObj.contains(s)){
					n.setVisibleObject(s);
					usedObj.add(s);
				}
			}
		}
		for(int i = 0; i < q.size(); i++){
			Node n = q.get(i);
			for(String s : n.getAttributes()){
				if(!usedAttr.contains(s)){
					n.setVisibleAttribute(s);
					usedAttr.add(s);
				}
			}
		}

	}

	@Override
	public void paint(Graphics g0) {
		Graphics2D g = (Graphics2D) g0;
		super.paint(g);
		g.addRenderingHints(new RenderingHints(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON));
		g.setRenderingHint(RenderingHints.KEY_RENDERING,
				RenderingHints.VALUE_RENDER_QUALITY);
		g.setColor(Color.WHITE);
		g.fillRect(0, 0, getWidth(), getHeight());
		// g.drawOval(300, 300, 50, 50);
		g.setColor(Color.BLACK);
		int radius = LatticeView.radius;
		if (graph.getEdges() != null) {
			for (Edge e : graph.getEdges()) {
				g.drawLine(e.getU().getX() + radius, e.getU().getY() + radius,
						e.getV().getX() + radius, e.getV().getY() + radius);
			}
		}
		for (Node n : graph.getNodes()) {
			g.setColor(Color.BLACK);
			g.fillOval(n.getX(), n.getY(), radius * 2, radius * 2);

			g.setColor(Color.ORANGE);
			if (!n.getVisibleObjects().isEmpty()) {
				g.drawString(n.getVisibleObjects().toString(), n.getX() + radius * 2,
						n.getY());
			}
			
			g.setColor(Color.GREEN);
			if(!n.getVisibleAttributes().isEmpty()){
				g.drawString(n.getVisibleAttributes().toString(), n.getX() - radius * 2,
						n.getY());
			}
		}
	}

	public void exportLatticeAsSVG() {

	}

	public void setLatticeGraph(LatticeGraph g) {
		graph = g;
	}
}
