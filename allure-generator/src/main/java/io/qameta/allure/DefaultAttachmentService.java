package io.qameta.allure;

import io.qameta.allure.entity.Attachment;
import io.qameta.allure.entity.AttachmentLink;
import io.qameta.allure.service.AttachmentService;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import static java.util.Objects.isNull;

/**
 * @author charlie (Dmitry Baev).
 */
@SuppressWarnings("all")
public class DefaultAttachmentService implements AttachmentService {

    private final Map<Long, AttachmentLink> links = new ConcurrentHashMap<>();
    private final Map<String, Attachment> filesByFileName = new ConcurrentHashMap<>();

    private final AtomicLong linkId = new AtomicLong();

    @Override
    public Attachment storeAttachmentLink(final long testResultId, final AttachmentLink link) {
        link.setId(linkId.incrementAndGet());

        return null;
    }

    @Override
    public Attachment storeAttachmentFile(final Path file) {
        return null;
    }

    @Override
    public List<Attachment> getAttachments(final long testResultId) {
        return null;
    }

    protected String getFileName(final Path content) {
        if (Files.isDirectory(content)) {
            throw new IllegalStateException("Directory can not be stored as attachment");
        }
        if (Files.notExists(content)) {
            throw new IllegalStateException("Attachment file not exists");
        }
        final Path file = content.getFileName();
        if (isNull(file)) {
            throw new IllegalStateException("Could not get file name");
        }
        return file.toString();
    }
}
