package io.qameta.allure.service;

import io.qameta.allure.entity.Attachment;
import io.qameta.allure.entity.AttachmentLink;

import java.nio.file.Path;
import java.util.List;

/**
 * @author charlie (Dmitry Baev).
 */
public interface AttachmentService {

    Attachment storeAttachmentLink(long testResultId, AttachmentLink link);

    Attachment storeAttachmentFile(Path file);

    List<Attachment> getAttachments(long testResultId);

}
