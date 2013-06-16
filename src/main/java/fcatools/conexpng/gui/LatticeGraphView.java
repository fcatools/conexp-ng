package fcatools.conexpng.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Set;
import java.util.TreeSet;

import org.apache.batik.dom.GenericDOMImplementation;
import org.apache.batik.dom.svg.SVGDOMImplementation;
import org.apache.batik.svggen.SVGGraphics2D;
import org.apache.batik.swing.JSVGCanvas;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.JPEGTranscoder;
import org.apache.commons.io.IOUtils;
import org.apache.fop.pdf.PDFLink;
import org.apache.fop.svg.PDFDocumentGraphics2D;
import org.apache.fop.svg.PDFGraphics2D;
import org.apache.xmlgraphics.java2d.GraphicContext;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;

import fcatools.conexpng.draw.Edge;
import fcatools.conexpng.draw.Label;
import fcatools.conexpng.draw.LatticeGraph;
import fcatools.conexpng.draw.LatticeGraphNodeMouseMotionListener;
import fcatools.conexpng.draw.Node;

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
		this.removeAll();
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

		// calc which obj/attr has to be shown
		Set<String> usedObj = new TreeSet<>();
		Set<String> usedAttr = new TreeSet<>();
		for (int i = q.size() - 1; i >= 0; i--) {
			Node n = q.get(i);
			for (String s : n.getObjects()) {
				if (!usedObj.contains(s)) {
					n.setVisibleObject(s);
					usedObj.add(s);
				}
			}
		}
		for (int i = 0; i < q.size(); i++) {
			Node n = q.get(i);
			for (String s : n.getAttributes()) {
				if (!usedAttr.contains(s)) {
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
		g.setRenderingHint(
			    RenderingHints.KEY_ANTIALIASING,
			    RenderingHints.VALUE_ANTIALIAS_ON);
			g.setRenderingHint(
			    RenderingHints.KEY_TEXT_ANTIALIASING,
			    RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

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
			
			Set<String> s = n.getVisibleObjects();
			if(!s.isEmpty()){
				//beneath the node			
				this.calcDrawPosition(g, s, true, n);
			}
			s = n.getVisibleAttributes();
			if(!s.isEmpty()){
				//upon the node
				this.calcDrawPosition(g, s, false, n);
			}
		}
	}
	
	private void calcDrawPosition(Graphics2D g, Set<String> elements, boolean areObjects, Node node){
		int x = node.getX();
		int y = node.getY();
		if(areObjects){
			y += 4 * LatticeView.radius;
			for(String s : elements){
				g.setColor(getBackground());
				g.fillRect(x, y - 10, s.length()*7, 10);
				g.setColor(Color.MAGENTA);
				g.drawString(s, x, y);
				y += 15;
			}
		}else{
			y -= LatticeView.radius;
			for(String s : elements){
				g.setColor(getBackground());
				g.fillRect(x, y - 10, s.length()*7, 10);
				g.setColor(Color.GREEN);
				g.drawString(s, x, y);
				y -= 15;
			}
		}
	}

	public void exportLatticeAsPDF() {
		DOMImplementation domImpl = GenericDOMImplementation
				.getDOMImplementation();

		String svgNS = SVGDOMImplementation.SVG_NAMESPACE_URI;
		Document document = domImpl.createDocument(svgNS, "svg", null);

		
		SVGGraphics2D svgGenerator = new SVGGraphics2D(document);
		this.paint(svgGenerator);

		Writer out;
		try { 
           out=new FileWriter(new File("test_batik.svg")); 
           svgGenerator.stream(out, true);
		} catch (IOException ioEx) {
			ioEx.printStackTrace();
		}
		

//		File outputFile = new File("D:/out.pdf");
//		OutputStream out = null;
//        try {
//        	out = new java.io.FileOutputStream(outputFile);
//            out = new java.io.BufferedOutputStream(out);
//            PDFDocumentGraphics2D g2d = new PDFDocumentGraphics2D();
//            g2d.setDeviceDPI(PDFDocumentGraphics2D.NORMAL_PDF_RESOLUTION);
//            g2d.setGraphicContext(new GraphicContext());
//            Dimension pageSize = new Dimension(595, 842); //A4
//            g2d.setupDocument(out, pageSize.width, pageSize.height);
//
//
//            //Works:
//            //g2d.addLink(targetRect, tx, "http://www.apache.org",PDFLink.EXTERNAL);
//            //Doesn't work
//            //g2d.addLink(targetRect, tx, "[/XYZ 0 0 null]", PDFLink.INTERNAL);
//            //Works:
//            g2d.finish();
//        } catch (FileNotFoundException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} finally {
//            IOUtils.closeQuietly(out);
//        }
	}

	public void setLatticeGraph(LatticeGraph g) {
		graph = g;
		init();
	}
	
	public void setMove(boolean change){
		for(Node n: graph.getNodes()){
			n.moveSubgraph(change);
		}
	}
	
}
