package fcatools.conexpng.gui.lattice;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.net.MalformedURLException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;
import java.util.TreeSet;

import org.apache.batik.dom.GenericDOMImplementation;
import org.apache.batik.dom.svg.SVGDOMImplementation;
import org.apache.batik.svggen.SVGGraphics2D;
import org.apache.batik.swing.JSVGCanvas;
import org.apache.batik.transcoder.Transcoder;
import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.fop.pdf.TempFileStreamCache;
import org.apache.fop.svg.PDFTranscoder;
import org.apache.xpath.axes.ChildIterator;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;

import fcatools.conexpng.Conf;

/*The JSVGCanvas provides a set of build-in interactors that
 * let the users manipulate the displayed document, including ones for zooming,
 * panning and rotating. Interactors catch user input to the JSVGCanvas component
 * and translate them into behaviour.
 */
public class LatticeGraphView extends JSVGCanvas {

    private static final long serialVersionUID = -8623872314193862285L;
    private LatticeGraph graph;
    private Conf state;
    private ArrayList<Node> lastIdeal;
    private boolean idealHighlighting;

    public LatticeGraphView(LatticeGraph graph, Conf state) {
        this.graph = graph;
        this.state = state;
        this.lastIdeal = new ArrayList<Node>();
        this.init();
    }

    private void init() {
        this.removeAll();
        for (Node n : graph.getNodes()) {
            this.add(n);
            n.addMouseMotionListener(new LatticeGraphNodeMouseMotionListener(n));
            n.addMouseListener(new NodeMouseClickListener(n));
        }

        // calc which obj/attr has to be shown
        Set<String> usedObj = new TreeSet<>();
        Set<String> usedAttr = new TreeSet<>();
        Node maxNode = new Node();
        Node minNode;
        if(graph.getNodes().size() == 0){
        	minNode = new Node();
        }else {
        	minNode = graph.getNode(0);
        }
        	
        
        for(Node u : graph.getNodes()){
        	if(u.getIdeal().size() > maxNode.getIdeal().size()){
        		maxNode = u;
        	}
        	else if(u.getIdeal().size() < minNode.getIdeal().size()){
        		minNode = u;
        	}
        }
        
        //queue benutzen um über die below von oben nach unten die Attribute zu setzen.
        Queue<Node> pq = new LinkedList<>();
        pq.add(maxNode);
        while(!pq.isEmpty()){
        	Node n = pq.remove();
        	for(String a : n.getAttributes()){
        		if(!usedAttr.contains(a)){
        			n.setVisibleAttribute(a);
        			usedAttr.add(a);
        		}
        	}
        	for(Node u : n.getBelow()){
        		pq.add(u);
        	}
        }
        
        pq.add(minNode);
        while(!pq.isEmpty()){
        	Node n = pq.remove();
        	for(String o : n.getObjects()){
        		if(!usedObj.contains(o)){
        			n.setVisibleObject(o);
        			usedObj.add(o);
        		}
        	}
        	for(Node u : graph.getNodes()){
        		if(u.getBelow().contains(n)){
        			pq.add(u);
        		}
        	}
        }

    }

    @Override
    public void paint(Graphics g0) {

        super.paint(g0);
        

        // DOMImplementation impl = SVGDOMImplementation.getDOMImplementation();
        // String svgNS = SVGDOMImplementation.SVG_NAMESPACE_URI;
        // SVGDocument doc = (SVGDocument) impl.createDocument(svgNS, "svg",
        // null);

        // SVGGraphics2D g = new SVGGraphics2D(doc);

        Graphics2D g = (Graphics2D) g0;

        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        g.setColor(Color.BLACK);
        int radius = LatticeView.radius;
//		if (graph.getEdges() != null && state.showEdges) {
//			for (Edge e : graph.getEdges()) {
//				g.setColor(Color.BLACK);
//				if (e.getU().isPartOfAnIdeal() && e.getV().isPartOfAnIdeal()
//						&& idealHighlighting) {
//					g.setColor(Color.BLUE);
//				} else if (!(e.getU().isPartOfAnIdeal() && e.getV()
//						.isPartOfAnIdeal()) && idealHighlighting) {
//					g.setColor(Color.LIGHT_GRAY);
//				}
//				g.drawLine(e.getU().getX() + radius, e.getU().getY() + radius,
//						e.getV().getX() + radius, e.getV().getY() + radius);
//			}
//		}
        for(Node n: graph.getNodes()){
            if (state.showEdges) {
                for (Node u : n.getBelow()) {
                    g.setColor(Color.BLACK);
                    if (n.isPartOfAnIdeal() && u.isPartOfAnIdeal()
                            && idealHighlighting) {
                        g.setColor(Color.BLUE);
                    } else if (!(n.isPartOfAnIdeal() && u.isPartOfAnIdeal())
                            && idealHighlighting) {
                        g.setColor(Color.LIGHT_GRAY);
                    }
                    g.drawLine(n.getX() + radius, n.getY() + radius, u.getX()
                            + radius, u.getY() + radius);
                }
            }
        }
        for (Node n : graph.getNodes()) {
            g.setColor(Color.WHITE);
            g.fillOval(n.getX(), n.getY(), radius * 2, radius * 2);
            g.setColor(Color.BLACK);
            g.drawOval(n.getX(), n.getY(), radius * 2, radius * 2);
            if (n.isPartOfAnIdeal() && idealHighlighting) {
                lastIdeal.add(n);
                g.setColor(Color.BLUE);
                g.drawOval(n.getX(), n.getY(), radius * 2, radius * 2);
            } else if ((!n.isPartOfAnIdeal()) && idealHighlighting) {
                g.setColor(Color.LIGHT_GRAY);
                g.fillOval(n.getX(), n.getY(), radius * 2, radius * 2);
            }

            Set<String> s = n.getVisibleObjects();
            if ((!s.isEmpty()) && state.showObjectLabel) {
                // beneath the node
                this.calcDrawPosition(g, s, true, n);
            }
            s = n.getVisibleAttributes();
            if ((!s.isEmpty()) && state.showAttributLabel) {
                // upon the node
                this.calcDrawPosition(g, s, false, n);
            }
        }
        resetHighlighting();
        lastIdeal.clear();

        // Element root = doc.getDocumentElement();
        // g.getRoot(root);
        //
        // this.setSVGDocument(doc);
    }

