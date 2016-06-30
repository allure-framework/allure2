package org.allurefw.report;

import com.google.common.reflect.ClassPath;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.allurefw.report.entity.TestCaseResult;
import org.allurefw.report.entity.TestGroup;
import org.allurefw.report.testdata.TestData;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

/**
 * @author charlie (Dmitry Baev).
 */
public class ProcessStageTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Test
    public void shouldGenerateFromAllure1Results() throws Exception {
        Path output = Paths.get("target/allure1-report");
        new ReportGenerator(getDataFolder("allure1data/")).generate(output);
    }

    @Test
    public void shouldGenerateFromJunitResults() throws Exception {
        Path output = Paths.get("target/junit-report");
        new ReportGenerator(getDataFolder("junitdata/")).generate(output);
    }

    @Test
    public void shouldInjectTestCases() throws Exception {
        TestsResults mock = mock(TestsResults.class);
        TestCaseResult testCase = TestData.randomTestCase();
        doReturn(Collections.singletonList(testCase)).when(mock).getTestCases();

        SomeClassWithTestCases instance = getInjector(mock).getInstance(SomeClassWithTestCases.class);

        assertThat(instance.testCases, notNullValue());
        assertThat(instance.testCases, hasSize(1));
        TestCaseResult next = instance.testCases.iterator().next();
        assertThat(next, is(testCase));
    }

    @Test
    public void shouldInjectTestGroups() throws Exception {
        TestsResults mock = mock(TestsResults.class);
        TestGroup info = TestData.randomTestGroup().withType("suite");
        doReturn(Collections.singletonList(info)).when(mock).getTestGroups();

        SomeClassWithTestGroups instance = getInjector(mock).getInstance(SomeClassWithTestGroups.class);

        assertThat(instance.testGroups, notNullValue());
        assertThat(instance.testGroups.keySet(), hasSize(1));
        assertThat(instance.testGroups, hasEntry(info.getName(), info));
    }

    private Path getDataFolder(String prefix) throws IOException {
        ClassPath classPath = ClassPath.from(getClass().getClassLoader());
        Map<String, URL> files = classPath.getResources().stream()
                .filter(info -> info.getResourceName().startsWith(prefix))
                .collect(Collectors.toMap(
                        info -> info.getResourceName().substring(prefix.length()),
                        ClassPath.ResourceInfo::url)
                );
        Path dir = folder.newFolder().toPath();
        files.forEach((name, url) -> {
            Path file = dir.resolve(name);
            try (InputStream is = url.openStream()) {
                Files.copy(is, file);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        return dir;
    }

    private Injector getInjector(TestsResults mock) {
        return Guice.createInjector(
                new ProcessStageModule(Collections.singletonList(mock), Collections.emptyList())
        );
    }

    private static class SomeClassWithTestCases {

        @Inject
        public Set<TestCaseResult> testCases;

    }

    private static class SomeClassWithTestGroups {

        @Inject
        @Named("suite")
        public Map<String, TestGroup> testGroups;

    }
}
