package fcatools.conexpng.io;

import de.tudresden.inf.tcs.fcaapi.exception.IllegalObjectException;
import de.tudresden.inf.tcs.fcalib.FullObject;
import de.tudresden.inf.tcs.fcalib.utils.ListSet;
import fcatools.conexpng.ProgramState;
import fcatools.conexpng.model.FormalContext;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

public class CEXReader {

    private FormalContext context;

    public CEXReader(ProgramState state) throws XMLStreamException, IllegalObjectException, IOException {
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

                if (name(element, "Attributes"))
                    addAttributes(parser);
                if (name(element, "Objects"))
                    addObjects(parser);
                if (name(element, "Lattice"))
                    addLatice(parser);

                break;
            default:
                break;
            }
        }
        // TODO: Read column widths if present!
        state.columnWidths = new HashMap<>();
        state.newContext(context);
    }

    private void addLatice(XMLEventReader parser) throws XMLStreamException {

        while (parser.hasNext()) {
            XMLEvent event = parser.nextEvent();
            switch (event.getEventType()) {
            case XMLStreamConstants.START_ELEMENT:
                StartElement element = event.asStartElement();

                break;
            case XMLStreamConstants.END_ELEMENT:
                if (event.asEndElement().getName().toString().equals("Lattice"))
                    return;
            }
        }

    }

    private boolean name(StartElement element, String string) {
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
                            try{
                            attrs.add(context.getAttributeAtIndex(i));
                            } catch (java.lang.IndexOutOfBoundsException e){
                                throw new IOException("There are not enough attributes");
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
