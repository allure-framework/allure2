package org.allurefw.report;

import com.google.common.collect.Iterables;
import com.google.common.io.Files;
import org.allurefw.report.entity.Attachment;
import org.allurefw.report.entity.GroupInfo;
import org.allurefw.report.entity.TestCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static org.allurefw.report.ReportApiUtils.generateUid;

/**
 * @author Dmitry Baev charlie@yandex-team.ru
 *         Date: 17.02.16
 */
public class DefaultReportDataManager implements ReportDataManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultReportDataManager.class);

    private Iterable<TestCase> testCases = Collections.emptyList();

    private Map<String, Map<String, GroupInfo>> groups = new HashMap<>();

    private Map<Path, Attachment> attachments = new HashMap<>();

    @Override
    public void addTestCases(Iterable<TestCase> testCases) {
        this.testCases = Iterables.concat(this.testCases, testCases);
    }

    @Override
    public void addGroupInfo(String groupType, GroupInfo groupInfo) {
        groups.putIfAbsent(groupType, new HashMap<>());
        groups.get(groupType).put(groupInfo.getName(), groupInfo);
    }

    @Override
    public Attachment addAttachment(Path path, String type) {
        String uid = generateUid();
        String fileName = path.getFileName().toString();
        String realType = Optional.ofNullable(type)
                .filter(Objects::nonNull)
                .filter(s -> !s.isEmpty())
                .orElseGet(() -> ReportApiUtils.probeContentType(path));
        String extension = Optional.of(Files.getFileExtension(fileName))
                .filter(s -> !s.isEmpty())
                .orElseGet(() -> ReportApiUtils.getExtensionByMimeType(realType));
        String fileNameWithoutExtension = Files.getNameWithoutExtension(fileName);
        String source = uid + (extension.isEmpty() ? "" : "." + extension);
        long size = getSize(path);

        Attachment attachment = new Attachment()
                .withUid(uid)
                .withName(fileNameWithoutExtension)
                .withSource(source)
                .withType(realType)
                .withSize(size);

        attachments.put(path, attachment);
        return attachment;
    }

    private long getSize(Path path) {
        try {
            return java.nio.file.Files.size(path);
        } catch (IOException e) {
            LOGGER.warn("Could not get the size of file {} {}", path, e);
            return 0;
        }
    }

    public Iterable<TestCase> getTestCases() {
        return testCases;
    }

    public Map<Path, Attachment> getAttachments() {
        return attachments;
    }
}
