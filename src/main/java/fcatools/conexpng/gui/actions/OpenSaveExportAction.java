package fcatools.conexpng.gui.actions;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;

import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.JOptionPane;
import javax.xml.stream.XMLStreamException;

import org.apache.batik.dom.GenericDOMImplementation;
import org.apache.batik.dom.svg.SVGDOMImplementation;
import org.apache.batik.svggen.SVGGraphics2D;
import org.apache.batik.transcoder.Transcoder;
import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.fop.svg.PDFTranscoder;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;

import com.alee.extended.filefilter.AbstractFileFilter;
import com.alee.laf.filechooser.WebFileChooser;

import de.tudresden.inf.tcs.fcaapi.exception.IllegalObjectException;
import fcatools.conexpng.Conf;
import fcatools.conexpng.Util;
import fcatools.conexpng.gui.MainFrame;
import fcatools.conexpng.gui.MainFrame.StillCalculatingDialog;
import fcatools.conexpng.gui.MainFrame.UnsavedChangesDialog;
import fcatools.conexpng.gui.MainToolbar;
import fcatools.conexpng.gui.lattice.LatticeGraphView;
import fcatools.conexpng.gui.lattice.LatticeView;
import fcatools.conexpng.gui.lattice.Node;
import fcatools.conexpng.io.CEXReader;
import fcatools.conexpng.io.CEXWriter;
import fcatools.conexpng.io.CSVReader;
import fcatools.conexpng.io.CSVWriter;
import fcatools.conexpng.io.CXTReader;
import fcatools.conexpng.io.CXTWriter;
import fcatools.conexpng.io.OALReader;
import fcatools.conexpng.io.OALWriter;
import fcatools.conexpng.io.locale.LocaleHandler;

public class OpenSaveExportAction extends AbstractAction {

    /**
     * 
     */
    private static final long serialVersionUID = -4420979019496416898L;

    private enum DialogType {
        OPEN, SAVE, SAVEAS, EXPORT
    };

    private MainFrame mainFrame;
    private Conf state;
    private LatticeGraphView latticeGraphView;
    private DialogType type;

    /**
     * Creates an object for an open/save dialog.
     * 
     * @param mainFrame
     *            to attach dialogs to
     * @param state
     *            to fetch context from
     * @param openDialog
     *            true if it is an open dialog, false if not
     * @param saveAs
     *            if openDialog is false, specify if save dialog is of type save
     *            as
     */
    public OpenSaveExportAction(MainFrame mainFrame, Conf state, boolean openDialog, boolean saveAs) {
        this.mainFrame = mainFrame;
        this.state = state;
        if (openDialog) {
            this.type = DialogType.OPEN;
        } else if (saveAs) {
            this.type = DialogType.SAVEAS;
        } else {
            this.type = DialogType.SAVE;
        }
    }

    /**
     * Creates an object for an export dialog.
     * 
     * @param mainFrame
     *            to attach dialogs to
     * @param state
     *            to fetch lattice and layout from
     * @param latticeGraphView
     *            lattice graph view to paint the image for export
     */
    public OpenSaveExportAction(MainFrame mainFrame, Conf state, LatticeGraphView latticeGraphView) {
        this.mainFrame = mainFrame;
        this.state = state;
        this.latticeGraphView = latticeGraphView;
        this.type = DialogType.EXPORT;
    }

