package io.qameta.allure;

import org.junit.Test;

import java.nio.file.Paths;

/**
 * @author charlie (Dmitry Baev).
 */
public class MainTest {

    @Test
    public void name() throws InterruptedException {
        final DirectoryWatcher watcher = new DirectoryWatcher();
        watcher.watch(
                files -> files.forEach(System.out::println),
                Paths.get("/Users/charlie/projects/allure2/allure-generator/test-data/demo2"),
                Paths.get("/Users/charlie/projects/allure2/allure-generator/test-data/demo")
        );

        watcher.stop();
        watcher.waitCompletion();
    }
}