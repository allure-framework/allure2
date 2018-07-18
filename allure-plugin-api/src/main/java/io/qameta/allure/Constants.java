package io.qameta.allure;

/**
 * @author charlie (Dmitry Baev).
 * @since 2.7
 */
@SuppressWarnings("PMD.ClassNamingConventions")
public final class Constants {

    /**
     * The name of directory that contains widgets data.
     */
    public static final String WIDGETS_DIR = "widgets";

    /**
     * The name of directory with main report data.
     */
    public static final String DATA_DIR = "data";

    /**
     * The name of directory with report plugins.
     */
    public static final String PLUGINS_DIR = "plugins";

    /**
     * The name of directory with exported data.
     */
    public static final String EXPORT_DIR = "export";

    /**
     * The name of directory with historical data.
     */
    public static final String HISTORY_DIR = "history";

    private Constants() {
        throw new IllegalStateException("Do not instance");
    }
}
