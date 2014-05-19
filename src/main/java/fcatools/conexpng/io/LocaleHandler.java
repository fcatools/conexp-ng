package fcatools.conexpng.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.Properties;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

import javax.swing.JOptionPane;

import fcatools.conexpng.Main;

/**
 * Provides static methods to get/set current localization.
 * 
 * @author Torsten Casselt
 */
public class LocaleHandler {
    private static final String LOCALE_BUNDLE_NAME = "locale.locale";
    // get user settings path
    private static final String USER_SETTINGS_PATH = Main.getSettingsDirectory().getPath();
    private static final String USER_SETTINGS_FILE_PATH = USER_SETTINGS_PATH + "/options.prop";

	private static ResourceBundle SETTINGS_RESOURCE_BUNDLE;
	private static ResourceBundle LOCALE_RESOURCE_BUNDLE;

	/**
	 * Sets locale based on settings. If user settings are existent, take them;
	 * if not, restore defaults (en).
	 */
	public static void readLocale() {
        boolean localeDefault = false;
		File userSettings = new File(USER_SETTINGS_FILE_PATH);
		FileInputStream in = null;
		if (userSettings.exists()) {
			// read user settings and use them
			try {
				in = new FileInputStream(USER_SETTINGS_FILE_PATH);
				SETTINGS_RESOURCE_BUNDLE = new PropertyResourceBundle(in);
				in.close();
			} catch (IOException e) {
				JOptionPane.showMessageDialog(null, e.getMessage()
						+ " Fall back to defaults (en)!", "Error",
						JOptionPane.ERROR_MESSAGE);
				e.printStackTrace();
                localeDefault = true;
			}
            // check if locale is already set in settings file
            if (!SETTINGS_RESOURCE_BUNDLE.containsKey("locale")) {
                localeDefault = true;
            }
		} else {
			// fall back to defaults (en)
            localeDefault = true;
		}
        LOCALE_RESOURCE_BUNDLE = ResourceBundle.getBundle(LOCALE_BUNDLE_NAME, new Locale(localeDefault ? "en"
                : SETTINGS_RESOURCE_BUNDLE.getString("locale")));
	}

	/**
	 * Returns localized string.
	 * 
	 * @param key
	 *            key for string to localize
	 * @return localized string
	 */
	public static String getString(String key) {
		try {
			return LOCALE_RESOURCE_BUNDLE.getString(key);
		} catch (MissingResourceException e) {
			return '!' + key + '!';
		}
	}

	/**
	 * Sets localization in settings.
	 * 
	 * @param locale
	 *            localization to switch settings to
	 */
	public static void setLocale(String locale) {
		// create folder if non-existent
		File userFolder = new File(USER_SETTINGS_PATH);
		if (!userFolder.exists()) {
			userFolder.mkdir();
		}
		// read settings only if user settings file exists
		Properties props = new Properties();
		File userSettings = new File(USER_SETTINGS_FILE_PATH);
		if (userSettings.exists()) {
			FileInputStream in = null;
			try {
				in = new FileInputStream(USER_SETTINGS_FILE_PATH);
				props.load(in);
				in.close();
			} catch (IOException e) {
				JOptionPane.showMessageDialog(null, e.getMessage()
						+ " User settings not readable!", "Error",
						JOptionPane.ERROR_MESSAGE);
				e.printStackTrace();
			}
		}
		// set new localization only if it is not already set
		if (!props.containsKey("locale")
				|| !props.getProperty("locale").equals(locale)) {
			props.setProperty("locale", locale);
			FileOutputStream out = null;
			// create user settings file and store set localization in it
			try {
				out = new FileOutputStream(USER_SETTINGS_FILE_PATH);
			} catch (FileNotFoundException e) {
				JOptionPane.showMessageDialog(null, e.getMessage()
						+ " User settings not writable!", "Error",
						JOptionPane.ERROR_MESSAGE);
				e.printStackTrace();
			}
			try {
				props.store(out, null);
			} catch (IOException e) {
				JOptionPane.showMessageDialog(null, e.getMessage()
						+ " Locale settings could not be written to file!",
						"Error", JOptionPane.ERROR_MESSAGE);
				e.printStackTrace();
			}
		}
	}
}
