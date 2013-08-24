package fcatools.conexpng;

import java.util.HashMap;
import java.util.Map;

/**
 * This class stores all the states of the GUI-elements, which you want to save
 * in the cex-file. You can only save simple datatypes, if you want to store a
 * complex one, you havt to do this: 1. you have to provide a representing
 * toString()-method 2. a method to parse such a string and build an object of
 * that datastructure 3. modify the {@link CEXReader} and use this parse-method
 * in it
 *
 * @author DavidBormann
 *
 */
public class GUIConf {

    // general
    public int lastTab = 0;

    // Dependencies
    public double support = 0.5;
    public double confidence = 0.1;
    public boolean lexsorting = false;
    public int splitpanepos = 206;
    public int assoscrollpos = 0;
    public int implscrollpos = 0;
    public int dependenciessettingssplitpos = 170;

    // Contexteditor
    public Map<Integer, Integer> columnWidths = new HashMap<>();

    // Latticeview
    public int latticesettingssplitpos = 170;
    public boolean showObjectLabel = true;
    public boolean showAttributLabel = true;
    public boolean showEdges = true;
    public boolean idealHighlighting = false;

}
