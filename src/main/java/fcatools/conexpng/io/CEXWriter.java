package fcatools.conexpng.io;

import de.tudresden.inf.tcs.fcaapi.FCAImplication;
import de.tudresden.inf.tcs.fcalib.FullObject;
import fcatools.conexpng.Conf;
import fcatools.conexpng.gui.lattice.Node;
import fcatools.conexpng.model.AssociationRule;

import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.lang.reflect.Field;
import java.util.HashMap;

public class CEXWriter {

    private Conf state;

    private XMLEventFactory eventFactory = XMLEventFactory.newInstance();

    private HashMap<String, String> attrIds = new HashMap<>();

    public CEXWriter(Conf state, String path) throws XMLStreamException, FileNotFoundException {
        this.state = state;
        XMLEventWriter writer = null;
        XMLOutputFactory outputFactory = XMLOutputFactory.newInstance();

        writer = outputFactory.createXMLEventWriter(new FileOutputStream(path));

        writer.add(eventFactory.createStartDocument());
        writer.add(eventFactory.createStartElement("", "", "ConceptualSystem"));
        writer.add(eventFactory.createStartElement("", "", "Version"));
        // to show that this is our cex-format (2.0)
        writer.add(eventFactory.createAttribute("MajorNumber", "2"));
        writer.add(eventFactory.createAttribute("MinorNumber", "0"));
        writer.add(eventFactory.createEndElement("", "", "Version"));
        addContext(writer);
        addAssociations(writer);
        addImplications(writer);
        addGUIState(writer);
        addLatice(writer);
        writer.add(eventFactory.createEndElement("", "", "ConceptualSystem"));
        writer.add(eventFactory.createEndDocument());
        writer.close();
    }

