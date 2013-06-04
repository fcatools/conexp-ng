package com.eugenkiss.conexp2;

import com.eugenkiss.conexp2.gui.MainFrame;
import com.eugenkiss.conexp2.model.FormalContext;
import com.google.common.collect.Sets;
import de.tudresden.inf.tcs.fcaapi.exception.IllegalObjectException;
import de.tudresden.inf.tcs.fcalib.FullObject;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.*;
import java.util.Properties;

/**
 *
 * The code for (re)storing the window location is from:
 * <href a="http://stackoverflow.com/a/7778332/283607">What is the best practice for setting JFrame locations in Java?</a>
 *
 */
public class Main {

    public static final String optionsFileName = new File(getSettingsDirectory(), "options.prop").getPath();



    public static void main(String... args) {
        // Set System L&F
        try {
            UIManager.setLookAndFeel(
                    UIManager.getSystemLookAndFeelClassName());
        }
        catch (UnsupportedLookAndFeelException e) {
        }
        catch (ClassNotFoundException e) {
        }
        catch (InstantiationException e) {
        }
        catch (IllegalAccessException e) {
        }

        // Disable border around focused cells as it does not fit into the context editor concept
        UIManager.put("Table.focusCellHighlightBorder", new EmptyBorder(0,0,0,0));
        // Disable changing foreground color of cells as it does not fit into the context editor concept
        UIManager.put("Table.focusCellForeground", Color.black);

        ProgramState testState = new ProgramState();
        testState.filePath = "../example.cex";
        testState.context = new FormalContext();
        testState.context.addAttribute("female");
        testState.context.addAttribute("juvenile");
        testState.context.addAttribute("adult");
        testState.context.addAttribute("male");
        try {
            testState.context.addObject(new FullObject<>("girl", Sets.newHashSet("female", "juvenile")));
            testState.context.addObject(new FullObject<>("woman", Sets.newHashSet("female", "adult")));
            testState.context.addObject(new FullObject<>("boy", Sets.newHashSet("male", "juvenile")));
            testState.context.addObject(new FullObject<>("man", Sets.newHashSet("male", "adult")));
        } catch (IllegalObjectException e1) {
            e1.printStackTrace();
        }

        final JFrame f = new MainFrame(testState);
        f.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        f.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent we) {
                try {
                    storeOptions(f);
                } catch(Exception e) {
                    e.printStackTrace();
                }
                System.exit(0);
            }
        });

        File optionsFile = new File(optionsFileName);
        if (optionsFile.exists()) {
            try {
                restoreOptions(f);
            } catch(IOException ioe) {
                ioe.printStackTrace();
            }
        } else {
            f.setLocationByPlatform(true);
        }
        f.setVisible(true);
    }


    /** Store location & size of UI */
    private static void storeOptions(Frame f) throws Exception {
        File file = new File(optionsFileName);
        Properties p = new Properties();
        // restore the frame from 'full screen' first!
        f.setExtendedState(Frame.NORMAL);
        Rectangle r = f.getBounds();
        int x = (int)r.getX();
        int y = (int)r.getY();
        int w = (int)r.getWidth();
        int h = (int)r.getHeight();

        p.setProperty("x", "" + x);
        p.setProperty("y", "" + y);
        p.setProperty("w", "" + w);
        p.setProperty("h", "" + h);

        BufferedWriter br = new BufferedWriter(new FileWriter(file));
        p.store(br, "Properties of the user frame");
    }

    /** Restore location & size of UI */
    private static void restoreOptions(Frame f) throws IOException {
        File file = new File(optionsFileName);
        Properties p = new Properties();
        BufferedReader br = new BufferedReader(new FileReader(file));
        p.load(br);

        int x = Integer.parseInt(p.getProperty("x"));
        int y = Integer.parseInt(p.getProperty("y"));
        int w = Integer.parseInt(p.getProperty("w"));
        int h = Integer.parseInt(p.getProperty("h"));

        Rectangle r = new Rectangle(x,y,w,h);

        f.setBounds(r);
    }

    // http://stackoverflow.com/a/193987/283607
    private static File getSettingsDirectory() {
        String userHome = System.getProperty("user.home");
        if(userHome == null) {
            throw new IllegalStateException("user.home==null");
        }
        File home = new File(userHome);
        File settingsDirectory = new File(home, ".conexp2");
        if(!settingsDirectory.exists()) {
            if(!settingsDirectory.mkdir()) {
                throw new IllegalStateException(settingsDirectory.toString());
            }
        }
        return settingsDirectory;
    }

}
