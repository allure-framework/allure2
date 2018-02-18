package io.qameta.allure;

import io.qameta.allure.entity.AttachmentLink;
import io.qameta.allure.service.AttachmentService;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import static java.util.Objects.isNull;

/**
 * @author charlie (Dmitry Baev).
 */
public class DefaultAttachmentService implements AttachmentService {

    private final Map<Long, List<AttachmentLink>> links = new ConcurrentHashMap<>();
    private final Map<String, AttachmentLink> filesByFileName = new ConcurrentHashMap<>();

    private final AtomicLong linkId = new AtomicLong();

    @Override
    public AttachmentLink storeAttachmentLink(final long testResultId,
                                              final AttachmentLink link) {
        link.setId(linkId.incrementAndGet());
        links.computeIfAbsent(testResultId, id -> new ArrayList<>()).add(link);
        return link;
    }

    @Override
    public AttachmentLink storeAttachmentFile(final Path file) {
        final String fileName = getFileName(file);

        return null;
    }

    @Override
    public List<AttachmentLink> getAttachments(final long testResultId) {
        return links.get(testResultId);
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
