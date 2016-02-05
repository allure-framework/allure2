package org.allurefw.report;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationIntrospector;
import com.google.inject.Inject;
import org.allurefw.report.entity.TestCase;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Set;

/**
 * @author Dmitry Baev charlie@yandex-team.ru
 *         Date: 30.01.16
 */
public class Lifecycle {

    @Inject
    protected Set<TestCaseProvider> providers;

    @Inject
    protected Set<TestCaseProcessor> processors;

    @Inject
    protected Set<ReportDataProvider> datas;

    @Inject
    protected Set<WidgetDataProvider> widgets;

    @Inject
    protected ReportConfig config;

    @Inject
    protected AttachmentFilesIndex attachments;

    public void generate(Path output) {

        boolean findAnyResults = false;
        for (TestCaseProvider provider : providers) {
            for (TestCase testCase : provider) {
                findAnyResults = true;

                for (TestCaseProcessor processor : processors) {
                    //todo we should copy test case at first
                    processor.process(testCase);
                }

                write(output.resolve("testcases"), testCase.getUid() + ".json", testCase);
            }
        }

        if (!findAnyResults && config.isFailIfNoResultsFound()) {
            System.out.println("Could not find any results");
        }

        datas.forEach(provider -> write(output, provider.getFileName(), provider.provide()));

        HashMap<String, Object> results = widgets.stream()
                .collect(
                        HashMap::new,
                        (map, provider) -> map.put(provider.getWidgetId(), provider.provide()),
                        HashMap::putAll
                );

        write(output, "widgets.json", results);

        attachments.findAll().forEach(file -> {
            Path source = Paths.get(file.getPath());
            Path target = output.resolve("attachments").resolve(file.getUid() + "." + file.getFileExtension());
            try {
                Files.createDirectories(target.getParent());
                Files.copy(source, target);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    private void write(Path outputDir, String fileName, Object object) {
        try {
            Files.createDirectories(outputDir);
        } catch (IOException ignored) {
        }
        try (OutputStream stream = Files.newOutputStream(outputDir.resolve(fileName))) {
            getMapper().writeValue(stream, object);
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }

    private ObjectMapper getMapper() {
        return new ObjectMapper()
                .configure(MapperFeature.USE_WRAPPER_NAME_AS_PROPERTY_NAME, true)
                .setAnnotationIntrospector(new JaxbAnnotationIntrospector(TypeFactory.defaultInstance()))
                .enable(SerializationFeature.INDENT_OUTPUT)
                .setSerializationInclusion(JsonInclude.Include.NON_NULL);
    }
}