    @Override
    public void actionPerformed(ActionEvent ae) {
        // check if calculations still happening, show dialog to warn user
        if (!type.equals(DialogType.EXPORT) && !state.canBeSaved()) {
            StillCalculatingDialog scd = mainFrame.new StillCalculatingDialog();
            if (scd.isYes()) {
                return;
            }
        }
        // check if there are unsaved changes, show dialog to warn user
        if (type.equals(DialogType.OPEN) && state.unsavedChanges) {
            UnsavedChangesDialog ucd = mainFrame.new UnsavedChangesDialog();
            if (ucd.isYes()) {
                new OpenSaveExportAction(mainFrame, state, false, false).actionPerformed(ae);
            } else if (ucd.isCancel()) {
                return;
            }
        }
        // save immediately without file chooser if file is already known
        if (type.equals(DialogType.SAVE) && !state.filePath.endsWith("untitled.cex")) {
            saveContext(state.filePath, null);
            return;
        }
        // create file chooser, override approveSelection to ensure that an existing file is not overwritten
        final WebFileChooser fc = new WebFileChooser() {
            private static final long serialVersionUID = -1884427254565909222L;

            @Override
            public void approveSelection() {
                File f = getSelectedFile();
                // add extension in case no suitable extension already exists
                String fName = f.getAbsolutePath();
                if (!FileFilters.hasSupportedExtension(fName)) {
                    AbstractFileFilter activeFileFilter = getActiveFileFilter();
                    if (activeFileFilter.equals(FileFilters.cexFilter)) {
                        f = new File(fName.concat(".cex"));
                    } else if (activeFileFilter.equals(FileFilters.csvFilter)) {
                        f = new File(fName.concat(".csv"));
                    } else if (activeFileFilter.equals(FileFilters.cxtFilter)) {
                        f = new File(fName.concat(".cxt"));
                    } else if (activeFileFilter.equals(FileFilters.jpgFilter)) {
                        f = new File(fName.concat(".jpg"));
                    } else if (activeFileFilter.equals(FileFilters.oalFilter)) {
                        f = new File(fName.concat(".oal"));
                    } else if (activeFileFilter.equals(FileFilters.pdfFilter)) {
                        f = new File(fName.concat(".pdf"));
                    } else if (activeFileFilter.equals(FileFilters.pngFilter)) {
                        f = new File(fName.concat(".png"));
                    } else if (activeFileFilter.equals(FileFilters.svgFilter)) {
                        f = new File(fName.concat(".svg"));
                    }
                    setSelectedFile(f);
                }
                if (f.exists() && getDialogType() == SAVE_DIALOG) {
                    int result = JOptionPane.showConfirmDialog(this,
                            LocaleHandler.getString("MainFrame.OverwritingFileDialog.optionPane") + f.getName()
                            + "?", LocaleHandler.getString("MainFrame.OverwritingFileDialog.title"), JOptionPane.YES_NO_CANCEL_OPTION);
                    switch(result){
                        case JOptionPane.YES_OPTION:
                            super.approveSelection();
                            return;
                        case JOptionPane.NO_OPTION:
                            return;
                        case JOptionPane.CLOSED_OPTION:
                            return;
                        case JOptionPane.CANCEL_OPTION:
                            cancelSelection();
                            return;
                    }
                }
                super.approveSelection();
            }        
        };
        fc.setCurrentDirectory(state.filePath.substring(0,
                state.filePath.lastIndexOf(System.getProperty("file.separator"))));
        // set dialog type
        if (type.equals(DialogType.OPEN)) {
            fc.setDialogType(WebFileChooser.OPEN_DIALOG);
        } else {
            fc.setDialogType(WebFileChooser.SAVE_DIALOG);
        }
        fc.setMultiSelectionEnabled(false);
        fc.setAcceptAllFileFilterUsed(false);
        fc.setFileSelectionMode(WebFileChooser.FILES_ONLY);
        // add file filters
        if (type.equals(DialogType.EXPORT)) {
            fc.addChoosableFileFilter(FileFilters.svgFilter);
            fc.addChoosableFileFilter(FileFilters.pdfFilter);
            fc.addChoosableFileFilter(FileFilters.pngFilter);
            fc.addChoosableFileFilter(FileFilters.jpgFilter);
            // set default file filter
            fc.setFileFilter(FileFilters.svgFilter);
        } else {
            fc.addChoosableFileFilter(FileFilters.cexFilter);
            fc.addChoosableFileFilter(FileFilters.csvFilter);
            fc.addChoosableFileFilter(FileFilters.cxtFilter);
            fc.addChoosableFileFilter(FileFilters.oalFilter);
            // set default file filter
            fc.setFileFilter(FileFilters.cexFilter);
        }
        // show file chooser
        int fcRet;
        if (type.equals(DialogType.OPEN)) {
            fcRet = fc.showOpenDialog(mainFrame);
        } else {
            fcRet = fc.showSaveDialog(mainFrame);
        }
        if (fcRet == WebFileChooser.APPROVE_OPTION) {
            handleFile(fc.getSelectedFile(), fc.getActiveFileFilter());
        }
    }

