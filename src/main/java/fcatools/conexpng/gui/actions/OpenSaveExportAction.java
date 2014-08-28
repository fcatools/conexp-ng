package fcatools.conexpng.gui.actions;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.OutputStream;
import java.io.Writer;

import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;

import org.apache.batik.dom.GenericDOMImplementation;
import org.apache.batik.dom.svg.SVGDOMImplementation;
import org.apache.batik.svggen.SVGGraphics2D;
import org.apache.batik.transcoder.Transcoder;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.fop.svg.PDFTranscoder;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;

import com.alee.laf.filechooser.WebFileChooser;

import fcatools.conexpng.Conf;
import fcatools.conexpng.Util;
import fcatools.conexpng.gui.MainFrame;
import fcatools.conexpng.gui.MainFrame.StillCalculatingDialog;
import fcatools.conexpng.gui.MainFrame.UnsavedChangesDialog;
import fcatools.conexpng.gui.MainToolbar;
import fcatools.conexpng.gui.lattice.LatticeGraphView;
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
            // saveFile(MainToolbar.this.state.filePath);
        }
        // create file chooser, override approveSelection to ensure that an existing file is not overwritten
        final WebFileChooser fc = new WebFileChooser() {
            private static final long serialVersionUID = -1884427254565909222L;

            @Override
            public void approveSelection() {
                File f = getSelectedFile();
                // check if file with added cex/svg extension exists in case an extension is added automatically (no suitable extension existing)
                String fName = f.getAbsolutePath();
                if (!FileFilters.hasSupportedExtension(fName)) {
                    for (FileFilter ff : getChoosableFileFilters()) {
                        if (ff.equals(FileFilters.cexFilter)) {
                            f = new File(fName.concat(".cex"));
                            break;
                        } else if (ff.equals(FileFilters.svgFilter)) {
                            f = new File(fName.concat(".svg"));
                            break;
                        }
                    }
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
        } else {
            fc.addChoosableFileFilter(FileFilters.cexFilter);
            fc.addChoosableFileFilter(FileFilters.csvFilter);
            fc.addChoosableFileFilter(FileFilters.cxtFilter);
            fc.addChoosableFileFilter(FileFilters.oalFilter);
        }
        // show file chooser
        int fcRet;
        if (type.equals(DialogType.OPEN)) {
            fcRet = fc.showOpenDialog(mainFrame);
        } else {
            fcRet = fc.showSaveDialog(mainFrame);
        }
        if (fcRet == WebFileChooser.APPROVE_OPTION) {
            handleFile(fc.getSelectedFile());
        }
    }

    /**
     * Open or save file.
     * 
     * @param selectedFile
     *            file that was selected in the file chooser
     */
    private void handleFile(File selectedFile) {
        String path = selectedFile.getAbsolutePath();
        try {
            if (type.equals(DialogType.OPEN)) {
                // open context
                if (path.endsWith(".cex")) {
                    new CEXReader(state, path);
                } else if (path.endsWith(".csv")) {
                    new CSVReader(state, path);
                } else if (path.endsWith(".cxt")) {
                    new CXTReader(state, path);
                } else if (path.endsWith(".oal")) {
                    new OALReader(state, path);
                }
            } else if (type.equals(DialogType.EXPORT)) {
                // export lattice
                Dimension d = calculateMaxDimension();
                Rectangle r = new Rectangle(d.width + 30, d.height + 30);
                if (path.endsWith(".jpg") || path.endsWith(".jpeg") || path.endsWith(".png")) {
                    // using TYPE_3BYTE_BGR as workaround for OpenJDK, don't
                    // change it
                    BufferedImage bi = new BufferedImage(r.width, r.height, BufferedImage.TYPE_3BYTE_BGR);
                    Graphics2D g = bi.createGraphics();
                    latticeGraphView.paint(g);
                    ImageIO.write(bi, path.endsWith(".png") ? "PNG" : "JPG", new File(path));
                } else {
                    DOMImplementation domImpl = GenericDOMImplementation.getDOMImplementation();
                    String svgNS = SVGDOMImplementation.SVG_NAMESPACE_URI;
                    Document document = domImpl.createDocument(svgNS, "svg", null);
                    SVGGraphics2D svgGenerator = new SVGGraphics2D(document);
                    latticeGraphView.paint(svgGenerator);
                    Transcoder transcoder = null;
                    if (path.endsWith(".pdf")) {
                        transcoder = new PDFTranscoder();
                        transcoder.addTranscodingHint(PDFTranscoder.KEY_WIDTH, new Float(r.width));
                        transcoder.addTranscodingHint(PDFTranscoder.KEY_HEIGHT, new Float(r.height));
                        transcoder.addTranscodingHint(PDFTranscoder.KEY_AOI, r);
                        // use temp file to store svg file
                        File tmpFile = File.createTempFile("exported_lattice.svg", ".tmp");
                        Writer tmpOut = new FileWriter(tmpFile);
                        svgGenerator.stream(tmpOut, true);
                        tmpOut.close();
                        TranscoderInput inputSvgImage = new TranscoderInput(new FileReader(tmpFile));
                        OutputStream ostream = new FileOutputStream(path);
                        TranscoderOutput outputFile = new TranscoderOutput(ostream);
                        transcoder.transcode(inputSvgImage, outputFile);
                        ostream.close();
                        tmpFile.delete();
                    } else {
                        // save as svg, concat svg extension if not already there
                        if (!path.endsWith(".svg")) {
                            path = path.concat(".svg");
                        }
                        Writer out = new FileWriter(new File(path));
                        svgGenerator.stream(out, true);
                        out.close();
                    }
                }
            } else {
                // save context
                if (path.endsWith(".csv")) {
                    new CSVWriter(state, path);
                } else if (path.endsWith(".cxt")) {
                    new CXTWriter(state, path);
                } else if (path.endsWith(".oal")) {
                    new OALWriter(state, path);
                } else {
                    // save context as cex if no extension is specified
                    if (!path.endsWith(".cex")) {
                        path = path.concat(".cex");
                    }
                    new CEXWriter(state, path);
                }
                state.setNewFile(path);
                state.unsavedChanges = false;
                MainToolbar.getSaveButton().setEnabled(false);
            }
        } catch (Exception e) {
            Util.handleIOExceptions(mainFrame, e, path);
        }
    }

    /**
     * Calculates maximum width and height of lattice graph for pixel graphics.
     * 
     * @return dimension consisting of max width and height
     */
    private Dimension calculateMaxDimension() {
        int maxWidth = 0;
        int maxHeight = 0;
        for (Node n : state.lattice.getNodes()) {
            if (maxWidth < n.getX()) {
                maxWidth = n.getX();
            }
            if (maxHeight < n.getY()) {
                maxHeight = n.getY();
            }
            if (state.guiConf.showAttributLabel) {
                if (maxWidth < n.getAttributesLabel().getX()) {
                    maxWidth = n.getAttributesLabel().getX();
                }
                if (maxHeight < n.getAttributesLabel().getY()) {
                    maxHeight = n.getAttributesLabel().getY();
                }
            }
            if (state.guiConf.showObjectLabel) {
                if (maxWidth < n.getObjectsLabel().getX()) {
                    maxWidth = n.getObjectsLabel().getX();
                }
                if (maxHeight < n.getObjectsLabel().getY()) {
                    maxHeight = n.getObjectsLabel().getY();
                }
            }
        }
        return new Dimension(maxWidth, maxHeight);
    }
}
