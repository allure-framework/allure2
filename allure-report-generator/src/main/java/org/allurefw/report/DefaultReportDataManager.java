package org.allurefw.report;

import com.google.common.collect.Iterables;
import org.allurefw.report.entity.AttachmentFile;
import org.allurefw.report.entity.TestCase;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.allurefw.report.ReportApiUtils.generateUid;

/**
 * @author Dmitry Baev charlie@yandex-team.ru
 *         Date: 17.02.16
 */
public class DefaultReportDataManager implements ReportDataManager {

    private Iterable<TestCase> testCases = Collections.emptyList();

    private List<AttachmentFile> attachments = new ArrayList<>();

    @Override
    public void addTestCases(Iterable<TestCase> testCases) {
        this.testCases = Iterables.concat(this.testCases, testCases);
    }

    @Override
    public void enrichGroup(String groupType, String groupName, Map<String, String> properties) {

    }

    @Override
    public String addAttachment(Path path) {
        AttachmentFile attachment = new AttachmentFile()
                .withUid(generateUid())
                .withPath(path.toAbsolutePath().toString());
        attachments.add(attachment);
        return attachment.getUid() + "." + attachment.getFileExtension();
    }

    public Iterable<TestCase> getTestCases() {
        return testCases;
    }

    public List<AttachmentFile> getAttachments() {
        return attachments;
    }
}
