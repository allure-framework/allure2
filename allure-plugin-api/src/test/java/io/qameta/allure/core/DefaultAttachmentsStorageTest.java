package io.qameta.allure.core;

import io.qameta.allure.entity.Attachment;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static com.github.npathai.hamcrestopt.OptionalMatchers.hasValue;
import static com.github.npathai.hamcrestopt.OptionalMatchers.isPresent;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.junit.Assert.assertThat;

/**
 * @author charlie (Dmitry Baev).
 */
public class DefaultAttachmentsStorageTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Test
    public void shouldAddAttachment() throws Exception {
        String fileName = "some-name";
        Path attachmentFile = folder.newFile(fileName).toPath();
        Files.write(attachmentFile, "hello".getBytes(StandardCharsets.UTF_8));

        DefaultAttachmentsStorage storage = new DefaultAttachmentsStorage();
        Attachment attachment = storage.addAttachment(attachmentFile);
        assertThat(attachment.getName(), is(fileName));
        assertThat(attachment.getSize(), is(5L));
        assertThat(attachment.getUid(), notNullValue());
        assertThat(attachment.getSource(), notNullValue());
        assertThat(attachment.getType(), is("text/plain"));
    }

    @Test
    public void shouldFindAttachment() throws Exception {
        String fileName = "some-name";
        Path attachmentFile = folder.newFile(fileName).toPath();
        Files.write(attachmentFile, "hello".getBytes(StandardCharsets.UTF_8));

        DefaultAttachmentsStorage storage = new DefaultAttachmentsStorage();
        Attachment attachment = storage.addAttachment(attachmentFile);
        Optional<Attachment> found = storage.findAttachmentByFileName(fileName);
        assertThat(found, isPresent());
        assertThat(found, hasValue(attachment));
    }

    @Test
    public void shouldNotOverrideAttachments() throws Exception {
        String fileName = "asdasdasdasd";
        Path attachmentFile = folder.newFile(fileName).toPath();
        Files.write(attachmentFile, "sdafdsfsdf".getBytes(StandardCharsets.UTF_8));

        DefaultAttachmentsStorage storage = new DefaultAttachmentsStorage();
        Attachment first = storage.addAttachment(attachmentFile);
        Attachment second = storage.addAttachment(attachmentFile);
        assertThat(first.getSource(), is(second.getSource()));
    }

    @Test
    public void shouldFindAll() throws Exception {
        String fileName = "asdasdasdasd";
        Path attachmentFile = folder.newFile(fileName).toPath();
        Files.write(attachmentFile, "sdafdsfsdf".getBytes(StandardCharsets.UTF_8));

        DefaultAttachmentsStorage storage = new DefaultAttachmentsStorage();
        storage.addAttachment(attachmentFile);
        Set<Map.Entry<Path, Attachment>> set = storage.getAttachments().entrySet();
        assertThat(set, hasSize(1));
    }
}