    /**
     * Open or save file.
     * 
     * @param selectedFile
     *            file that was selected in the file chooser
     * @param activeFileFilter
     *            selected file filter to determine desired file type if no
     *            extension is specified
     */
    private void handleFile(File selectedFile, AbstractFileFilter activeFileFilter) {
        String path = selectedFile.getAbsolutePath();
        if (type.equals(DialogType.OPEN)) {
            openContext(mainFrame, state, path);
        } else if (type.equals(DialogType.EXPORT)) {
            exportLattice(path, activeFileFilter);
        } else {
            saveContext(path, activeFileFilter);
        }
    }

    /**
     * Open a context file.
     * 
     * @param mainFrame
     *            main frame for showing error dialogs, given to this method to
     *            keep it static
     * @param state
     *            to save context in
     * @param path
     *            to read from
     */
    public static void openContext(MainFrame mainFrame, Conf state, String path) {
        // open context
        try {
            if (path.endsWith(".cex")) {
                new CEXReader(state, path);
            } else if (path.endsWith(".csv")) {
                new CSVReader(state, path);
            } else if (path.endsWith(".cxt")) {
                new CXTReader(state, path);
            } else if (path.endsWith(".oal")) {
                new OALReader(state, path);
            }
        } catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException
                | SecurityException | XMLStreamException | IllegalObjectException | IOException e) {
            Util.handleIOExceptions(mainFrame, e, path, Util.FileOperationType.OPEN);
        }
    }

    /**
     * Exports the lattice to file.
     * 
     * @param path
     *            to export lattice to
     * @param activeFileFilter
     *            selected file filter to determine desired file type if no
     *            extension is specified
     */
    private void exportLattice(String path, AbstractFileFilter activeFileFilter) {
        // export lattice
        if (path.endsWith(".jpg") || path.endsWith(".jpeg") || path.endsWith(".png")) {
            exportLatticeAsPixelGraphic(path);
        } else if (path.endsWith(".svg") || path.endsWith(".pdf")) {
            exportLatticeAsVectorGraphic(path);
        }
    }


    /**
     * Exports the lattice as a pixel graphic.
     * 
     * @param path
     *            to save image to
     */
    private void exportLatticeAsPixelGraphic(String path) {
        try {
            Dimension d = latticeGraphView.getSize();
            // using TYPE_3BYTE_BGR as workaround for OpenJDK, don't
            // change it
            BufferedImage bi = new BufferedImage(d.width, d.height, BufferedImage.TYPE_3BYTE_BGR);
            Graphics2D g = bi.createGraphics();
            latticeGraphView.paint(g);
            ImageIO.write(bi, path.endsWith(".png") ? "PNG" : "JPG", new File(path));
        } catch (IOException e) {
            Util.handleIOExceptions(mainFrame, e, path, Util.FileOperationType.EXPORT);
        }
    }

    /**
     * Exports lattice as vector graphics.
     * 
     * @param path
     *            to save image to
     */
    private void exportLatticeAsVectorGraphic(String path) {
        try {
            DOMImplementation domImpl = GenericDOMImplementation.getDOMImplementation();
            String svgNS = SVGDOMImplementation.SVG_NAMESPACE_URI;
            Document document = domImpl.createDocument(svgNS, "svg", null);
            SVGGraphics2D svgGenerator = new SVGGraphics2D(document);
            latticeGraphView.paint(svgGenerator);
            if (path.endsWith(".pdf")) {
                // use temp file to store svg file
                File tmpFile = File.createTempFile("exported_lattice.svg", ".tmp");
                Writer out = new FileWriter(tmpFile);
                svgGenerator.stream(out, true);
                out.close();
                Dimension d = latticeGraphView.getSize();
                Transcoder transcoder = new PDFTranscoder();
                transcoder.addTranscodingHint(PDFTranscoder.KEY_WIDTH, new Float(d.width));
                transcoder.addTranscodingHint(PDFTranscoder.KEY_HEIGHT, new Float(d.height));
                transcoder.addTranscodingHint(PDFTranscoder.KEY_AOI, new Rectangle(d.width, d.height));
                TranscoderInput inputSvgImage = new TranscoderInput(new FileReader(tmpFile));
                OutputStream ostream = new FileOutputStream(path);
                TranscoderOutput outputFile = new TranscoderOutput(ostream);
                transcoder.transcode(inputSvgImage, outputFile);
                ostream.close();
                tmpFile.delete();
            } else {
                Writer out = new FileWriter(new File(path));
                svgGenerator.stream(out, true);
                out.close();
            }
        } catch (TranscoderException | IOException e) {
            Util.handleIOExceptions(mainFrame, e, path, Util.FileOperationType.EXPORT);
        }
    }

    /**
     * Calculates maximum width and height of lattice graph for pixel graphics.
     * Not used anymore because only the visible view port is exported.
     * 
     * @return dimension consisting of max width and height
     */
    @SuppressWarnings("unused")
    @Deprecated
    private Dimension calculateMaxDimension() {
        int maxWidth = 0;
        int maxHeight = 0;
        Point offset = LatticeGraphView.getOffset();
        double zoom = LatticeView.zoomFactor;
        // real width/height after applying offset and zoom factor
        int realWidth = 0;
        int realHeight = 0;
        for (Node n : state.lattice.getNodes()) {
            realWidth = (int) Math.ceil(n.getX() * zoom + offset.getX());
            if (maxWidth < n.getX()) {
                maxWidth = n.getX();
            }
            realHeight = (int) Math.ceil(n.getY() * zoom + offset.getY());
            if (maxHeight < realHeight) {
                maxHeight = realHeight;
            }
            if (state.guiConf.showAttributLabel) {
                realWidth = (int) Math.ceil(n.getAttributesLabel().getX() * zoom + offset.getX());
                if (maxWidth < realWidth) {
                    maxWidth = realWidth;
                }
                realHeight = (int) Math.ceil(n.getAttributesLabel().getY() * zoom + offset.getY());
                if (maxHeight < realHeight) {
                    maxHeight = realHeight;
                }
            }
            if (state.guiConf.showObjectLabel) {
                realWidth = (int) Math.ceil(n.getObjectsLabel().getX() * zoom + offset.getX());
                if (maxWidth < realWidth) {
                    maxWidth = realWidth;
                }
                realHeight = (int) Math.ceil(n.getObjectsLabel().getY() * zoom + offset.getY());
                if (maxHeight < realHeight) {
                    maxHeight = realHeight;
                }
            }
        }
        return new Dimension(maxWidth, maxHeight);
    }

    /**
     * Save the context to file.
     * 
     * @param path
     *            to save context to
     * @param activeFileFilter
     *            selected file filter to determine desired file type if no
     *            extension is specified
     */
    private void saveContext(String path, AbstractFileFilter activeFileFilter) {
        // save context
        try {
            if (path.endsWith(".cex")) {
                new CEXWriter(state, path);
            } else if (path.endsWith(".csv")) {
                new CSVWriter(state, path);
            } else if (path.endsWith(".cxt")) {
                new CXTWriter(state, path);
            } else if (path.endsWith(".oal")) {
                new OALWriter(state, path);
            }
            state.setNewFile(path);
            state.unsavedChanges = false;
            MainToolbar.getSaveButton().setEnabled(false);
        } catch (IOException | XMLStreamException e) {
            Util.handleIOExceptions(mainFrame, e, path, Util.FileOperationType.SAVE);
        }
    }
}
