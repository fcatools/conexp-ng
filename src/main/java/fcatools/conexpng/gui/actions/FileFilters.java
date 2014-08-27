package fcatools.conexpng.gui.actions;

import javax.swing.filechooser.FileNameExtensionFilter;

import fcatools.conexpng.io.locale.LocaleHandler;

/**
 * This class encapsulates the available file types in dialogs.
 * 
 * @author Torsten Casselt
 */
public class FileFilters {

    public static enum SupportedFileFormat {
        cex, csv, cxt, jpg, jpeg, oal, pdf, png, svg
    };

    public static FileNameExtensionFilter cexFilter = new FileNameExtensionFilter(
            LocaleHandler.getString("FileFilters.cexFilter.getDescription"), "cex");
    public static FileNameExtensionFilter csvFilter = new FileNameExtensionFilter(
            LocaleHandler.getString("FileFilters.csvFilter.getDescription"), "csv");
    public static FileNameExtensionFilter cxtFilter = new FileNameExtensionFilter(
            LocaleHandler.getString("FileFilters.cxtFilter.getDescription"), "cxt");
    public static FileNameExtensionFilter jpgFilter = new FileNameExtensionFilter(
            LocaleHandler.getString("FileFilters.jpgFilter.getDescription"), "jpg", "jpeg");
    public static FileNameExtensionFilter oalFilter = new FileNameExtensionFilter(
            LocaleHandler.getString("FileFilters.oalFilter.getDescription"), "oal");
    public static FileNameExtensionFilter pdfFilter = new FileNameExtensionFilter(
            LocaleHandler.getString("FileFilters.pdfFilter.getDescription"), "pdf");
    public static FileNameExtensionFilter pngFilter = new FileNameExtensionFilter(
            LocaleHandler.getString("FileFilters.pngFilter.getDescription"), "png");
    public static FileNameExtensionFilter svgFilter = new FileNameExtensionFilter(
            LocaleHandler.getString("FileFilters.svgFilter.getDescription"), "svg");

    /**
     * Checks if given file name has a supported extension.
     * 
     * @param fileName
     *            file name to check
     * @return true if file name has supported extension, false if not
     */
    public static boolean hasSupportedExtension(String fileName) {
        for (SupportedFileFormat sff : SupportedFileFormat.values()) {
            if (fileName.endsWith(sff.toString())) {
                return true;
            }
        }
        return false;
    }
}
