package io.qameta.allure;

import io.qameta.allure.allure1.Allure1Reader;
import io.qameta.allure.attachment.AllureAttachmentsReader;
import io.qameta.allure.junit.JunitReader;
import io.qameta.allure.logging.LoggingResultsVisitor;
import io.qameta.allure.trx.TrxReader;
import io.qameta.allure.xctest.XcTestReader;
import io.qameta.allure.xunit.XunitReader;
import org.junit.Test;

import java.nio.file.Paths;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

/**
 * @author charlie (Dmitry Baev).
 */
public class MainTest {

    @Test
    public void name() throws InterruptedException {
        final DirectoryWatcher watcher = new DirectoryWatcher();
        watcher.watch(
                System.out::println,
                Paths.get("/Users/charlie/projects/allure2/allure-reader/build/a"),
                Paths.get("/Users/charlie/projects/allure2/allure-reader/build/b")
        );

        watcher.shutdown();
        watcher.awaitTermination(1, TimeUnit.MINUTES);
        watcher.shutdownNow();
    }

    @Test
    public void name2() throws InterruptedException {
        final LoggingResultsVisitor visitor = new LoggingResultsVisitor();
        final CompositeResultsReader reader = new CompositeResultsReader(Arrays.asList(
                new Allure1Reader(),
                new AllureAttachmentsReader(),
                new JunitReader(),
                new TrxReader(),
                new XcTestReader(),
                new XunitReader()
        ));
        final DirectoryWatcher watcher = new DirectoryWatcher();
        watcher.watch(
                files -> files.forEach(file -> reader.readResultFile(visitor, file)),
                Paths.get("/Users/charlie/projects/allure2/allure-generator/test-data/demo")
        );

        watcher.shutdown();
        watcher.awaitTermination(1, TimeUnit.MINUTES);
        watcher.shutdownNow();
    }
}