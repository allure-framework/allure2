package io.qameta.allure;

import org.junit.Test;

import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

/**
 * @author charlie (Dmitry Baev).
 */
public class DirectoryWatcherTest {

    @Test
    public void shouldWaitForUpload() throws InterruptedException {
        final DirectoryWatcher watcher = new DirectoryWatcher();
        watcher.setBatchSize(10);
        watcher.setMaxDepth(1);
        watcher.setIndexInterval(1, TimeUnit.SECONDS);
        watcher.setProcessInterval(1, TimeUnit.SECONDS);
        watcher.watch(file -> {
            try {
                System.out.println("Start processing file " + file);
                Thread.sleep(1500);
                System.out.println("End of processing file " + file);
            } catch (InterruptedException e) {
                System.out.println(e.getMessage());
            }
        }, Paths.get("/Users/charlie/projects/allure2/allure-generator/test-data"));

        watcher.shutdown();
        watcher.awaitTermination(10, TimeUnit.SECONDS);
    }
}