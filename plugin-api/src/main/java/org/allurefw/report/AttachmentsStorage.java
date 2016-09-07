package org.allurefw.report;

import org.allurefw.report.entity.Attachment;

import java.util.Optional;
import java.util.Set;

/**
 * @author charlie (Dmitry Baev).
 */
public interface AttachmentsStorage {

    Attachment addAttachment(ResultsSource source, String resourceName);

    Optional<Attachment> findAttachmentByName(String resourceName);

    Set<Attachment> getAttachments();

}
