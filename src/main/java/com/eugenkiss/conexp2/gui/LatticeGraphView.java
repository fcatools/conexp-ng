package com.eugenkiss.conexp2.gui;

import java.awt.Color;
import java.awt.Graphics;
import org.apache.batik.swing.JSVGCanvas;

import com.eugenkiss.conexp2.draw.Edge;
import com.eugenkiss.conexp2.draw.LatticeGraph;
import com.eugenkiss.conexp2.draw.LatticeGraphNodeMouseListener;
import com.eugenkiss.conexp2.draw.Node;

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
        for (Node n : graph.getNodes()) {
            this.add(n);
            n.addMouseListener(new LatticeGraphNodeMouseListener(n));
        }

    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, getWidth(), getHeight());
       // g.drawOval(300, 300, 50, 50);
        g.setColor(Color.BLACK);
        int radius=LatticeView.radius;
        if (graph.getEdges() != null) {
            for (Edge e : graph.getEdges()) {
                g.drawLine(e.getU().getX()+radius, e.getU().getY()+radius, e.getV().getX()+radius, e.getV().getY()+radius);
            }
            g.setColor(Color.RED);
            for (Node n : graph.getNodes()) {
                g.fillOval(n.getX(), n.getY(), radius*2, radius*2);
            }
        }
    }

    public void exportLatticeAsSVG() {

    }
}
