package io.qameta.allure.testdata;

import com.google.common.reflect.ClassPath;
import io.qameta.allure.DefaultLaunchResults;
import io.qameta.allure.core.LaunchResults;
import io.qameta.allure.entity.TestResult;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @since 2.0
 */
public final class TestData {

    private TestData() {
    }

    public static List<LaunchResults> createSingleLaunchResults(TestResult... input) {
        return createSingleLaunchResults(new HashMap<>(), input);
    }

    public static List<LaunchResults> createSingleLaunchResults(Map<String, Object> extra, TestResult... input) {
        List<LaunchResults> launchResultsList = new ArrayList<>();
        launchResultsList.add(new DefaultLaunchResults(Arrays.stream(input).collect(Collectors.toSet()), null, extra));
        return launchResultsList;
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
}
