package org.allurefw.report;

import org.allurefw.report.entity.AttachmentFile;

import java.util.List;
import java.util.Optional;

/**
 * You can use this index to get information about
 * your report attachments during report generation.
 *
 * @author Dmitry Baev charlie@yandex-team.ru
 *         Date: 10.07.15
 */
public interface AttachmentFilesIndex {

    /**
     * Finds the attachment by given uid. Returns null if there is
     * no attachment with such uid.
     *
     * @param uid uid to find attachment.
     * @return found attachment info or null if there is no attachment
     * with such uid.
     */
    Optional<AttachmentFile> find(String uid);

    /**
     * Finds the attachment by given source. Returns null if there is
     * no attachment with such source.
     *
     * @param source source to find attachment.
     * @return found attachment info or null if there is no attachment
     * with such source.
     */
    Optional<AttachmentFile> findByFileName(String source);

    /**
     * Returns the list of all attachments found in result directories.
     */
    List<AttachmentFile> findAll();
}
