package org.allurefw;

import static org.allurefw.LabelName.FEATURE;
import static org.allurefw.LabelName.FRAMEWORK;
import static org.allurefw.LabelName.HOST;
import static org.allurefw.LabelName.ISSUE;
import static org.allurefw.LabelName.LANGUAGE;
import static org.allurefw.LabelName.SEVERITY;
import static org.allurefw.LabelName.STORY;
import static org.allurefw.LabelName.SUITE;
import static org.allurefw.LabelName.TEST_ID;
import static org.allurefw.LabelName.THREAD;

/**
 * @author Dmitry Baev charlie@yandex-team.ru
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
     * Create suite label with given value.
     */
    public static Label createSuiteLabel(String suite) {
        return createLabel(SUITE, suite);
    }

    /**
     * Create feature label with given value.
     */
    public static Label createFeatureLabel(String feature) {
        return createLabel(FEATURE, feature);
    }

    /**
     * Create story label with given value.
     */
    public static Label createStoryLabel(String story) {
        return createLabel(STORY, story);
    }

    /**
     * Create severity label with given value.
     */
    public static Label createSeverityLabel(SeverityLevel level) {
        return createLabel(SEVERITY, level.value());
    }

    /**
     * Create language label with given value.
     */
    public static Label createProgrammingLanguageLabel() {
        return createLabel(LANGUAGE, LANG);
    }

    /**
     * Create test framework label with given value.
     */
    public static Label createTestFrameworkLabel(String testFrameworkName) {
        return createLabel(FRAMEWORK, testFrameworkName);
    }

    /**
     * Create issue label with given value.
     */
    public static Label createIssueLabel(String issueKey) {
        return createLabel(ISSUE, issueKey);
    }

    /**
     * Create test label with given value.
     */
    public static Label createTestLabel(String testKey) {
        return createLabel(TEST_ID, testKey);
    }

    /**
     * Create host label with given value.
     */
    public static Label createHostLabel(String host) {
        return createLabel(HOST, host);
    }

    /**
     * Create thread label with given value.
     */
    public static Label createThreadLabel(String thread) {
        return createLabel(THREAD, thread);
    }

    /**
     * Create label with given {@link LabelName} and value.
     */
    public static Label createLabel(LabelName name, String value) {
        return new Label().withName(name.value()).withValue(value);
    }
}
