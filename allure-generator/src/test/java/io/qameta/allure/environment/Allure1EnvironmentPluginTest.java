package io.qameta.allure.environment;

import io.qameta.allure.ConfigurationBuilder;
import io.qameta.allure.DefaultResultsVisitor;
import io.qameta.allure.allure1.Allure1Plugin;
import io.qameta.allure.core.Configuration;
import io.qameta.allure.core.LaunchResults;
import io.qameta.allure.entity.Environment;
import io.qameta.allure.entity.EnvironmentItem;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;

import static org.allurefw.allure1.AllureUtils.generateTestSuiteJsonName;
import static org.allurefw.allure1.AllureUtils.generateTestSuiteXmlName;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Egor Borisov ehborisov@gmail.com
 */
public class Allure1EnvironmentPluginTest {

    @ClassRule
    public static TemporaryFolder folder = new TemporaryFolder();

    @Test
    public void shouldReadEnvironmentProperties() throws Exception {

        EnvironmentItem[] expected = new EnvironmentItem[]{
                new EnvironmentItem().withName("allure.test.run.id").withValues("some-id"),
                new EnvironmentItem().withName("allure.test.run.name").withValues("some-name"),
                new EnvironmentItem().withName("allure.test.property").withValues("1")
        };

        Environment environment = process(
                "allure1/sample-testsuite.json", generateTestSuiteJsonName(),
                "allure1/environment.properties", "environment.properties"
        );

        assertThat(environment.getEnvironmentItems())
                .as("Unexpected environment properties have been read from properties file")
                .hasSize(3)
                .usingFieldByFieldElementComparator()
                .containsExactlyInAnyOrder(expected);
    }

    @Test
    public void shouldReadEnvironmentXml() throws Exception {
        EnvironmentItem[] expected = new EnvironmentItem[]{
                new EnvironmentItem().withName("my.properties.browser").withValues("Firefox"),
                new EnvironmentItem().withName("my.properties.url").withValues("http://yandex.ru"),
        };

        Environment environment = process(
                "allure1/sample-testsuite.json", generateTestSuiteJsonName(),
                "allure1/environment.xml", "environment.xml"
        );

        assertThat(environment.getEnvironmentItems())
                .as("Unexpected environment properties have been read from xml file")
                .hasSize(2)
                .usingFieldByFieldElementComparator()
                .containsExactlyInAnyOrder(expected);
    }

    @Test
    public void shouldStackParameterValues() throws Exception {
        EnvironmentItem[] expected = new EnvironmentItem[]{
                new EnvironmentItem().withName("allure.test.run.id").withValues("some-id"),
                new EnvironmentItem().withName("allure.test.run.name").withValues("some-name"),
                new EnvironmentItem().withName("allure.test.property").withValues("1", "2"),
                new EnvironmentItem().withName("allure.test.other.property").withValues("value")
        };

        Environment environment = process(
                "allure1/environment-variables-testsuite.xml", generateTestSuiteXmlName(),
                "allure1/environment.properties", "environment.properties"
        );

        assertThat(environment.getEnvironmentItems())
                .as("Unexpected environment properties have been read from test results and properties file")
                .hasSize(4)
                .usingFieldByFieldElementComparator()
                .containsExactlyInAnyOrder(expected);
    }

    private Environment process(String... strings) throws IOException {
        Path resultsDirectory = folder.newFolder().toPath();
        Iterator<String> iterator = Arrays.asList(strings).iterator();
        while (iterator.hasNext()) {
            String first = iterator.next();
            String second = iterator.next();
            copyFile(resultsDirectory, first, second);
        }
        Allure1Plugin reader = new Allure1Plugin();
        final Configuration configuration = new ConfigurationBuilder().useDefault().build();
        final DefaultResultsVisitor resultsVisitor = new DefaultResultsVisitor(configuration);
        reader.readResults(configuration, resultsVisitor, resultsDirectory);
        Allure1EnvironmentPlugin envPlugin = new Allure1EnvironmentPlugin();
        LaunchResults results = resultsVisitor.getLaunchResults();
        return envPlugin.getData(configuration, Collections.singletonList(results));
    }

    private void copyFile(Path dir, String resourceName, String fileName) throws IOException {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(resourceName)) {
            Files.copy(is, dir.resolve(fileName));
        }
    }
}
