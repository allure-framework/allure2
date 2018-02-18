package io.qameta.allure.service;

import io.qameta.allure.entity.AttachmentLink;

import java.nio.file.Path;
import java.util.List;

/**
 * @author charlie (Dmitry Baev).
 */
public interface AttachmentService {

    AttachmentLink storeAttachmentLink(long testResultId, AttachmentLink link);

    AttachmentLink storeAttachmentFile(Path file);

    List<AttachmentLink> getAttachments(long testResultId);

}
