package io.qameta.allure.core;

import io.qameta.allure.AttachmentsStorage;
import io.qameta.allure.ReportApiUtils;
import io.qameta.allure.entity.Attachment;

import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static com.google.common.io.Files.getFileExtension;
import static io.qameta.allure.ReportApiUtils.generateUid;
import static io.qameta.allure.ReportApiUtils.getFileSizeSafe;
import static io.qameta.allure.ReportApiUtils.probeContentType;

/**
 * @author charlie (Dmitry Baev).
 */
public class DefaultAttachmentsStorage implements AttachmentsStorage {

    private final Map<Path, Attachment> attachments = new HashMap<>();

    @Override
    public Attachment addAttachment(final Path attachmentFile) {
        return attachments.computeIfAbsent(attachmentFile, file -> {
            final String uid = generateUid();
            final String realType = probeContentType(file);
            final String extension = Optional.of(getFileExtension(file.toString()))
                    .filter(s -> !s.isEmpty())
                    .map(s -> "." + s)
                    .orElseGet(() -> ReportApiUtils.getExtensionByMimeType(realType));
            final String source = uid + (extension.isEmpty() ? "" : extension);
            final Long size = getFileSizeSafe(file);
            return new Attachment()
                    .withUid(uid)
                    .withName(file.getFileName().toString())
                    .withSource(source)
                    .withType(realType)
                    .withSize(size);
        });
    }

    @Override
    public Optional<Attachment> findAttachmentByFileName(final String resourceName) {
        return attachments.values().stream()
                .filter(attachment -> Objects.equals(resourceName, attachment.getName()))
                .findAny();
    }

    @Override
    public Map<Path, Attachment> getAttachments() {
        return Collections.unmodifiableMap(attachments);
    }
}