    private void addLatice(XMLEventWriter writer) throws XMLStreamException {
        writer.add(eventFactory.createStartElement("", "", "Lattices"));
        writer.add(eventFactory.createStartElement("", "", "Lattice"));
        if (!state.context.getDontConsideredAttr().isEmpty()) {
            writer.add(eventFactory.createStartElement("", "", "AttributeMask"));
            for (String attr : state.context.getAttributes()) {
                if (!state.context.getDontConsideredAttr().contains(attr)) {
                    writer.add(eventFactory.createStartElement("", "", "HasAttribute"));
                    writer.add(eventFactory.createAttribute("AttributeIdentifier", "" + attrIds.get(attr)));
                    writer.add(eventFactory.createEndElement("", "", "HasAttribute"));
                }
            }
            writer.add(eventFactory.createEndElement("", "", "AttributeMask"));
        }
        if (!state.context.getDontConsideredObj().isEmpty()) {
            writer.add(eventFactory.createStartElement("", "", "ObjectMask"));
            for (int i = 0; i < state.context.getObjectCount(); i++) {
                if (!state.context.getDontConsideredObj().contains(state.context.getObjectAtIndex(i))) {
                    writer.add(eventFactory.createStartElement("", "", "HasAttribute"));
                    writer.add(eventFactory.createAttribute("AttributeIdentifier", "" + i));
                    writer.add(eventFactory.createEndElement("", "", "HasAttribute"));
                }
            }
            writer.add(eventFactory.createEndElement("", "", "ObjectMask"));
        }
        writer.add(eventFactory.createStartElement("", "", "LineDiagram"));
        writer.add(eventFactory.createStartElement("", "", "ConceptFigures"));
        for (Node n : state.lattice.getNodes()) {
            writer.add(eventFactory.createStartElement("", "", "LineDiagramFigure"));
            writer.add(eventFactory.createAttribute("Type", "Concept"));
            writer.add(eventFactory.createStartElement("", "", "FigureCooords"));
            writer.add(eventFactory.createStartElement("", "", "Point2D"));
            writer.add(eventFactory.createAttribute("x", "" + n.getX()));
            writer.add(eventFactory.createAttribute("y", "" + n.getY()));
            writer.add(eventFactory.createEndElement("", "", "Point2D"));
            writer.add(eventFactory.createEndElement("", "", "FigureCooords"));

            writer.add(eventFactory.createStartElement("", "", "Intent"));
            for (String attr : n.getAttributes()) {
                writer.add(eventFactory.createStartElement("", "", "HasAttribute"));
                writer.add(eventFactory.createAttribute("AttributeIdentifier", "" + attrIds.get(attr)));
                writer.add(eventFactory.createEndElement("", "", "HasAttribute"));
            }
            writer.add(eventFactory.createEndElement("", "", "Intent"));
            writer.add(eventFactory.createEndElement("", "", "LineDiagramFigure"));
        }
        writer.add(eventFactory.createEndElement("", "", "ConceptFigures"));

        writer.add(eventFactory.createStartElement("", "", "LineDiagramSettings"));
        createSettingsElement(writer, "AttributeLabelsDisplayMode", "AllAttribsMultiLabelsStrategy");
        createSettingsElement(writer, "ObjectLabelsDisplayMode", "AllObjectsLabelsStrategy");
        createSettingsElement(writer, "LabelFontSize", "12");
        createSettingsElement(writer, "ShowCollisions", "false");
        createSettingsElement(writer, "MaxNodeRadius", "8");
        createSettingsElement(writer, "EdgeDisplayMode", "ConceptConnectionEdgeSizeCalcStrategy");
        createSettingsElement(writer, "HighlightMode", "FilterIdealHighlightStrategy");
        createSettingsElement(writer, "GridSizeX", "75");
        createSettingsElement(writer, "GridSizeY", "55");
        createSettingsElement(writer, "Layout", "MinIntersectionLayout");
        writer.add(eventFactory.createEndElement("", "", "LineDiagramSettings"));

        writer.add(eventFactory.createStartElement("", "", "UpConceptLabels"));
        for (Node n : state.lattice.getNodes()) {
            if (!n.getVisibleAttributes().isEmpty()) {
                writer.add(eventFactory.createStartElement("", "", "LineDiagramFigure"));
                writer.add(eventFactory.createAttribute("Type", "ConceptLabel"));
                writer.add(eventFactory.createStartElement("", "", "FigureCooords"));
                writer.add(eventFactory.createStartElement("", "", "Point2D"));
                writer.add(eventFactory.createAttribute("x", "" + n.getAttributesLabel().getX()));
                writer.add(eventFactory.createAttribute("y", "" + n.getAttributesLabel().getY()));
                writer.add(eventFactory.createEndElement("", "", "Point2D"));
                writer.add(eventFactory.createEndElement("", "", "FigureCooords"));

                writer.add(eventFactory.createStartElement("", "", "Intent"));
                for (String attr : n.getAttributes()) {
                    writer.add(eventFactory.createStartElement("", "", "HasAttribute"));
                    writer.add(eventFactory.createAttribute("AttributeIdentifier", "" + attrIds.get(attr)));
                    writer.add(eventFactory.createEndElement("", "", "HasAttribute"));
                }
                writer.add(eventFactory.createEndElement("", "", "Intent"));
                writer.add(eventFactory.createEndElement("", "", "LineDiagramFigure"));
            }
        }
        writer.add(eventFactory.createEndElement("", "", "UpConceptLabels"));

        writer.add(eventFactory.createStartElement("", "", "ConceptLabels"));
        for (Node n : state.lattice.getNodes()) {
            if (!n.getVisibleObjects().isEmpty()) {
                writer.add(eventFactory.createStartElement("", "", "LineDiagramFigure"));
                writer.add(eventFactory.createAttribute("Type", "ConceptLabel"));
                writer.add(eventFactory.createStartElement("", "", "FigureCooords"));
                writer.add(eventFactory.createStartElement("", "", "Point2D"));
                writer.add(eventFactory.createAttribute("x", "" + n.getObjectsLabel().getX()));
                writer.add(eventFactory.createAttribute("y", "" + n.getObjectsLabel().getY()));
                writer.add(eventFactory.createEndElement("", "", "Point2D"));
                writer.add(eventFactory.createEndElement("", "", "FigureCooords"));

                writer.add(eventFactory.createStartElement("", "", "Intent"));
                for (String attr : n.getAttributes()) {
                    writer.add(eventFactory.createStartElement("", "", "HasAttribute"));
                    writer.add(eventFactory.createAttribute("AttributeIdentifier", "" + attrIds.get(attr)));
                    writer.add(eventFactory.createEndElement("", "", "HasAttribute"));
                }
                writer.add(eventFactory.createEndElement("", "", "Intent"));
                writer.add(eventFactory.createEndElement("", "", "LineDiagramFigure"));
            }
        }
        writer.add(eventFactory.createEndElement("", "", "ConceptLabels"));

        writer.add(eventFactory.createEndElement("", "", "LineDiagram"));
        writer.add(eventFactory.createEndElement("", "", "Lattice"));
        writer.add(eventFactory.createEndElement("", "", "Lattices"));
    }

    private void createSettingsElement(XMLEventWriter writer, String element, String attribute)
            throws XMLStreamException {
        writer.add(eventFactory.createStartElement("", "", element));
        writer.add(eventFactory.createAttribute("Value", attribute));
        writer.add(eventFactory.createEndElement("", "", element));

    }

