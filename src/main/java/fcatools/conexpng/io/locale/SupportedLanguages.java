package fcatools.conexpng.io.locale;

/**
 * Enumeration of supported languages (properties file exists).
 * 
 * @author Torsten Casselt
 */
public enum SupportedLanguages {
    EN, DE;

    /**
     * Checks if given language is supported.
     * 
     * @param locale
     *            language to check support for
     * @return true if language is supported, false if not
     */
    public static boolean contains(String locale) {
        for (SupportedLanguages sl : SupportedLanguages.values()) {
            if (sl.toString().toLowerCase().equals(locale.toLowerCase())) {
                return true;
            }
        }
        return false;
    }
}
