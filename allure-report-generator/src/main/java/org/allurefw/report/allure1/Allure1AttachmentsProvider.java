package org.allurefw.report.allure1;

import com.google.inject.Inject;
import org.allurefw.report.AttachmentFileProvider;
import org.allurefw.report.ResultDirectories;
import org.allurefw.report.entity.AttachmentFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.yandex.qatools.allure.AllureUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.stream.Collectors;

import static org.allurefw.report.ReportApiUtils.generateUid;

/**
 * @author Dmitry Baev charlie@yandex-team.ru
 *         Date: 05.02.16
 */
public class Allure1AttachmentsProvider implements AttachmentFileProvider {

    public static final Logger LOGGER = LoggerFactory.getLogger(Allure1AttachmentsProvider.class);

    private final Path[] resultDirectories;

    @Inject
    public Allure1AttachmentsProvider(@ResultDirectories Path[] resultDirectories) {
        this.resultDirectories = resultDirectories;
    }

    @Override
    public Iterator<AttachmentFile> iterator() {
        return AllureUtils.listAttachmentFilesSafe(resultDirectories).stream()
                .map(path -> new AttachmentFile()
                        .withUid(generateUid())
                        .withPath(path.toAbsolutePath().toString())
                        .withSize(getSize(path)))
                .collect(Collectors.toList()).iterator();
    }

    public long getSize(Path path) {
        try {
            return Files.size(path);
        } catch (IOException e) {
            LOGGER.error("Couldn't get the size of the attachment file " + path, e);
            return -1;
        }
    }
}
