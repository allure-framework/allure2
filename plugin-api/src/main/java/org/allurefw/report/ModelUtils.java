package org.allurefw.report;

import org.allurefw.report.entity.Label;
import org.allurefw.report.entity.LabelName;

/**
 * @author Dmitry Baev baev@qameta.io
 *         Date: 30.01.16
 */
public final class ModelUtils {

    private static final String VERSION = ModelUtils.class.getPackage().getImplementationVersion();

    private static final String LANG = "java";

    ModelUtils() {
    }

    public static String getVersion() {
        return VERSION;
    }

    /**
     * Create label with given {@link LabelName} and value.
     */
    public static Label createLabel(LabelName name, String value) {
        return createLabel(name.value(), value);
    }

    /**
     * Create label with given name and value.
     */
    public static Label createLabel(String name, String value) {
        return new Label().withName(name).withValue(value);
    }
}
