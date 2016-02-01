package org.allurefw.report;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationIntrospector;
import com.google.inject.Inject;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.UUID;

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
    protected Set<ReportDataProvider> dataProviders;

    public void generate(Path output) {
        boolean findAnyResults = false;
        for (TestCaseProvider provider : providers) {
            for (TestCase testCase : provider) {
                findAnyResults = true;

                for (TestCaseProcessor processor : processors) {
                    processor.process(testCase);
                }

                write(output, UUID.randomUUID() + ".json", testCase);
            }
        }
        if (!findAnyResults) {
            System.out.println("Could not find any results");
        }

        for (ReportDataProvider provider : dataProviders) {
            write(output, provider.getFileName(), provider.provide());
        }
    }

    private void write(Path outputDir, String fileName, Object object) {
        try (OutputStream stream = Files.newOutputStream(outputDir.resolve(fileName))) {
            getMapper().writeValue(stream, object);
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }

    private ObjectMapper getMapper() {
        return new ObjectMapper()
                .setAnnotationIntrospector(new JaxbAnnotationIntrospector(TypeFactory.defaultInstance()))
                .enable(SerializationFeature.INDENT_OUTPUT)
                .setSerializationInclusion(JsonInclude.Include.NON_NULL);
    }
}
