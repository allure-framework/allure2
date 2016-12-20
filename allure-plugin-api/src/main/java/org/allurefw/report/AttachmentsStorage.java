package org.allurefw.report;

import org.allurefw.report.entity.Attachment;

import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * @author charlie (Dmitry Baev).
 */
public interface AttachmentsStorage {

    Attachment addAttachment(Path attachment);

    Optional<Attachment> findAttachmentByFileName(String resourceName);

    Map<Path, Attachment> getAttachments();

}
