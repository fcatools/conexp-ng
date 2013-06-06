import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFrame;

import org.apache.batik.dom.svg.SVGDOMImplementation;
import org.apache.batik.svggen.SVGGraphics2D;
import org.apache.batik.swing.JSVGCanvas;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Element;
import org.w3c.dom.svg.SVGDocument;

/**
 * In diesem Fenster wird ein eine SVG-Grafik mittels Batik dargestellt.
 *
 * Per Maus wird ein Begrenzungsrechtecks einer Ellipse gezeichnet (Maustaste
 * drücken: Startpunkt, Maustaste loslassen: Endpunkt), beim Loslassen wird der
 * Kreis gezeichnet.
 *
 * Das ganze geht in ein SVG-Dokument ein, das auf einem SVG-Canvas gezeichnet
 * wird. Die SVG-Datei läßt sich auch speichern.
 *
 * @author Wolfgang Knauf
 *
 */
public class BatikExample extends JFrame {
    /**
     * SerialVersionUID (damit Eclipse schweigt)
     *
     */
    private static final long serialVersionUID = 1L;

    /**
     * Liste von Rechtecken, die die Rahmen der zu zeichnenden Kreise
     * beschreiben
     */
    private List<Rectangle> listRectangles = new ArrayList<Rectangle>();

    /** In diesem Feld wird die SVG-Grafik gezeichnet. */
    private JSVGCanvas canvas = null;

    /**
     * Konstruktor, initialisiert das Fenster.
     *
     */
    public BatikExample() {
        // Canvase erzeugen:
        this.canvas = new JSVGCanvas();
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        // GridBayLayout definieren für volle Fenstergröße:
        GridBagLayout layout = new GridBagLayout();
        this.getContentPane().setLayout(layout);
        // Einen Speichern-Button unten zufügen:
        JButton buttonSave = new JButton("Speichern");
        buttonSave.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                // Hier wird gespeichert:
                drawPoints(false);
            }

        });

        GridBagConstraints gridBagConstraintsButton = new GridBagConstraints(0,
                0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER,
                GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0);
        this.getContentPane().add(buttonSave, gridBagConstraintsButton);

        GridBagConstraints gridBagConstraintsSVG = new GridBagConstraints(0, 1,
                1, 1, 1.0, 1.0, GridBagConstraints.CENTER,
                GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0);
        this.getContentPane().add(this.canvas, gridBagConstraintsSVG);

        // Click-listener zufügen (als anonymer MouseListener bzw. dessen
        // implementierende Dummy-Klasse "MouseAdapter"):
        canvas.addMouseListener(new MouseAdapter() {
            /**
             * Startpunkt (X-Koordinate) des aktuellen Rechtecks. Beim Mausdown
             * wird der Startpunkt definiert und hier gespeichert. Beim
             * Maus-loslassen wird das Rechteck fertiggestellt.
             */
            private int intStartXLastRectangle = -1;

            /**
             * Startpunkt (Y-Koordinate) des aktuellen Rechtecks. Beim Mausdown
             * wird der Startpunkt definiert und hier gespeichert. Beim
             * Maus-loslassen wird das Rechteck fertiggestellt.
             */
            private int intStartYLastRectangle = -1;

            /**
             * Die Maus wird gedrückt: Startpunkt des Rechtecks speichern.
             *
             */
            @Override
            public void mousePressed(MouseEvent e) {
                this.intStartXLastRectangle = (int) e.getX();
                this.intStartYLastRectangle = (int) e.getY();

                System.out.println("Click an " + e.getX() + "/" + e.getY());
            }

            /**
             * Maus wird losgelassen: Rechteck fertigzeichnen.
             *
             * @param e
             *            Mauskoordinaten etc.
             */
            @Override
            public void mouseReleased(MouseEvent e) {
                System.out.println("mouseReleased an " + e.getX() + "/"
                        + e.getY());

                // Juhu, das Rechteck fertigmachen!
                // Breite = "Endpunkt - Startpunkt".
                int intX = (int) e.getX();
                int intY = (int) e.getY();
                // Beachten: eventuell wird die Maus nach links gezogen => in
                // diesem Fall Weite umdrehen.
                int intWidth = intX - this.intStartXLastRectangle;
                if (intX < this.intStartXLastRectangle) {
                    intWidth = -1 * intWidth;
                    // Swap x1/x2:
                    int intTemp = intX;
                    intX = this.intStartXLastRectangle;
                    this.intStartXLastRectangle = intTemp;
                }
                int intHeight = intY - this.intStartYLastRectangle;
                if (intY < this.intStartYLastRectangle) {
                    intHeight = -1 * intHeight;
                    // Swap y1/y2:
                    int intTemp = intY;
                    intY = this.intStartYLastRectangle;
                    this.intStartYLastRectangle = intTemp;
                }

                // Begrenzungsrechteck zur Liste zufügen:
                Rectangle rectAktuell = new Rectangle(
                        this.intStartXLastRectangle,
                        this.intStartYLastRectangle, intWidth, intHeight);
                listRectangles.add(rectAktuell);

                // Zeichnen:
                drawPoints(true);
            }
        });

    }

    /**
     * Zeichnet das Ding (auf Bildschim oder in Datei).
     *
     * @param bolDrawOrSave
     *            TRUE: echtes Zeichnen auf den Bildschirm. FALSE: Speichern in
     *            Datei.
     */
    private void drawPoints(boolean bolDrawOrSave) {
        // Create an SVG document.
        DOMImplementation impl = SVGDOMImplementation.getDOMImplementation();
        String svgNS = SVGDOMImplementation.SVG_NAMESPACE_URI;
        SVGDocument doc = (SVGDocument) impl.createDocument(svgNS, "svg", null);

        // Create a converter for this document.
        SVGGraphics2D g = new SVGGraphics2D(doc);

        // Über die Liste der Rechtecke laufen und jeweils eine Ellipse
        // zeichnen:
        for (Iterator<Rectangle> iterator = this.listRectangles.iterator(); iterator
                .hasNext();) {
            Rectangle rectCurrent = iterator.next();

            Shape circle = new Ellipse2D.Double(rectCurrent.x, rectCurrent.y,
                    rectCurrent.width, rectCurrent.height);
            g.setPaint(Color.red);
            g.draw(circle);
        }

        // Was soll passieren? Zeichnen oder speichern?
        // Beides zusammen geht nicht, dann kommt eine von beiden Operationen
        // leer an.
        if (bolDrawOrSave == true) {
            // Echtes Zeichnen:
            // Populate the document root with the generated SVG content.
            Element root = doc.getDocumentElement();
            g.getRoot(root);

            this.canvas.setSVGDocument(doc);
        } else {
            // Speichern in Datei:
            try {
                FileWriter fileWriter = new FileWriter("test.svg");
                g.stream(fileWriter, false);
            } catch (IOException ioEx) {
                // Bösepfui, NIE nachmachen!
                ioEx.printStackTrace();
            }
        }
    }

    /**
     * Main-Methode, zeigt das Fenster an.
     *
     * @param args
     *            Kommandozeilenparameter, hier ungenutzt.
     */
    public static void main(String[] args) {
        BatikExample form = new BatikExample();

        form.pack();
        form.setVisible(true);
    }
}