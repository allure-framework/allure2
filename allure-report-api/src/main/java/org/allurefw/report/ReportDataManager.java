package org.allurefw.report;

import org.allurefw.Label;
import org.allurefw.LabelName;
import org.allurefw.report.entity.Attachment;
import org.allurefw.report.entity.AttachmentFile;
import org.allurefw.report.entity.TestCase;

import java.nio.file.Path;
import java.util.Collections;
import java.util.Map;

/**
 * @author Dmitry Baev charlie@yandex-team.ru
 *         Date: 17.02.16
 */
public interface ReportDataManager {

    /**
     * Bulk add test cases. See {@link #addTestCase(TestCase)}
     *
     * @param testCases the iterable of test cases to add. If iterator is lazy
     *                  only one test case will be loaded per time.
     */
    void addTestCases(Iterable<TestCase> testCases);

    /**
     * Adds given test case to the report.
     *
     * @param testCase the test case to add to the report.
     */
    default void addTestCase(TestCase testCase) {
        addTestCases(Collections.singletonList(testCase));
    }

    /**
     * Enrich the grouping with properties.
     *
     * @param groupType  the type of group to enrich. An example <code>suite</code>.
     * @param groupName  the name of group to enrich. An example <code>MyTest</code>.
     * @param properties the properties to add to group.
     */
    void enrichGroup(String groupType, String groupName, Map<String, String> properties);

    /**
     * Shortcut for {@link #enrichGroup(String, String, Map)}
     */
    default void enrichGroup(String groupingName, String groupName, String key, String value) {
        enrichGroup(groupingName, groupName, Collections.singletonMap(key, value));
    }

    /**
     * Shortcut for {@link #enrichGroup(String, String, Map)}
     */
    default void enrichGroup(LabelName groupType, String groupName, Map<String, String> properties) {
        enrichGroup(groupType.value(), groupName, properties);
    }

    /**
     * Shortcut for {@link #enrichGroup(String, String, String, String)}
     */
    default void enrichGroup(LabelName groupType, String groupName, String key, String value) {
        enrichGroup(groupType.value(), groupName, key, value);
    }

    /**
     * Shortcut for {@link #enrichGroup(String, String, String, String)}
     */
    default void enrichGroup(Label groupLabel, String key, String value) {
        enrichGroup(groupLabel.getName(), groupLabel.getValue(), key, value);
    }

    /**
     * Shortcut for {@link #enrichGroup(String, String, Map)}
     */
    default void enrichGroup(Label groupLabel, String key, Map<String, String> properties) {
        enrichGroup(groupLabel.getName(), groupLabel.getValue(), properties);
    }

    /**
     * Adds given attachment to the report and return the file name of
     * such attachment in report data directory.
     *
     * @param path the path to file to add to the report.
     * @param type the mime-type of attachment.
     * @return the {@link AttachmentFile}. The source of attachmentFile helps to access
     * such attachment in the report data directory.
     */
    Attachment addAttachment(Path path, String type);

    /**
     * Shortcut for {@link #addAttachment(Path, String)}
     */
    default Attachment addAttachment(Path path) {
        return addAttachment(path, null);
    }
}
