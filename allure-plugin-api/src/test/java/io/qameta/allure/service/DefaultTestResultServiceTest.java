package io.qameta.allure.service;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import io.qameta.allure.config.ReportConfig;
import io.qameta.allure.entity.TestLabel;
import io.qameta.allure.entity.TestResult;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author charlie (Dmitry Baev).
 */
public class DefaultTestResultServiceTest {

    @Test
    public void shouldCreateTestResult() throws IOException {
        final TestResult result = new TestResult();
        result.getLabels().add(new TestLabel().setName("os").setValue("HUOS"));

        final DefaultTestResultService service = new DefaultTestResultService(getConfig());
        final TestResult created = service.create(result);

        System.out.println(created);
    }

    public static ReportConfig getConfig() throws IOException {
        try (InputStream is = DefaultTestResultServiceTest.class.getClassLoader().getResourceAsStream("sample-config.yml")) {
            final ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
            mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
            return mapper.readValue(is, ReportConfig.class);
        }
    }
}