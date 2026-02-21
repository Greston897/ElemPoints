package jar.elem.elempoints.plugin.util;

import org.bukkit.Bukkit;

/**
 * Utility for server version detection.
 * Supports 1.16 through 1.21+.
 */
public final class VersionUtil {

    private static final int MAJOR;
    private static final int MINOR;

    static {
        String version = Bukkit.getBukkitVersion(); // e.g. "1.20.4-R0.1-SNAPSHOT"
        String[] parts = version.split("[.-]");
        int major = 1, minor = 16;
        try {
            major = Integer.parseInt(parts[0]);
            minor = Integer.parseInt(parts[1]);
        } catch (Exception ignored) {}
        MAJOR = major;
        MINOR = minor;
    }

    private VersionUtil() {}

    public static int getMajor() { return MAJOR; }
    public static int getMinor() { return MINOR; }

    /** @return true if server is 1.X or higher */
    public static boolean isAtLeast(int minor) {
        return MINOR >= minor;
    }

    /** @return e.g. "1.20" */
    public static String getVersionString() {
        return MAJOR + "." + MINOR;
    }
}