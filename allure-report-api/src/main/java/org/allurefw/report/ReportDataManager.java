package org.allurefw.report;

import org.allurefw.LabelName;
import org.allurefw.report.entity.Attachment;
import org.allurefw.report.entity.AttachmentFile;
import org.allurefw.report.entity.GroupInfo;
import org.allurefw.report.entity.TestCase;

import java.nio.file.Path;
import java.util.Collections;

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
     * Adds given test case to the reportData.
     *
     * @param testCase the test case to add to the reportData.
     */
    default void addTestCase(TestCase testCase) {
        addTestCases(Collections.singletonList(testCase));
    }

    /**
     * Adds a new group info.
     *
     * @param groupType the type of the group, an example <code>suite</code>.
     * @param groupInfo the group info to add.
     */
    void addGroupInfo(String groupType, GroupInfo groupInfo);

    /**
     * Shortcut for {@link #addGroupInfo(String, GroupInfo)}
     */
    default void addGroupInfo(LabelName groupType, GroupInfo groupInfo) {
        addGroupInfo(groupType.value(), groupInfo);
    }

    /**
     * Adds given attachment to the reportData and return the file name of
     * such attachment in reportData data directory.
     *
     * @param path the path to file to add to the reportData.
     * @param type the mime-type of attachment.
     * @return the {@link AttachmentFile}. The source of attachmentFile helps to access
     * such attachment in the reportData data directory.
     */
    Attachment addAttachment(Path path, String type);

    /**
     * Shortcut for {@link #addAttachment(Path, String)}
     */
    default Attachment addAttachment(Path path) {
        return addAttachment(path, null);
    }

}
