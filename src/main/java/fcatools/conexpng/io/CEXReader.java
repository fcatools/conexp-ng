package fcatools.conexpng.io;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import de.tudresden.inf.tcs.fcaapi.Concept;
import de.tudresden.inf.tcs.fcaapi.FCAImplication;
import de.tudresden.inf.tcs.fcaapi.exception.IllegalObjectException;
import de.tudresden.inf.tcs.fcalib.FullObject;
import de.tudresden.inf.tcs.fcalib.Implication;
import de.tudresden.inf.tcs.fcalib.utils.ListSet;
import fcatools.conexpng.Conf;
import fcatools.conexpng.gui.lattice.LatticeGraph;
import fcatools.conexpng.gui.lattice.Node;
import fcatools.conexpng.model.AssociationRule;
import fcatools.conexpng.model.FormalContext;
import fcatools.conexpng.model.LatticeConcept;

public class CEXReader {

    private FormalContext context;
    private LatticeGraph lattice;
    private Set<AssociationRule> associations;
    private Set<FCAImplication<String>> implications;
    private Set<Concept<String, FullObject<String, String>>> concepts;

    public CEXReader(Conf state, String path) throws XMLStreamException, IllegalObjectException, IOException,
            NumberFormatException, IllegalArgumentException, IllegalAccessException, NoSuchFieldException,
            SecurityException {
        concepts = new ListSet<>();
        associations = new TreeSet<>();
        implications = new HashSet<>();
        InputStream in = null;

        in = new FileInputStream(path);
        XMLInputFactory factory = XMLInputFactory.newInstance();
        XMLEventReader parser = null;

        parser = factory.createXMLEventReader(in);

        while (parser.hasNext()) {
            XMLEvent event = null;

            event = parser.nextEvent();

            switch (event.getEventType()) {
            case XMLStreamConstants.START_DOCUMENT:
                context = new FormalContext();
                break;
            case XMLStreamConstants.END_DOCUMENT:

                parser.close();

                break;
            case XMLStreamConstants.START_ELEMENT:
                StartElement element = event.asStartElement();
                if (name(element, "Attributes"))
                    addAttributes(parser);
                if (name(element, "Objects"))
                    addObjects(parser);
                if (name(element, "Lattice"))
                    addLattice(parser);
                if (name(element, "Implications"))
                    addImplications(parser);
                if (name(element, "Associations"))
                    addAssociations(parser);

                break;
            default:
                break;
            }
        }
        parser.close();
        if (lattice == null) {
            lattice = new LatticeGraph();
        }
        state.concepts = concepts;
        state.associations = associations;
        state.implications = implications;
        state.lattice = lattice;
        state.context = context;
        state.setNewFile(path);
    }

    private void addImplications(XMLEventReader parser) throws XMLStreamException, IOException {
        while (parser.hasNext()) {
            XMLEvent event = parser.nextEvent();
            switch (event.getEventType()) {
            case XMLStreamConstants.START_ELEMENT:
                StartElement element = event.asStartElement();
                if (element.getName().toString().equals("Implication")) {

                    Set<String> premise = new TreeSet<>();
                    while (!((event = parser.nextEvent()).isEndElement() && event.asEndElement().getName().toString()
                            .equals("Premise"))) {
                        if (event.isStartElement()
                                && event.asStartElement().getName().toString().equals("HasAttribute")) {
                            int i = Integer.parseInt(event.asStartElement()
                                    .getAttributeByName(new QName("AttributeIdentifier")).getValue());
                            premise.add(context.getAttributeAtIndex(i));
                        }
                    }
                    Set<String> conc = new TreeSet<>();
                    while (!((event = parser.nextEvent()).isEndElement() && event.asEndElement().getName().toString()
                            .equals("Conclusion"))) {
                        if (event.isStartElement()
                                && event.asStartElement().getName().toString().equals("HasAttribute")) {
                            int i = Integer.parseInt(event.asStartElement()
                                    .getAttributeByName(new QName("AttributeIdentifier")).getValue());
                            conc.add(context.getAttributeAtIndex(i));
                        }
                    }

                    implications.add(new Implication<String>(premise, conc));
                }
                break;
            case XMLStreamConstants.END_ELEMENT:
                if (event.asEndElement().getName().toString().equals("Implications"))
                    return;
            }
        }
    }

