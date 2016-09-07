package org.allurefw.report;

import org.allurefw.report.entity.Attachment;

import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import static com.google.common.io.Files.getFileExtension;
import static org.allurefw.report.ReportApiUtils.generateUid;

/**
 * @author charlie (Dmitry Baev).
 */
public class DefaultAttachmentsStorage implements AttachmentsStorage {

    private final Set<Attachment> attachments = new HashSet<>();

    @Override
    public Attachment addAttachment(ResultsSource resultsSource, String resourceName) {
        String uid = generateUid();
        String realType = ReportApiUtils.probeContentType(
                resultsSource.getResult(resourceName), resourceName
        );
        String extension = Optional.of(getFileExtension(resourceName))
                .filter(s -> !s.isEmpty())
                .orElseGet(() -> ReportApiUtils.getExtensionByMimeType(realType));
        String source = uid + (extension.isEmpty() ? "" : "." + extension);
        Long size = resultsSource.getSize(resourceName);
        Attachment attachment = new Attachment()
                .withUid(uid)
                .withName(resourceName)
                .withSource(source)
                .withType(realType)
                .withSize(size);
        attachments.add(attachment);
        return attachment;
    }

    @Override
    public Optional<Attachment> findAttachmentByName(String resourceName) {
        return attachments.stream()
                .filter(attachment -> Objects.equals(resourceName, attachment.getName()))
                .findAny();
    }

    @Override
    public Set<Attachment> getAttachments() {
        return Collections.unmodifiableSet(attachments);
    }
}
