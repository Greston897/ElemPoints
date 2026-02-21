package jar.elem.elempoints.api;

/**
 * Static provider for accessing the {@link ElemPointsAPI}.
 * <p>
 * This is the primary entry point for other plugins.
 * <p>
 * Example:
 * <pre>{@code
 * // In your onEnable():
 * if (ElemPointsProvider.isAvailable()) {
 *     ElemPointsAPI api = ElemPointsProvider.get();
 *     // use api...
 * } else {
 *     getLogger().warning("ElemPoints not found!");
 * }
 * }</pre>
 *
 * <h3>Maven Dependency</h3>
 * <pre>{@code
 * <dependency>
 *     <groupId>jar.elem.elempoints</groupId>
 *     <artifactId>elempoints-api</artifactId>
 *     <version>2.0.0</version>
 *     <scope>provided</scope>
 * </dependency>
 * }</pre>
 *
 * @since 2.0.0
 */
public final class ElemPointsProvider {

    private static ElemPointsAPI instance;

    private ElemPointsProvider() {}

    /**
     * Returns the API instance.
     *
     * @return the ElemPointsAPI
     * @throws IllegalStateException if the API is not registered yet
     */
    public static ElemPointsAPI get() {
        ElemPointsAPI api = instance;
        if (api == null) {
            throw new IllegalStateException(
                    "ElemPoints API is not loaded! Possible causes:\n" +
                            "  1. ElemPoints plugin is not installed on the server\n" +
                            "  2. Your plugin loaded before ElemPoints — add 'depend: [ElemPoints]' to plugin.yml\n" +
                            "  3. ElemPoints failed to enable — check server logs"
            );
        }
        return api;
    }

    /**
     * Checks if the API is available and ready.
     *
     * @return true if the API can be used
     */
    public static boolean isAvailable() {
        return instance != null && instance.isReady();
    }

    /**
     * Internal: registers the API implementation.
     * Called by ElemPoints plugin only.
     */
    public static void register(ElemPointsAPI api) {
        if (api == null) throw new IllegalArgumentException("API cannot be null");
        instance = api;
    }

    /**
     * Internal: unregisters the API.
     * Called on plugin disable.
     */
    public static void unregister() {
        instance = null;
    }
}