    private void addImplications(XMLEventWriter writer) throws XMLStreamException {
        if (state.implications != null && !state.implications.isEmpty()) {
            writer.add(eventFactory.createStartElement("", "", "Implications"));
            for (FCAImplication<String> i : state.implications) {
                writer.add(eventFactory.createStartElement("", "", "Implication"));
                writer.add(eventFactory.createStartElement("", "", "Premise"));
                for (String premise : i.getPremise()) {
                    writer.add(eventFactory.createStartElement("", "", "HasAttribute"));
                    writer.add(eventFactory.createAttribute("AttributeIdentifier", "" + attrIds.get(premise)));
                    writer.add(eventFactory.createEndElement("", "", "HasAttribute"));
                }
                writer.add(eventFactory.createEndElement("", "", "Premise"));
                writer.add(eventFactory.createStartElement("", "", "Conclusion"));
                for (String conc : i.getConclusion()) {
                    writer.add(eventFactory.createStartElement("", "", "HasAttribute"));
                    writer.add(eventFactory.createAttribute("AttributeIdentifier", "" + attrIds.get(conc)));
                    writer.add(eventFactory.createEndElement("", "", "HasAttribute"));
                }
                writer.add(eventFactory.createEndElement("", "", "Conclusion"));
                writer.add(eventFactory.createEndElement("", "", "Implication"));
            }
            writer.add(eventFactory.createEndElement("", "", "Implications"));
        }
    }

    private void addAssociations(XMLEventWriter writer) throws XMLStreamException {
        if (state.associations != null && !state.associations.isEmpty()) {
            writer.add(eventFactory.createStartElement("", "", "Associations"));

            for (AssociationRule a : state.associations) {
                writer.add(eventFactory.createStartElement("", "", "Association"));
                writer.add(eventFactory.createAttribute("Support", "" + a.getSupport()));
                writer.add(eventFactory.createAttribute("Confidence", "" + a.getConfidence()));

                writer.add(eventFactory.createStartElement("", "", "Premise"));
                for (String premise : a.getPremise()) {
                    writer.add(eventFactory.createStartElement("", "", "HasAttribute"));
                    writer.add(eventFactory.createAttribute("AttributeIdentifier", "" + attrIds.get(premise)));
                    writer.add(eventFactory.createEndElement("", "", "HasAttribute"));
                }
                writer.add(eventFactory.createEndElement("", "", "Premise"));

                writer.add(eventFactory.createStartElement("", "", "Consequent"));
                for (String cons : a.getConsequent()) {
                    writer.add(eventFactory.createStartElement("", "", "HasAttribute"));
                    writer.add(eventFactory.createAttribute("AttributeIdentifier", "" + attrIds.get(cons)));
                    writer.add(eventFactory.createEndElement("", "", "HasAttribute"));
                }
                writer.add(eventFactory.createEndElement("", "", "Consequent"));
                writer.add(eventFactory.createEndElement("", "", "Association"));
            }
            writer.add(eventFactory.createEndElement("", "", "Associations"));
        }
    }

    private void addGUIState(XMLEventWriter writer) throws XMLStreamException {
        writer.add(eventFactory.createStartElement("", "", "Settings"));

        for (Field f : state.guiConf.getClass().getDeclaredFields()) {
            if (f.getName().equals("columnWidths")) {
                addColumnWidths(writer);
                continue;
            }
            try {
                writer.add(eventFactory.createStartElement("", "", f.getName()));
                writer.add(eventFactory.createCharacters("" + f.get(state.guiConf)));
                writer.add(eventFactory.createEndElement("", "", f.getName()));

            } catch (IllegalArgumentException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        writer.add(eventFactory.createEndElement("", "", "Settings"));
    }

    private void addColumnWidths(XMLEventWriter writer) throws XMLStreamException {
        writer.add(eventFactory.createStartElement("", "", "ColumnWidths"));

        for (int column : state.guiConf.columnWidths.keySet()) {
            writer.add(eventFactory.createStartElement("", "", "Column"));
            writer.add(eventFactory.createAttribute("Number", "" + column));
            writer.add(eventFactory.createStartElement("", "", "Width"));

            writer.add(eventFactory.createCharacters("" + state.guiConf.columnWidths.get(column)));

            writer.add(eventFactory.createEndElement("", "", "Width"));
            writer.add(eventFactory.createEndElement("", "", "Column"));
        }
        writer.add(eventFactory.createEndElement("", "", "ColumnWidths"));

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

            attrIds.put(state.context.getAttributeAtIndex(i), "" + i);
            writer.add(eventFactory.createCharacters(state.context.getAttributeAtIndex(i)));

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
            for (String attr : state.context.getAttributesForObject(obj.getIdentifier())) {
                writer.add(eventFactory.createStartElement("", "", "HasAttribute"));
                writer.add(eventFactory.createAttribute("AttributeIdentifier", "" + attrIds.get(attr)));
                writer.add(eventFactory.createEndElement("", "", "HasAttribute"));
            }
            writer.add(eventFactory.createEndElement("", "", "Intent"));
            writer.add(eventFactory.createEndElement("", "", "Object"));
        }
        writer.add(eventFactory.createEndElement("", "", "Objects"));

    }

}