    private void calcDrawPosition(Graphics2D g, Set<String> elements,
            boolean areObjects, Node node) {
        int x = node.getX();
        int y = node.getY();
        
        if(!node.getVisibleAttributes().isEmpty() && !node.getVisibleObjects().isEmpty()){
        	g.setColor(Color.BLUE);
        	g.fillArc(x, y, LatticeView.radius * 2, LatticeView.radius * 2, 0, 180);
        	g.setColor(Color.BLACK);
        	g.fillArc(x, y, LatticeView.radius * 2, LatticeView.radius * 2, 180, 180);
            g.drawOval(x, y, LatticeView.radius * 2, LatticeView.radius * 2);
        	
        }else if(!node.getVisibleObjects().isEmpty()){
        	g.setColor(Color.BLACK);
        	g.fillOval(x, y, LatticeView.radius * 2, LatticeView.radius * 2);
        	g.setColor(Color.WHITE);
            g.fillArc(x, y, LatticeView.radius * 2, LatticeView.radius * 2, 0, 180);
            g.setColor(Color.BLACK);
            g.drawOval(x, y, LatticeView.radius * 2, LatticeView.radius * 2);
        }else {
        	g.setColor(Color.BLUE);
        	g.fillOval(x, y, LatticeView.radius * 2, LatticeView.radius * 2);
        	g.setColor(Color.WHITE);
            g.fillArc(x, y, LatticeView.radius * 2, LatticeView.radius * 2, 180, 180);
            g.setColor(Color.BLACK);
            g.drawOval(x, y, LatticeView.radius * 2, LatticeView.radius * 2);
        }
        
        
        if (areObjects) {
            y += 4 * LatticeView.radius;
            for (String s : elements) {
                g.setColor(getBackground());
                g.fillRect(x, y - 10, s.length() * 7, 10);
                g.setColor(Color.MAGENTA);
                g.drawString(s, x, y);
                y += 15;
            }
        } else {
            y -= LatticeView.radius;
            for (String s : elements) {
                g.setColor(getBackground());
                g.fillRect(x, y - 10, s.length() * 7, 10);
                g.setColor(Color.GREEN);
                g.drawString(s, x, y);
                y -= 15;
            }
        }
    }

    public void exportLatticeAsPDF(String path) {
        DOMImplementation domImpl = GenericDOMImplementation
                .getDOMImplementation();

        String svgNS = SVGDOMImplementation.SVG_NAMESPACE_URI;
        Document document = domImpl.createDocument(svgNS, "svg", null);

        SVGGraphics2D svgGenerator = new SVGGraphics2D(document);
        this.paint(svgGenerator);

        Writer out;
        try {
            out = new FileWriter(new File("test_batik.svg"));
            svgGenerator.stream(out, true);
        } catch (IOException ioEx) {
            ioEx.printStackTrace();
        }

        String svg_URI_input;
        try {
            svg_URI_input = Paths.get("test_batik.svg").toUri().toURL()
                    .toString();
            TranscoderInput input_svg_image = new TranscoderInput(svg_URI_input);
            OutputStream pdf_ostream = new FileOutputStream("test.pdf");
            TranscoderOutput output_pdf_file = new TranscoderOutput(pdf_ostream);
            Transcoder transcoder = new PDFTranscoder();
            transcoder.transcode(input_svg_image, output_pdf_file);
            pdf_ostream.flush();
            pdf_ostream.close();

        } catch (MalformedURLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (TranscoderException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        // File outputFile = new File("out.pdf");
        // OutputStream out = null;
        // try {
        // out = new java.io.FileOutputStream(outputFile);
        // out = new java.io.BufferedOutputStream(out);
        // PDFDocumentGraphics2D g2d = new PDFDocumentGraphics2D();
        // g2d.setDeviceDPI(PDFDocumentGraphics2D.NORMAL_PDF_RESOLUTION);
        // g2d.setGraphicContext(new GraphicContext());
        // Dimension pageSize = new Dimension(595, 842); //A4
        // g2d.setupDocument(out, pageSize.width, pageSize.height);
        //
        //
        // //Works:
        // //g2d.addLink(targetRect, tx,
        // "http://www.apache.org",PDFLink.EXTERNAL);
        // //Doesn't work
        // //g2d.addLink(targetRect, tx, "[/XYZ 0 0 null]", PDFLink.INTERNAL);
        // //Works:
        // g2d.finish();
        // } catch (FileNotFoundException e) {
        // // TODO Auto-generated catch block
        // e.printStackTrace();
        // } catch (IOException e) {
        // // TODO Auto-generated catch block
        // e.printStackTrace();
        // } finally {
        // IOUtils.closeQuietly(out);
        // }
    }

    public void setLatticeGraph(LatticeGraph g) {
        graph = g;
        init();
    }

    private void resetHighlighting() {
        for (Node n : lastIdeal) {
            n.setPartOfAnIdeal(false);
        }
    }

    public void setMove(boolean change) {
        for (Node n : graph.getNodes()) {
            n.moveSubgraph(change);
        }
    }

    public void idealHighlighting(boolean change) {
        this.idealHighlighting = change;
    }

}
