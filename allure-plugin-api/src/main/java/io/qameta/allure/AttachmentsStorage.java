package io.qameta.allure;

import io.qameta.allure.entity.Attachment;

import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;

/**
 * @author charlie (Dmitry Baev).
 */
public interface AttachmentsStorage {

    Attachment addAttachment(Path attachment);

    Optional<Attachment> findAttachmentByFileName(String resourceName);

    Map<Path, Attachment> getAttachments();

}
