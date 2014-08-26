package fcatools.conexpng;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Rectangle;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

import javax.swing.JOptionPane;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;

import com.alee.laf.WebLookAndFeel;
import com.google.common.collect.Sets;

import de.tudresden.inf.tcs.fcaapi.exception.IllegalObjectException;
import de.tudresden.inf.tcs.fcalib.FullObject;
import fcatools.conexpng.gui.MainFrame;
import fcatools.conexpng.gui.lattice.LatticeGraph;
import fcatools.conexpng.gui.lattice.LatticeGraphComputer;
import fcatools.conexpng.io.CEXReader;
import fcatools.conexpng.io.locale.LocaleHandler;
import fcatools.conexpng.model.FormalContext;

/**
 * 
 * The code for (re)storing the window location is from: <href
 * a="http://stackoverflow.com/a/7778332/283607">What is the best practice for
 * setting JFrame locations in Java?</a>
 * 
 */
public class Main {

    public static final String settingsDirName = ".conexp-ng";
    public static final String optionsFileName = new File(getSettingsDirectory(), "options.prop").getPath();
    private static Rectangle r;
    private static Exception exception = null;
    private static boolean fileOpened = false;

    public Main() {
        System.setProperty("user.language", LocaleHandler.readLocale());

        WebLookAndFeel.install();

        // Disable border around focused cells as it does not fit into the
        // context editor concept
        UIManager.put("Table.focusCellHighlightBorder", new EmptyBorder(0, 0, 0, 0));
        // Disable changing foreground color of cells as it does not fit into
        // the context editor concept
        UIManager.put("Table.focusCellForeground", Color.black);

        final Conf state = new Conf();

        // initialize lattice algorithms
        LatticeGraphComputer.init();

        boolean firstStart = false;
        File optionsFile = new File(optionsFileName);
        if (optionsFile.exists()) {
            try {
                restoreOptions(state);
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        } else {
            showExample(state);
            state.filePath = System.getProperty("user.home") + System.getProperty("file.separator") + "untitled.cex";
            firstStart = true;
        }

        // Create main window and take care of correctly saving and restoring
        // the last window location
        final MainFrame f = new MainFrame(state);
        f.setTitle("ConExp-NG - \"" + state.filePath + "\"");
        f.setMinimumSize(new Dimension(1000, 660));
        if (exception != null) {
            Util.handleIOExceptions(f, exception, state.filePath);
        }
        if (firstStart) {
            f.setSize(1000, 660);
            f.setLocationByPlatform(true);
            state.contextChanged();
        } else
            f.setBounds(r);
        f.setVisible(true);

        // Force various GUI components to update
        if (fileOpened)
            state.loadedFile();
        else
            state.contextChanged();
        state.saveConf();
    }

    public static void main(String... args) {
        new Main();
    }

    private void showExample(Conf state) {
        FormalContext context = new FormalContext();
        context.addAttribute("female");
        context.addAttribute("juvenile");
        context.addAttribute("adult");
        context.addAttribute("male");
        try {
            context.addObject(new FullObject<>("girl", Sets.newHashSet("female", "juvenile")));
            context.addObject(new FullObject<>("woman", Sets.newHashSet("female", "adult")));
            context.addObject(new FullObject<>("boy", Sets.newHashSet("male", "juvenile")));
            context.addObject(new FullObject<>("man", Sets.newHashSet("male", "adult")));
        } catch (IllegalObjectException e1) {
            e1.printStackTrace();
        }
        state.newContext(context);
        state.lattice = new LatticeGraph();
    }

    // Store settings: location & size of UI, dir that was last opened from,
    // language
    public static void storeOptions(Frame f, Conf state) {
        Properties p = new Properties();
        // restore the frame from 'full screen' first!
        f.setExtendedState(Frame.NORMAL);
        Rectangle r = f.getBounds();
        int x = (int) r.getX();
        int y = (int) r.getY();
        int w = (int) r.getWidth();
        int h = (int) r.getHeight();

        p.setProperty("x", "" + x);
        p.setProperty("y", "" + y);
        p.setProperty("w", "" + w);
        p.setProperty("h", "" + h);
        if (new File(state.filePath).exists())
            p.setProperty("lastOpened", state.filePath);
        else if (!state.lastOpened.isEmpty())
            p.setProperty("lastOpened", state.lastOpened.firstElement());
        else
            p.setProperty("lastOpened", "");
        for (int i = 0; i < 5; i++) {
            if (state.lastOpened.size() > i)
                p.setProperty("lastOpened" + i, state.lastOpened.get(i));
            else
                p.setProperty("lastOpened" + i, "");
        }
        // store selected language
        p.setProperty("locale", LocaleHandler.getSelectedLanguage());
        FileOutputStream out = null;
        // open stream to settings file
        try {
            out = new FileOutputStream(optionsFileName);
        } catch (FileNotFoundException e) {
            JOptionPane.showMessageDialog(null,
                    e.getMessage() + LocaleHandler.getString("Main.storeOptions.streamError"),
                    LocaleHandler.getString("error"),
                    JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
        // store settings in file
        try {
            p.store(out, "Settings");
            out.close();
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null,
                    e.getMessage() + LocaleHandler.getString("Main.storeOptions.storeError"),
                    LocaleHandler.getString("error"), JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    // Restore location & size of UI & dir that was last opened from
    private void restoreOptions(Conf state) throws IOException {
        File file = new File(optionsFileName);
        Properties p = new Properties();
        BufferedReader br = new BufferedReader(new FileReader(file));
        p.load(br);
        br.close();

        int x = Integer.parseInt(p.getProperty("x"));
        int y = Integer.parseInt(p.getProperty("y"));
        int w = Integer.parseInt(p.getProperty("w"));
        int h = Integer.parseInt(p.getProperty("h"));
        String lastOpened = p.getProperty("lastOpened");
        if (p.containsKey("lastOpened0")) {
            for (int i = 0; i < 5; i++) {
                if (p.getProperty("lastOpened" + i).isEmpty())
                    break;
                state.lastOpened.add(p.getProperty("lastOpened" + i));
            }
        }
        if (new File(lastOpened).isFile()) {
            state.filePath = lastOpened;
            try {
                new CEXReader(state, state.filePath);
                fileOpened = true;
            } catch (Exception e) {
                exception = e;
            }
        } else {
            showExample(state);
            state.filePath = System.getProperty("user.home") + System.getProperty("file.separator") + "untitled.cex";
        }
        r = new Rectangle(x, y, w, h);
    }

    // http://stackoverflow.com/a/193987/283607
    public static File getSettingsDirectory() {
        String userHome = System.getProperty("user.home");
        if (userHome == null) {
            throw new IllegalStateException("user.home==null");
        }
        File home = new File(userHome);
        File settingsDirectory = new File(home, settingsDirName);
        if (!settingsDirectory.exists()) {
            if (!settingsDirectory.mkdir()) {
                throw new IllegalStateException(settingsDirectory.toString());
            }
        }
        return settingsDirectory;
    }

}