    private void addAssociations(XMLEventReader parser) throws XMLStreamException, IOException {
        while (parser.hasNext()) {
            XMLEvent event = parser.nextEvent();
            switch (event.getEventType()) {
            case XMLStreamConstants.START_ELEMENT:
                StartElement element = event.asStartElement();
                if (element.getName().toString().equals("Association")) {

                    double sup = 0;
                    double conf = 0;
                    sup = Double.parseDouble(element.getAttributeByName(new QName("Support")).getValue());
                    conf = Double.parseDouble(element.getAttributeByName(new QName("Confidence")).getValue());

                    Set<String> premise = new TreeSet<>();
                    while (!((event = parser.nextEvent()).isEndElement() && event.asEndElement().getName().toString()
                            .equals("Premise"))) {
                        if (event.isStartElement()
                                && event.asStartElement().getName().toString().equals("HasAttribute")) {
                            int i = Integer.parseInt(event.asStartElement()
                                    .getAttributeByName(new QName("AttributeIdentifier")).getValue());
                            premise.add(context.getAttributeAtIndex(i));
                        }
                    }
                    Set<String> cons = new TreeSet<>();
                    while (!((event = parser.nextEvent()).isEndElement() && event.asEndElement().getName().toString()
                            .equals("Consequent"))) {
                        if (event.isStartElement()
                                && event.asStartElement().getName().toString().equals("HasAttribute")) {
                            int i = Integer.parseInt(event.asStartElement()
                                    .getAttributeByName(new QName("AttributeIdentifier")).getValue());
                            cons.add(context.getAttributeAtIndex(i));
                        }
                    }

                    associations.add(new AssociationRule(premise, cons, sup, conf));
                }
                break;
            case XMLStreamConstants.END_ELEMENT:
                if (event.asEndElement().getName().toString().equals("Associations"))
                    return;
            }
        }
    }

    private void addLattice(XMLEventReader parser) throws XMLStreamException {
        lattice = new LatticeGraph();
        while (parser.hasNext()) {
            XMLEvent event = parser.nextEvent();
            switch (event.getEventType()) {
            case XMLStreamConstants.START_ELEMENT: {
                StartElement element = event.asStartElement();
                if (name(element, "AttributeMask")) {
                    for (String s : context.getAttributes()) {
                        context.dontConsiderAttribute(s);
                    }
                    event = parser.nextEvent();
                    while (!(event.isEndElement() && name(event.asEndElement(), "AttributeMask"))) {
                        if (event.isStartElement()) {
                            int i = Integer.parseInt(event.asStartElement()
                                    .getAttributeByName(new QName("AttributeIdentifier")).getValue());
                            context.considerAttribute(context.getAttributeAtIndex(i));
                        }
                        event = parser.nextEvent();
                    }
                }
                if (name(element, "ObjectMask")) {
                    for (FullObject<String, String> o : context.getObjects()) {
                        context.dontConsiderObject(o);
                    }
                    event = parser.nextEvent();
                    while (!(event.isEndElement() && name(event.asEndElement(), "ObjectMask"))) {
                        if (event.isStartElement()) {
                            int i = Integer.parseInt(event.asStartElement()
                                    .getAttributeByName(new QName("AttributeIdentifier")).getValue());
                            context.considerObject(context.getObjectAtIndex(i));
                        }
                        event = parser.nextEvent();
                    }
                }
                if (name(element, "ConceptFigures")) {
                    getNodes(parser);
                }
                if (name(element, "UpConceptLabels")) {
                    getAttributeLabels(parser);
                }
                if (name(element, "ConceptLabels")) {
                    getObjectLabels(parser);
                }
                break;
            }
            case XMLStreamConstants.END_ELEMENT:
                if (event.asEndElement().getName().toString().equals("Lattice"))
                    return;
            }
        }

    }

