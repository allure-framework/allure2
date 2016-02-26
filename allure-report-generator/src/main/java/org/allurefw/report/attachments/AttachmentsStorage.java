package org.allurefw.report.attachments;

import com.google.common.io.Files;
import org.allurefw.report.ReportApiUtils;
import org.allurefw.report.entity.Attachment;
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
 *         Date: 26.02.16
 */
public class AttachmentsStorage {

    private static final Logger LOGGER = LoggerFactory.getLogger(AttachmentsStorage.class);

    private Map<Path, Attachment> attachments = new HashMap<>();

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

    public Map<Path, Attachment> getAttachments() {
        return Collections.unmodifiableMap(attachments);
    }
}
