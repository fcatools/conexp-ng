package fcatools.conexpng.io;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.HashMap;

import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import de.tudresden.inf.tcs.fcalib.FullObject;

import fcatools.conexpng.ProgramState;

public class CEXWriter {

    private ProgramState state;

    private XMLEventFactory eventFactory = XMLEventFactory.newInstance();

    private HashMap<String, String> ids = new HashMap<>();

    public CEXWriter(ProgramState state) throws XMLStreamException,
            FileNotFoundException {
        this.state = state;
        XMLEventWriter writer = null;
        XMLOutputFactory outputFactory = XMLOutputFactory.newInstance();

        writer = outputFactory.createXMLEventWriter(new FileOutputStream(
                state.filePath));

        writer.add(eventFactory.createStartDocument());
        writer.add(eventFactory.createStartElement("", "", "ConceptualSystem"));
        writer.add(eventFactory.createStartElement("", "", "Version"));
        // to show that this is our cex-format (2.0)
        writer.add(eventFactory.createAttribute("Majornumber", "2"));
        writer.add(eventFactory.createAttribute("Minornumber", "0"));
        writer.add(eventFactory.createEndElement("", "", "Version"));
        addContext(writer);
        writer.add(eventFactory.createEndElement("", "", "ConceptualSystem"));
        writer.add(eventFactory.createEndDocument());
    }

    private void addContext(XMLEventWriter writer) throws XMLStreamException {
        writer.add(eventFactory.createStartElement("", "", "Contexts"));
        writer.add(eventFactory.createStartElement("", "", "Context"));
        writer.add(eventFactory.createAttribute("Type", "Binary"));
        writer.add(eventFactory.createAttribute("Identifier", "0"));

        addAttributes(writer);

        addObjects(writer);

        writer.add(eventFactory.createEndElement("", "", "Context"));
        writer.add(eventFactory.createEndElement("", "", "Contexts"));

    }

    private void addAttributes(XMLEventWriter writer) throws XMLStreamException {
        writer.add(eventFactory.createStartElement("", "", "Attributes"));

        for (int i = 0; i < state.context.getAttributeCount(); i++) {
            writer.add(eventFactory.createStartElement("", "", "Attribute"));
            writer.add(eventFactory.createAttribute("Identifier", "" + i));
            writer.add(eventFactory.createStartElement("", "", "Name"));

            ids.put(state.context.getAttributeAtIndex(i), "" + i);
            writer.add(eventFactory.createCharacters(state.context
                    .getAttributeAtIndex(i)));

            writer.add(eventFactory.createEndElement("", "", "Name"));
            writer.add(eventFactory.createEndElement("", "", "Attribute"));
        }
        writer.add(eventFactory.createEndElement("", "", "Attributes"));

    }

    private void addObjects(XMLEventWriter writer) throws XMLStreamException {
        writer.add(eventFactory.createStartElement("", "", "Objects"));

        for (FullObject<String, String> obj : state.context.getObjects()) {

            writer.add(eventFactory.createStartElement("", "", "Object"));
            writer.add(eventFactory.createStartElement("", "", "Name"));
            writer.add(eventFactory.createCharacters(obj.getIdentifier()));
            writer.add(eventFactory.createEndElement("", "", "Name"));
            writer.add(eventFactory.createStartElement("", "", "Intent"));
            for (String attr : state.context.getAttributesForObject(obj
                    .getIdentifier())) {
                writer.add(eventFactory.createStartElement("", "",
                        "HasAttribute"));
                writer.add(eventFactory.createAttribute("AttributeIdentifier",
                        "" + ids.get(attr)));
                writer.add(eventFactory
                        .createEndElement("", "", "HasAttribute"));
            }
            writer.add(eventFactory.createEndElement("", "", "Intent"));
            writer.add(eventFactory.createEndElement("", "", "Object"));
        }
        writer.add(eventFactory.createEndElement("", "", "Objects"));

    }

}