    private void getObjectLabels(XMLEventReader parser) throws XMLStreamException {
        double x = 0, y = 0;
        double minx = Double.MAX_VALUE, miny = Double.MAX_VALUE;
        while (parser.hasNext()) {
            XMLEvent event = parser.nextEvent();
            switch (event.getEventType()) {
            case XMLStreamConstants.START_ELEMENT: {
                StartElement element = event.asStartElement();
                if (name(element, "Point2D")) {
                    x = Double.parseDouble(event.asStartElement().getAttributeByName(new QName("x")).getValue());
                    y = Double.parseDouble(event.asStartElement().getAttributeByName(new QName("y")).getValue());
                    if (x < minx)
                        minx = x;
                    if (y < miny)
                        miny = y;
                }
                if (name(element, "Intent")) {
                    HashSet<String> attr = new HashSet<>();
                    event = parser.nextEvent();
                    while (!(event.isEndElement() && name(event.asEndElement(), "Intent"))) {
                        if (event.isStartElement()) {
                            int i = Integer.parseInt(event.asStartElement()
                                    .getAttributeByName(new QName("AttributeIdentifier")).getValue());
                            attr.add(context.getAttributeAtIndex(i));
                        }
                        event = parser.nextEvent();
                    }
                    Node n = getNodeWithIntent(attr);
                    n.getObjectsLabel().setXY((int) x, (int) y);
                }
                break;
            }
            case XMLStreamConstants.END_ELEMENT:
                if (name(event.asEndElement(), "ConceptLabels")) {
                    if (minx < 0 && miny < 0) {
                        lattice.translate((int) -minx + 10, (int) -miny + 10);
                        return;
                    } else if (miny < 0) {
                        lattice.translate(0, (int) -miny + 10);
                        return;

                    } else if (minx < 0) {
                        lattice.translate((int) -minx + 10, 0);
                        return;
                    }
                    return;
                }
            }
        }
    }

    private void getAttributeLabels(XMLEventReader parser) throws XMLStreamException {
        double x = 0, y = 0;
        double minx = Double.MAX_VALUE, miny = Double.MAX_VALUE;
        while (parser.hasNext()) {
            XMLEvent event = parser.nextEvent();
            switch (event.getEventType()) {
            case XMLStreamConstants.START_ELEMENT: {
                StartElement element = event.asStartElement();
                if (name(element, "Point2D")) {
                    x = Double.parseDouble(event.asStartElement().getAttributeByName(new QName("x")).getValue());
                    y = Double.parseDouble(event.asStartElement().getAttributeByName(new QName("y")).getValue());
                    if (x < minx)
                        minx = x;
                    if (y < miny)
                        miny = y;
                }
                if (name(element, "Intent")) {
                    HashSet<String> attr = new HashSet<>();
                    event = parser.nextEvent();
                    while (!(event.isEndElement() && name(event.asEndElement(), "Intent"))) {
                        if (event.isStartElement()) {
                            int i = Integer.parseInt(event.asStartElement()
                                    .getAttributeByName(new QName("AttributeIdentifier")).getValue());
                            attr.add(context.getAttributeAtIndex(i));
                        }
                        event = parser.nextEvent();
                    }
                    Node n = getNodeWithIntent(attr);
                    n.getAttributesLabel().setXY((int) x, (int) y);
                }
                break;
            }
            case XMLStreamConstants.END_ELEMENT:
                if (name(event.asEndElement(), "UpConceptLabels")) {
                    if (minx < 0 && miny < 0) {
                        lattice.translate((int) -minx + 10, (int) -miny + 10);
                        return;
                    } else if (miny < 0) {
                        lattice.translate(0, (int) -miny + 10);
                        return;

                    } else if (minx < 0) {
                        lattice.translate((int) -minx + 10, 0);
                        return;
                    }
                    return;
                }
            }
        }
    }

