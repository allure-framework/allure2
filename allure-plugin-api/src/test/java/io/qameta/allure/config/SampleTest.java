package io.qameta.allure.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author charlie (Dmitry Baev).
 */
public class SampleTest {

    @Test
    public void shouldParseConfig() throws IOException {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream("sample-config.yml")) {
            final ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
            mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
            final ReportConfig config = mapper.readValue(is, ReportConfig.class);

            System.out.println(config.getVersion());
            System.out.println(config.getProjectName());
            System.out.println("Categories:");
            config.getCategories().forEach(System.out::println);
            System.out.println("Custom fields:");
            config.getCustomFields().forEach(System.out::println);
            System.out.println("Tags:");
            config.getTags().forEach(System.out::println);
            System.out.println("Groups:");
            config.getGroups().forEach((key, groups) -> {
                System.out.println(key);
                System.out.println(groups);
            });
        }
    }
}
