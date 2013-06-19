package fcatools.conexpng.io;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import de.tudresden.inf.tcs.fcaapi.exception.IllegalObjectException;
import de.tudresden.inf.tcs.fcalib.FullObject;
import de.tudresden.inf.tcs.fcalib.utils.ListSet;

import fcatools.conexpng.ProgramState;
import fcatools.conexpng.model.FormalContext;

public class CEXReader {

    private FormalContext context;

    public CEXReader(ProgramState state) {
        InputStream in = null;
        try {
            in = new FileInputStream(state.filePath);
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
        }
        XMLInputFactory factory = XMLInputFactory.newInstance();
        XMLEventReader parser = null;
        try {
            parser = factory.createXMLEventReader(in);
        } catch (XMLStreamException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        while (parser.hasNext()) {
            XMLEvent event = null;
            try {
                event = parser.nextEvent();
            } catch (XMLStreamException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            switch (event.getEventType()) {
            case XMLStreamConstants.START_DOCUMENT:
                context = new FormalContext();
                break;
            case XMLStreamConstants.END_DOCUMENT:
                try {
                    parser.close();
                } catch (XMLStreamException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                break;
            case XMLStreamConstants.START_ELEMENT:
                StartElement element = event.asStartElement();
                try {
                    if (element.getName().toString().equals("Attributes"))
                        addAttributes(parser);
                    if (element.getName().toString().equals("Objects"))
                        addObjects(parser);
                } catch (XMLStreamException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                break;
            case XMLStreamConstants.END_ELEMENT:
                state.newContext(context);
                break;
            default:
                break;
            }
        }
    }

    private void addObjects(XMLEventReader parser) throws XMLStreamException {
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
                            attrs.add(context.getAttributeAtIndex(i));
                        }
                    }
                    try {
                        context.addObject(new FullObject<String, String>(
                                objName, attrs));
                    } catch (IllegalObjectException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
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
