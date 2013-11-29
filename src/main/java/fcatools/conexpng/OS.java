package fcatools.conexpng;

public class OS {

    public static final boolean isMacOsX;
    public static final boolean isWindows;
    public static final boolean isLinux;

    static {
        String os = System.getProperty("os.name");
        if (os != null)
            os = os.toLowerCase();

        isMacOsX = "mac os x".equals(os);
        isWindows = os != null && os.indexOf("windows") != -1;
        isLinux = os != null && os.indexOf("linux") != -1;
    }

}
