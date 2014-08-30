package fcatools.conexpng.gui.actions;

import java.io.File;

import javax.swing.ImageIcon;

import com.alee.utils.filefilter.AbstractFileFilter;

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

    public static AbstractFileFilter cexFilter = new AbstractFileFilter() {

        @Override
        public boolean accept(File pathname) {
            return !pathname.isHidden() && !pathname.isDirectory() && pathname.getName().endsWith(".cex");
        }

        @Override
        public String getDescription() {
            return LocaleHandler.getString("FileFilters.cexFilter.getDescription");
        }

        @Override
        public ImageIcon getIcon() {
            // TODO Auto-generated method stub
            return null;
        }
    };

    public static AbstractFileFilter csvFilter = new AbstractFileFilter() {

        @Override
        public boolean accept(File pathname) {
            return !pathname.isHidden() && !pathname.isDirectory() && pathname.getName().endsWith(".csv");
        }

        @Override
        public String getDescription() {
            return LocaleHandler.getString("FileFilters.csvFilter.getDescription");
        }

        @Override
        public ImageIcon getIcon() {
            // TODO Auto-generated method stub
            return null;
        }
    };

    public static AbstractFileFilter cxtFilter = new AbstractFileFilter() {

        @Override
        public boolean accept(File pathname) {
            return !pathname.isHidden() && !pathname.isDirectory() && pathname.getName().endsWith(".cxt");
        }

        @Override
        public String getDescription() {
            return LocaleHandler.getString("FileFilters.cxtFilter.getDescription");
        }

        @Override
        public ImageIcon getIcon() {
            // TODO Auto-generated method stub
            return null;
        }
    };

    public static AbstractFileFilter jpgFilter = new AbstractFileFilter() {

        @Override
        public boolean accept(File pathname) {
            return !pathname.isHidden() && !pathname.isDirectory()
                    && (pathname.getName().endsWith(".jpg") || pathname.getName().endsWith(".jpeg"));
        }

        @Override
        public String getDescription() {
            return LocaleHandler.getString("FileFilters.jpgFilter.getDescription");
        }

        @Override
        public ImageIcon getIcon() {
            // TODO Auto-generated method stub
            return null;
        }
    };

    public static AbstractFileFilter oalFilter = new AbstractFileFilter() {

        @Override
        public boolean accept(File pathname) {
            return !pathname.isHidden() && !pathname.isDirectory() && pathname.getName().endsWith(".oal");
        }

        @Override
        public String getDescription() {
            return LocaleHandler.getString("FileFilters.oalFilter.getDescription");
        }

        @Override
        public ImageIcon getIcon() {
            // TODO Auto-generated method stub
            return null;
        }
    };

    public static AbstractFileFilter pdfFilter = new AbstractFileFilter() {

        @Override
        public boolean accept(File pathname) {
            return !pathname.isHidden() && !pathname.isDirectory() && pathname.getName().endsWith(".pdf");
        }

        @Override
        public String getDescription() {
            return LocaleHandler.getString("FileFilters.pdfFilter.getDescription");
        }

        @Override
        public ImageIcon getIcon() {
            // TODO Auto-generated method stub
            return null;
        }
    };

    public static AbstractFileFilter pngFilter = new AbstractFileFilter() {

        @Override
        public boolean accept(File pathname) {
            return !pathname.isHidden() && !pathname.isDirectory() && pathname.getName().endsWith(".png");
        }

        @Override
        public String getDescription() {
            return LocaleHandler.getString("FileFilters.pngFilter.getDescription");
        }

        @Override
        public ImageIcon getIcon() {
            // TODO Auto-generated method stub
            return null;
        }
    };

    public static AbstractFileFilter svgFilter = new AbstractFileFilter() {

        @Override
        public boolean accept(File pathname) {
            return !pathname.isHidden() && !pathname.isDirectory() && pathname.getName().endsWith(".svg");
        }

        @Override
        public String getDescription() {
            return LocaleHandler.getString("FileFilters.svgFilter.getDescription");
        }

        @Override
        public ImageIcon getIcon() {
            // TODO Auto-generated method stub
            return null;
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
