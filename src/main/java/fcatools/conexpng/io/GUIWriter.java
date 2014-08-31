package fcatools.conexpng.io;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.lang.reflect.Field;

import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;

import fcatools.conexpng.Conf;
import fcatools.conexpng.GUIConf;

/**
 * Saves the gui state for every file separately. It is possible then to have
 * different views for multiple contexts.
 * 
 * @author Torsten Casselt
 */
public class GUIWriter {

    private XMLEventFactory eventFactory = XMLEventFactory.newInstance();
    private XMLOutputFactory outputFactory = XMLOutputFactory.newInstance();
    private GUIConf guiConf;

    /**
     * Creates a GUIWriter.
     * 
     * @param state
     *            to fetch GUIConf class from
     * @param path
     *            to save the gui state to
     * @throws XMLStreamException
     * @throws FileNotFoundException
     * @throws IllegalAccessException
     * @throws IllegalArgumentException
     */
    public GUIWriter(Conf state, String path) throws XMLStreamException, FileNotFoundException,
            IllegalArgumentException, IllegalAccessException {
        guiConf = state.guiConf;

        XMLEventWriter writer = outputFactory.createXMLEventWriter(new FileOutputStream(path.concat(".gui")));

        writer.add(eventFactory.createStartDocument());
        writer.add(eventFactory.createStartElement("", "", "GUIConf"));
        // add gui state taken from GUIConf class
        for (Field f : guiConf.getClass().getDeclaredFields()) {
            if (f.getName().equals("columnWidths")) {
                addColumnWidths(writer);
                continue;
            }
            writer.add(eventFactory.createStartElement("", "", f.getName()));
            writer.add(eventFactory.createCharacters("" + f.get(state.guiConf)));
            writer.add(eventFactory.createEndElement("", "", f.getName()));
        }
        writer.add(eventFactory.createEndElement("", "", "GUIConf"));
        writer.add(eventFactory.createEndDocument());
        writer.close();
    }

    /**
     * Adds column widths to xml file.
     * 
     * @param writer
     *            to write column widths xml to
     * @throws XMLStreamException
     */
    private void addColumnWidths(XMLEventWriter writer) throws XMLStreamException {
        writer.add(eventFactory.createStartElement("", "", "ColumnWidths"));

        for (int column : guiConf.columnWidths.keySet()) {
            writer.add(eventFactory.createStartElement("", "", "Column"));
            writer.add(eventFactory.createAttribute("Number", "" + column));
            writer.add(eventFactory.createStartElement("", "", "Width"));

            writer.add(eventFactory.createCharacters("" + guiConf.columnWidths.get(column)));

            writer.add(eventFactory.createEndElement("", "", "Width"));
            writer.add(eventFactory.createEndElement("", "", "Column"));
        }
        writer.add(eventFactory.createEndElement("", "", "ColumnWidths"));

    }
}
