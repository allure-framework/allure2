package org.allurefw.report.core;

import org.allurefw.report.AttachmentsStorage;
import org.allurefw.report.ReportApiUtils;
import org.allurefw.report.entity.Attachment;

import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static com.google.common.io.Files.getFileExtension;
import static org.allurefw.report.ReportApiUtils.generateUid;
import static org.allurefw.report.ReportApiUtils.getFileSizeSafe;
import static org.allurefw.report.ReportApiUtils.probeContentType;

/**
 * @author charlie (Dmitry Baev).
 */
public class DefaultAttachmentsStorage implements AttachmentsStorage {

    private final Map<Path, Attachment> attachments = new HashMap<>();

    @Override
    public Attachment addAttachment(Path file) {
        String uid = generateUid();
        String realType = probeContentType(file);
        String extension = Optional.of(getFileExtension(file.toString()))
                .filter(s -> !s.isEmpty())
                .orElseGet(() -> ReportApiUtils.getExtensionByMimeType(realType));
        String source = uid + (extension.isEmpty() ? "" : "." + extension);
        Long size = getFileSizeSafe(file);
        Attachment attachment = new Attachment()
                .withUid(uid)
                .withName(file.getFileName().toString())
                .withSource(source)
                .withType(realType)
                .withSize(size);
        attachments.put(file, attachment);
        return attachment;
    }

    @Override
    public Optional<Attachment> findAttachmentByFileName(String resourceName) {
        return attachments.values().stream()
                .filter(attachment -> Objects.equals(resourceName, attachment.getName()))
                .findAny();
    }

    @Override
    public Map<Path, Attachment> getAttachments() {
        return Collections.unmodifiableMap(attachments);
    }
}
