package io.qameta.allure.testdata;

import com.google.common.reflect.ClassPath;
import io.qameta.allure.entity.Attachment;
import io.qameta.allure.entity.TestCaseResult;
import io.qameta.allure.entity.TestGroup;
import org.apache.commons.lang3.RandomStringUtils;
import org.zeroturnaround.zip.ZipUtil;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

/**
 * @author charlie (Dmitry Baev).
 */
public final class TestData {

    TestData() {
    }

    public static String randomContent() {
        return RandomStringUtils.randomAlphabetic(50);
    }

    public static String randomString() {
        return RandomStringUtils.randomAlphabetic(10);
    }

    public static String randomName() {
        return RandomStringUtils.randomAlphabetic(7);
    }

    public static long randomLong() {
        return ThreadLocalRandom.current().nextLong();
    }

    public static String randomFileName() {
        return randomString() + ".txt";
    }

    public static Attachment randomAttachment() {
        return new Attachment()
                .withName(randomName())
                .withType("text/plain")
                .withSize(randomLong())
                .withUid(randomString())
                .withSource(randomFileName());
    }

    public static TestCaseResult randomTestCase() {
        return new TestCaseResult().withName("some test case");
    }

    public static TestGroup randomTestGroup() {
        return new TestGroup()
                .withName("some group name");
    }

    public static void unpackDummyPlugin(Path pluginDirectory) throws IOException {
        try (InputStream is = TestData.class.getClassLoader().getResourceAsStream("dummy-plugin.zip")) {
            ZipUtil.unwrap(is, pluginDirectory.toFile());
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
}