    private void getNodes(XMLEventReader parser) throws XMLStreamException {
        double x = 0, y = 0;
        ArrayList<Node> nodes = new ArrayList<>();
        double minx = Double.MAX_VALUE, miny = Double.MAX_VALUE;
        while (parser.hasNext()) {
            XMLEvent event = parser.nextEvent();
            switch (event.getEventType()) {
            case XMLStreamConstants.START_ELEMENT: {
                StartElement element = event.asStartElement();
                if (name(element, "Point2D")) {
                    x = Double.parseDouble(event.asStartElement().getAttributeByName(new QName("x")).getValue());
                    y = Double.parseDouble(event.asStartElement().getAttributeByName(new QName("y")).getValue());
                    if (x < minx)
                        minx = x;
                    if (y < miny)
                        miny = y;
                }
                if (name(element, "Intent")) {
                    Node n = new Node();
                    n.setX((int) x);
                    n.setY((int) y);
                    event = parser.nextEvent();
                    while (!(event.isEndElement() && name(event.asEndElement(), "Intent"))) {
                        if (event.isStartElement()) {
                            int i = Integer.parseInt(event.asStartElement()
                                    .getAttributeByName(new QName("AttributeIdentifier")).getValue());
                            n.addAttribute(context.getAttributeAtIndex(i));
                        }
                        event = parser.nextEvent();
                    }
                    nodes.add(n);
                    event = parser.nextEvent();
                    if (event.isStartElement()) {
                        element = event.asStartElement();

                        if (name(element, "Extent")) {

                            event = parser.nextEvent();
                            while (!(event.isEndElement() && name(event.asEndElement(), "Extent"))) {

                                if (event.isStartElement()) {
                                    int i = Integer.parseInt(event.asStartElement()
                                            .getAttributeByName(new QName("ObjectIdentifier")).getValue());
                                    n.addObject(context.getObjectAtIndex(i).getIdentifier());
                                }
                                event = parser.nextEvent();
                            }
                            Concept<String, FullObject<String, String>> concept = new LatticeConcept();
                            for (String attr : n.getAttributes()) {
                                concept.getIntent().add(attr);
                            }
                            for (String obj : n.getObjects()) {
                                concept.getExtent().add(context.getObject(obj));
                            }
                            concepts.add(concept);
                        }

                    }

                }
                break;
            }
            case XMLStreamConstants.END_ELEMENT:
                if (name(event.asEndElement(), "ConceptFigures")) {
                    lattice.setNodes(nodes);
                    if (minx < 0 && miny < 0) {
                        lattice.translate((int) -minx + 10, (int) -miny + 10);
                        return;
                    } else if (miny < 0) {
                        lattice.translate(0, (int) -miny + 10);
                        return;

                    } else if (minx < 0) {
                        lattice.translate((int) -minx + 10, 0);
                        return;
                    }
                    return;
                }
            }
        }
    }

    private Node getNodeWithIntent(Set<String> attributes) {
        for (Node n : lattice.getNodes()) {
            if (n.getAttributes().containsAll(attributes) && attributes.containsAll(n.getAttributes()))
                return n;
        }
        return null;
    }

    private boolean name(StartElement element, String string) {
        return element.getName().toString().equals(string);
    }

    private boolean name(EndElement element, String string) {
        return element.getName().toString().equals(string);
    }

    private void addObjects(XMLEventReader parser) throws XMLStreamException, IllegalObjectException, IOException {
        while (parser.hasNext()) {
            XMLEvent event = parser.nextEvent();
            switch (event.getEventType()) {
            case XMLStreamConstants.START_ELEMENT:
                StartElement element = event.asStartElement();
                if (element.getName().toString().equals("Name")) {
                    String objName = "";
                    while ((event = parser.nextEvent()).isCharacters()) {
                        objName += event.asCharacters().getData();
                    }
                    ListSet<String> attrs = new ListSet<>();
                    while (!((event = parser.nextEvent()).isEndElement() && event.asEndElement().getName().toString()
                            .equals("Intent"))) {
                        if (event.isStartElement()
                                && event.asStartElement().getName().toString().equals("HasAttribute")) {
                            int i = Integer.parseInt(event.asStartElement()
                                    .getAttributeByName(new QName("AttributeIdentifier")).getValue());
                            try {
                                attrs.add(context.getAttributeAtIndex(i));
                            } catch (java.lang.IndexOutOfBoundsException e) {
                                throw new IOException("There are not enough attributes");
                            }
                        }
                    }

                    context.addObject(new FullObject<String, String>(objName, attrs));
                }
                break;
            case XMLStreamConstants.END_ELEMENT:
                if (event.asEndElement().getName().toString().equals("Objects"))
                    return;
            }
        }
    }

    private void addAttributes(XMLEventReader parser) throws XMLStreamException {
        while (parser.hasNext()) {
            XMLEvent event = parser.nextEvent();
            switch (event.getEventType()) {
            case XMLStreamConstants.START_ELEMENT:
                StartElement element = event.asStartElement();
                if (element.getName().toString().equals("Name")) {
                    String attributeName = "";
                    while ((event = parser.nextEvent()).isCharacters()) {
                        attributeName += event.asCharacters().getData();
                    }
                    context.addAttribute(attributeName);
                }
                break;
            case XMLStreamConstants.END_ELEMENT:
                if (event.asEndElement().getName().toString().equals("Attributes"))
                    return;
            }
        }
    }
}
