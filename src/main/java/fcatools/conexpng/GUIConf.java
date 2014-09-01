package fcatools.conexpng;

import java.util.HashMap;
import java.util.Map;

import fcatools.conexpng.gui.MainFrame;
import fcatools.conexpng.io.GUIReader;

/**
 * This class stores the states of the GUI elements, which are saved in the GUI
 * file related to the context file. Each entry is set in the corresponding GUI
 * element on creation and (for loading GUI states at runtime) in
 * {@link MainFrame#updateGUI()} <br>
 * <br>
 * If a complex data type needs to be saved, there are several tasks to
 * complete: <br>
 * <br>
 * 1. Implement a representing toString()-method <br>
 * 2. Implement a method to parse such a string and build an object of the data
 * type <br>
 * 3. Modify the {@link GUIReader}: Add a case in the parse method
 * 
 * @author DavidBormann
 */
public class GUIConf {

    // general
    public int lastTab = 0;

    // dependencies
    public double support = 0.5;
    public double confidence = 0.1;
    public boolean lexSorting = false;
    public int assoImplSplitPanePos = 206;
    public int assoScrollPos = 0;
    public int implScrollPos = 0;
    public int dependenciesSettingsSplitPos = 170;

    // context
    public Map<Integer, Integer> columnWidths = new HashMap<>();
    public boolean compactMatrix = false;
    public boolean showArrowRelations = false;

    // lattice
    public int latticeSettingsSplitPos = 170;
    public boolean showObjectLabel = true;
    public boolean showAttributeLabel = true;
    public boolean showEdges = true;
    public boolean idealHighlighting = false;
    public double zoomFactor = 1.0;
    public double xOffset = 0.0;
    public double yOffset = 0.0;

}