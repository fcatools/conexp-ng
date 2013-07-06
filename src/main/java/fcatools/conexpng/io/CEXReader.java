package fcatools.conexpng.io;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import de.tudresden.inf.tcs.fcaapi.exception.IllegalObjectException;
import de.tudresden.inf.tcs.fcalib.FullObject;
import de.tudresden.inf.tcs.fcalib.utils.ListSet;

import fcatools.conexpng.ProgramState;
import fcatools.conexpng.gui.lattice.LatticeGraph;
import fcatools.conexpng.gui.lattice.Node;
import fcatools.conexpng.model.FormalContext;
import fcatools.conexpng.model.TestLatticeAlgorithm;

public class CEXReader {

    private FormalContext context;
    private LatticeGraph lattice;
    private boolean ourCEX;
    private Map<Integer, Integer> columnWidths;

    public CEXReader(ProgramState state) throws XMLStreamException,
            IllegalObjectException, IOException {
        InputStream in = null;

        in = new FileInputStream(state.filePath);

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
                if (name(element, "Version"))
                    ourCEX = element.asStartElement()
                            .getAttributeByName(new QName("MajorNumber"))
                            .toString().equals("1") ? true : false;
                if (name(element, "Attributes"))
                    addAttributes(parser);
                if (name(element, "Objects"))
                    addObjects(parser);
                if (name(element, "Lattice"))
                    addLatice(parser);
                if (name(element, "ColumnWidths"))
                    getColumnWidths(parser);
                break;
            default:
                break;
            }
        }
        if (lattice == null) {
            lattice = new TestLatticeAlgorithm().computeLatticeGraph(context
                    .getConcepts());
        }
        if (columnWidths == null) {
            columnWidths = new HashMap<>();
        }

        state.columnWidths = columnWidths;
        state.loadedFile(context, lattice);
    }

 

    private void addLatice(XMLEventReader parser) throws XMLStreamException {
        if (!ourCEX)
            lattice = new TestLatticeAlgorithm().computeLatticeGraph(context
                    .getConcepts());
        while (parser.hasNext()) {
            XMLEvent event = parser.nextEvent();
            switch (event.getEventType()) {
            case XMLStreamConstants.START_ELEMENT: {
                StartElement element = event.asStartElement();
                if (name(element, "ConceptFigures")) {
                    getNodes(parser);
                }
                break;
            }
            case XMLStreamConstants.END_ELEMENT:
                if (event.asEndElement().getName().toString().equals("Lattice"))
                    return;
            }
        }

    }

    private void getNodes(XMLEventReader parser) throws XMLStreamException {
        double x = 0, y = 0;
        double minx = Double.MAX_VALUE, miny = Double.MAX_VALUE;
        while (parser.hasNext()) {
            XMLEvent event = parser.nextEvent();
            switch (event.getEventType()) {
            case XMLStreamConstants.START_ELEMENT: {
                StartElement element = event.asStartElement();
                if (name(element, "Point2D")) {
                    x = Double.parseDouble(event.asStartElement()
                            .getAttributeByName(new QName("x")).getValue());
                    y = Double.parseDouble(event.asStartElement()
                            .getAttributeByName(new QName("y")).getValue());
                    if (x < minx)
                        minx = x;
                    if (y < miny)
                        miny = y;
                }
                if (name(element, "Intent")) {
                    HashSet<String> attr = new HashSet<>();
                    event = parser.nextEvent();
                    while (!(event.isEndElement() && name(event.asEndElement(),
                            "Intent"))) {
                        if (event.isStartElement()) {
                            int i = Integer.parseInt(event
                                    .asStartElement()
                                    .getAttributeByName(
                                            new QName("AttributeIdentifier"))
                                    .getValue());
                            attr.add(context.getAttributeAtIndex(i));
                        }
                        event = parser.nextEvent();
                    }
                    Node n = getNodeWithIntent(attr);
                    n.setX((int) x);
                    n.setY((int) y);
                }
                break;
            }
            case XMLStreamConstants.END_ELEMENT:
                if (name(event.asEndElement(), "ConceptFigures")) {
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
            if (n.getAttributes().containsAll(attributes)
                    && attributes.containsAll(n.getAttributes()))
                return n;
        }
        return null;
    }
    
    private void getColumnWidths(XMLEventReader parser)
            throws XMLStreamException {
        columnWidths = new HashMap<>();
        int column = 0, width = 0;
        while (parser.hasNext()) {
            XMLEvent event = parser.nextEvent();
            switch (event.getEventType()) {
            case XMLStreamConstants.START_ELEMENT:
                StartElement element = event.asStartElement();
                if (name(element, "Column")) {
                    column = Integer.parseInt(element.getAttributeByName(
                            new QName("Number")).getValue());
                    event = parser.nextEvent();
                    String temp = "";
                    while ((event = parser.nextEvent()).isCharacters()) {
                        temp += event.asCharacters().getData();
                    }
                    width = Integer.parseInt(temp);
                    columnWidths.put(column, width);
                }
                break;
            case XMLStreamConstants.END_ELEMENT:
                if (event.asEndElement().getName().toString()
                        .equals("ColumnWidths"))
                    return;
            }
        }
    }

    private boolean name(StartElement element, String string) {
        return element.getName().toString().equals(string);
    }

    private boolean name(EndElement element, String string) {
        return element.getName().toString().equals(string);
    }

    private void addObjects(XMLEventReader parser) throws XMLStreamException,
            IllegalObjectException, IOException {
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
                    while (!((event = parser.nextEvent()).isEndElement() && event
                            .asEndElement().getName().toString()
                            .equals("Intent"))) {
                        if (event.isStartElement()
                                && event.asStartElement().getName().toString()
                                        .equals("HasAttribute")) {
                            int i = Integer.parseInt(event
                                    .asStartElement()
                                    .getAttributeByName(
                                            new QName("AttributeIdentifier"))
                                    .getValue());
                            try {
                                attrs.add(context.getAttributeAtIndex(i));
                            } catch (java.lang.IndexOutOfBoundsException e) {
                                throw new IOException(
                                        "There are not enough attributes");
                            }
                        }
                    }

                    context.addObject(new FullObject<String, String>(objName,
                            attrs));
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
                if (event.asEndElement().getName().toString()
                        .equals("Attributes"))
                    return;
            }
        }
    }
}