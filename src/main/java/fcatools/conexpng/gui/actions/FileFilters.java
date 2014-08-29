package fcatools.conexpng.gui.actions;

import java.io.File;

import com.alee.extended.filefilter.FilesFilter;

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

    public static FilesFilter cexFilter = new FilesFilter() {

        @Override
        public boolean accept(File pathname) {
            return !pathname.isHidden() && !pathname.isDirectory() && pathname.getName().endsWith(".cex");
        }

        @Override
        public String getDescription() {
            return LocaleHandler.getString("FileFilters.cexFilter.getDescription");
        }
    };

    public static FilesFilter csvFilter = new FilesFilter() {

        @Override
        public boolean accept(File pathname) {
            return !pathname.isHidden() && !pathname.isDirectory() && pathname.getName().endsWith(".csv");
        }

        @Override
        public String getDescription() {
            return LocaleHandler.getString("FileFilters.csvFilter.getDescription");
        }
    };

    public static FilesFilter cxtFilter = new FilesFilter() {

        @Override
        public boolean accept(File pathname) {
            return !pathname.isHidden() && !pathname.isDirectory() && pathname.getName().endsWith(".cxt");
        }

        @Override
        public String getDescription() {
            return LocaleHandler.getString("FileFilters.cxtFilter.getDescription");
        }
    };

    public static FilesFilter jpgFilter = new FilesFilter() {

        @Override
        public boolean accept(File pathname) {
            return !pathname.isHidden() && !pathname.isDirectory()
                    && (pathname.getName().endsWith(".jpg") || pathname.getName().endsWith(".jpeg"));
        }

        @Override
        public String getDescription() {
            return LocaleHandler.getString("FileFilters.jpgFilter.getDescription");
        }
    };

    public static FilesFilter oalFilter = new FilesFilter() {

        @Override
        public boolean accept(File pathname) {
            return !pathname.isHidden() && !pathname.isDirectory() && pathname.getName().endsWith(".oal");
        }

        @Override
        public String getDescription() {
            return LocaleHandler.getString("FileFilters.oalFilter.getDescription");
        }
    };

    public static FilesFilter pdfFilter = new FilesFilter() {

        @Override
        public boolean accept(File pathname) {
            return !pathname.isHidden() && !pathname.isDirectory() && pathname.getName().endsWith(".pdf");
        }

        @Override
        public String getDescription() {
            return LocaleHandler.getString("FileFilters.pdfFilter.getDescription");
        }
    };

    public static FilesFilter pngFilter = new FilesFilter() {

        @Override
        public boolean accept(File pathname) {
            return !pathname.isHidden() && !pathname.isDirectory() && pathname.getName().endsWith(".png");
        }

        @Override
        public String getDescription() {
            return LocaleHandler.getString("FileFilters.pngFilter.getDescription");
        }
    };

    public static FilesFilter svgFilter = new FilesFilter() {

        @Override
        public boolean accept(File pathname) {
            return !pathname.isHidden() && !pathname.isDirectory() && pathname.getName().endsWith(".svg");
        }

        @Override
        public String getDescription() {
            return LocaleHandler.getString("FileFilters.svgFilter.getDescription");
        }
    };

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
