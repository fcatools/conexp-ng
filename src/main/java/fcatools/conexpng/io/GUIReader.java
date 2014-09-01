package fcatools.conexpng.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import fcatools.conexpng.Conf;
import fcatools.conexpng.GUIConf;
import fcatools.conexpng.gui.MainFrame;

/**
 * Parses the gui state and restores it for the given context.
 * 
 * @author Torsten Casselt
 */
public class GUIReader {

    private GUIConf guiConf = new GUIConf();

    /**
     * Creates a GUIReader.
     * 
     * @param mainFrame
     *            to update window after loading gui state
     * @param state
     *            to save the parsed GUIConf class to
     * @param path
     *            to read the gui state from
     * @throws XMLStreamException
     * @throws FileNotFoundException
     * @throws SecurityException
     * @throws NoSuchFieldException
     * @throws IllegalAccessException
     * @throws IllegalArgumentException
     * @throws NumberFormatException
     */
    public GUIReader(MainFrame mainFrame, Conf state, String path) throws XMLStreamException, FileNotFoundException,
            NumberFormatException,
            IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException {
        // check if gui file already exists, if not it is ignored
        if (new File(path.concat(".gui")).exists()) {
            InputStream in = new FileInputStream(path.concat(".gui"));
            XMLInputFactory factory = XMLInputFactory.newInstance();
            XMLEventReader parser = factory.createXMLEventReader(in);
            // check first element for start symbol, start parsing there, else
            // syntax error
            if (parser.hasNext()) {
                // discard document start
                parser.nextEvent();
                XMLEvent event = parser.nextEvent();
                // check for GUIConf start element, start parsing only if it
                // exists
                if (parser.hasNext() && event.getEventType() == XMLStreamConstants.START_ELEMENT
                        && event.asStartElement().getName().toString().equals("GUIConf")) {
                    readGuiState(parser);
                } else {
                    throw new XMLStreamException();
                }
            }
        }
        // initialize column widths if not already saved in file
        if (guiConf.columnWidths == null) {
            guiConf.columnWidths = new HashMap<>();
        }
        state.guiConf = guiConf;
        // update window if gui state was loaded when application already
        // started and main frame was built
        if (mainFrame != null) {
            mainFrame.updateGUI();
        }
    }

    /**
     * Reads the gui state with given parser and sets it in the current
     * {@link GUIConf}.
     * 
     * @param parser
     *            to read with
     * @throws XMLStreamException
     * @throws NumberFormatException
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     * @throws NoSuchFieldException
     * @throws SecurityException
     */
    private void readGuiState(XMLEventReader parser) throws XMLStreamException, NumberFormatException,
            IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException {
        while (parser.hasNext()) {
            XMLEvent event = parser.nextEvent();
            if (event.getEventType() == XMLStreamConstants.START_ELEMENT) {
                StartElement element = event.asStartElement();
                if (element.getName().toString().equals("ColumnWidths")) {
                    readColumnWidths(parser);
                } else {
                    String name = element.getName().toString();
                    String temp = "";
                    while ((event = parser.nextEvent()).isCharacters()) {
                        temp += event.asCharacters().getData();
                    }
                    if (guiConf.getClass().getField(name).getType() == Integer.TYPE) {
                        guiConf.getClass().getField(name).setInt(guiConf, Integer.parseInt(temp));
                    } else if (guiConf.getClass().getField(name).getType() == Boolean.TYPE) {
                        guiConf.getClass().getField(name).setBoolean(guiConf, Boolean.parseBoolean(temp));
                    } else if (guiConf.getClass().getField(name).getType() == Double.TYPE) {
                        guiConf.getClass().getField(name).setDouble(guiConf, Double.parseDouble(temp));
                    } else if (guiConf.getClass().getField(name).getClass().equals(String.class)) {
                        guiConf.getClass().getField(name).set(guiConf, temp);
                    }
                }
            } else if (event.getEventType() == XMLStreamConstants.END_DOCUMENT) {
                // end of file is reached, return
                return;
            }
        }
    }

    /**
     * Reads column widths with given parser.
     * 
     * @param parser
     *            to read with
     * @throws XMLStreamException
     */
    private void readColumnWidths(XMLEventReader parser) throws XMLStreamException {
        guiConf.columnWidths = new HashMap<>();
        int column = 0, width = 0;
        while (parser.hasNext()) {
            XMLEvent event = parser.nextEvent();
            switch (event.getEventType()) {
            case XMLStreamConstants.START_ELEMENT:
                StartElement element = event.asStartElement();
                if (element.getName().toString().equals("Column")) {
                    column = Integer.parseInt(element.getAttributeByName(new QName("Number")).getValue());
                    event = parser.nextEvent();
                    String temp = "";
                    while ((event = parser.nextEvent()).isCharacters()) {
                        temp += event.asCharacters().getData();
                    }
                    width = Integer.parseInt(temp);
                    guiConf.columnWidths.put(column, width);
                }
                break;
            case XMLStreamConstants.END_ELEMENT:
                if (event.asEndElement().getName().toString().equals("ColumnWidths")) {
                    return;
                }
            }
        }
    }
}
