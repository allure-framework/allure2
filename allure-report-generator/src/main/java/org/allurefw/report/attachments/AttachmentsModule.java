package org.allurefw.report.attachments;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import org.allurefw.report.AttachmentFileProvider;
import org.allurefw.report.AttachmentFilesIndex;
import org.allurefw.report.entity.AttachmentFile;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * @author Dmitry Baev charlie@yandex-team.ru
 *         Date: 04.02.16
 */
public class AttachmentsModule extends AbstractModule {

    @Override
    protected void configure() {
        //do nothing
    }

    @Provides
    @Singleton
    protected AttachmentFilesIndex index(Set<AttachmentFileProvider> providers) {
        final List<AttachmentFile> files = providers.stream()
                .flatMap(provider -> StreamSupport.stream(provider.spliterator(), false))
                .collect(Collectors.toList());

        return new AttachmentFilesIndex() {
            @Override
            public Optional<AttachmentFile> find(String uid) {
                return files.stream().filter(file -> uid.equals(file.getUid())).findAny();
            }

            @Override
            public Optional<AttachmentFile> findByFileName(String fileName) {
                return files.stream().filter(file -> fileName.equals(file.getFileName())).findAny();
            }

            @Override
            public List<AttachmentFile> findAll() {
                return Collections.unmodifiableList(files);
            }
        };
    }
}
