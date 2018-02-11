package io.qameta.allure.testdata;

import com.google.common.reflect.ClassPath;
import io.qameta.allure.entity.Statistic;
import io.qameta.allure.entity.TestResult;
import org.apache.commons.text.RandomStringGenerator;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.concurrent.ThreadLocalRandom.current;

/**
 * @since 2.0
 */
public final class TestData {

    private TestData() {
        throw new IllegalStateException("Do not instance");
    }

    public static void unpackFile(final String name, final Path output) throws IOException {
        try (InputStream is = TestData.class.getClassLoader().getResourceAsStream(name)) {
            Files.copy(is, output);
        }
    }

    public static void unpackDummyResources(String prefix, Path output) throws IOException {
        ClassPath classPath = ClassPath.from(TestData.class.getClassLoader());
        Map<String, URL> files = classPath.getResources().stream()
                .filter(info -> info.getResourceName().startsWith(prefix))
                .collect(Collectors.toMap(
                        info -> info.getResourceName().substring(prefix.length()),
                        ClassPath.ResourceInfo::url)
                );
        files.forEach((name, url) -> {
            Path file = output.resolve(name);
            try (InputStream is = url.openStream()) {
                Files.copy(is, file);
            } catch (IOException e) {
                throw new RuntimeException(String.format("name: %s, url: %s", name, url), e);
            }
        });
    }


    public static Statistic randomStatistic() {
        return new Statistic()
                .setFailed(current().nextLong(10))
                .setBroken(current().nextLong(10))
                .setPassed(current().nextLong(10))
                .setSkipped(current().nextLong(10))
                .setUnknown(current().nextLong(10));
    }

    public static TestResult randomTestResult() {
        return new TestResult().setName(randomString());
    }

    public static String randomString() {
        return new RandomStringGenerator.Builder()
                .withinRange('a', 'z').build()
                .generate(10);
    }
}
