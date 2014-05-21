package fcatools.conexpng.io.locale;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Locale;
import java.util.MissingResourceException;
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

    private static String SELECTED_LANGUAGE = SupportedLanguages.values()[0].toString();

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
    public static String readLocale() {
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
 + " Fall back to default language!", "Error",
						JOptionPane.ERROR_MESSAGE);
				e.printStackTrace();
                localeDefault = true;
			}
            // check if locale is already set in settings file
            if (!SETTINGS_RESOURCE_BUNDLE.containsKey("locale")) {
                localeDefault = true;
            }
		} else {
            // fall back to defaults
            localeDefault = true;
		}
        if (localeDefault) {
            LOCALE_RESOURCE_BUNDLE = ResourceBundle.getBundle(LOCALE_BUNDLE_NAME, new Locale(getSelectedLanguage()));
        } else {
            SELECTED_LANGUAGE = SETTINGS_RESOURCE_BUNDLE.getString("locale");
            LOCALE_RESOURCE_BUNDLE = ResourceBundle.getBundle(LOCALE_BUNDLE_NAME,
                    new Locale(SETTINGS_RESOURCE_BUNDLE.getString("locale")));
        }
        return getSelectedLanguage();
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
     * Get selected language.
     */
    public static String getSelectedLanguage() {
        return SELECTED_LANGUAGE;
    }

    /**
     * Sets selected language.
     * 
     * @param locale
     *            language to switch to
     */
    public static void setSelectedLanguage(String locale) {
        if (SupportedLanguages.contains(locale)) {
            SELECTED_LANGUAGE = locale;
            JOptionPane.showMessageDialog(null, getString("LocaleHandler.setSelectedLanguage.localeChanged.1") + locale
                    + getString("LocaleHandler.setSelectedLanguage.localeChanged.2"), getString("info"),
                    JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(null, getString("LocaleHandler.setSelectedLanguage.localeNotChanged.1")
                    + locale + getString("LocaleHandler.setSelectedLanguage.localeNotChanged.2"), getString("error"),
                    JOptionPane.ERROR_MESSAGE);
        }
    }
}
