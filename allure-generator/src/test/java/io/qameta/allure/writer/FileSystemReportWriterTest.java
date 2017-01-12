package io.qameta.allure.writer;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationIntrospector;
import io.qameta.allure.entity.Attachment;
import io.qameta.allure.entity.TestCaseResult;
import io.qameta.allure.testdata.TestData;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static io.qameta.allure.testdata.TestData.randomAttachment;
import static io.qameta.allure.testdata.TestData.randomContent;
import static io.qameta.allure.testdata.TestData.randomFileName;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.beans.HasPropertyWithValue.hasProperty;
import static org.junit.Assert.assertThat;
import static ru.yandex.qatools.matchers.nio.PathMatchers.contains;
import static ru.yandex.qatools.matchers.nio.PathMatchers.exists;

/**
 * @author charlie (Dmitry Baev).
 */
public class FileSystemReportWriterTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    private final ObjectMapper mapper = new ObjectMapper()
            .configure(MapperFeature.USE_WRAPPER_NAME_AS_PROPERTY_NAME, true)
            .setAnnotationIntrospector(new JaxbAnnotationIntrospector(TypeFactory.defaultInstance()))
            .enable(SerializationFeature.INDENT_OUTPUT)
            .setSerializationInclusion(JsonInclude.Include.NON_NULL);

    @Test
    public void shouldWriteTestCase() throws Exception {
        Path output = folder.newFolder().toPath();
        FileSystemReportWriter writer = new FileSystemReportWriter(mapper, output);
        TestCaseResult expected = TestData.randomTestCase();
        writer.writeTestCase(expected);
        assertThat(output, exists());
        Path testCasesDir = output.resolve("data/test-cases");
        assertThat(testCasesDir, contains(expected.getSource()));

        TestCaseResult actual = mapper.readValue(
                testCasesDir.resolve(expected.getSource()).toFile(),
                TestCaseResult.class
        );

        assertThat(actual, hasProperty("name", equalTo(expected.getName())));
    }

    @Test
    public void shouldWriteAttachments() throws Exception {
        Path output = folder.newFolder().toPath();
        FileSystemReportWriter writer = new FileSystemReportWriter(mapper, output);
        String expected = randomContent();
        byte[] bytes = expected.getBytes(StandardCharsets.UTF_8);
        Attachment attachment = randomAttachment();
        writer.writeAttachment(new ByteArrayInputStream(bytes), attachment);
        assertThat(output, exists());
        Path attachmentsDir = output.resolve("data/attachments");
        assertThat(attachmentsDir, contains(attachment.getSource()));

        byte[] allBytes = Files.readAllBytes(attachmentsDir.resolve(attachment.getSource()));
        String actual = new String(allBytes, StandardCharsets.UTF_8);

        assertThat(actual, is(expected));
    }

    @Test
    public void shouldWriteJsonData() throws Exception {
        Path output = folder.newFolder().toPath();
        FileSystemReportWriter writer = new FileSystemReportWriter(mapper, output);
        Attachment data = randomAttachment();
        String fileName = randomFileName();
        writer.writeJsonData(fileName, data);
        Path dataDir = output.resolve("data");

        assertThat(dataDir, contains(fileName));

        Attachment actual = mapper.readValue(
                dataDir.resolve(fileName).toFile(),
                Attachment.class
        );

        assertThat(actual, hasProperty("name", equalTo(data.getName())));
    }
}