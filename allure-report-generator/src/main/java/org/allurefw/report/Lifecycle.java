package org.allurefw.report;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationIntrospector;
import com.google.inject.Inject;

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

    public void generate(Path output) {
        boolean findAnyResults = false;
        for (TestCaseProvider provider : providers) {
            System.out.println("Found provider " + provider.getClass());
            for (TestCase testCase : provider) {
                findAnyResults = true;
                try (OutputStream stream = Files.newOutputStream(output.resolve(UUID.randomUUID().toString() + ".json"))) {
                    getMapper().writeValue(stream, testCase);
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                }
            }
        }
        if (!findAnyResults) {
            System.out.println("Could not find any results");
        }
    }

    private ObjectMapper getMapper() {
        return new ObjectMapper()
                .setAnnotationIntrospector(new JaxbAnnotationIntrospector(TypeFactory.defaultInstance()))
                .enable(SerializationFeature.INDENT_OUTPUT)
                .setSerializationInclusion(JsonInclude.Include.NON_NULL);
    }
}
