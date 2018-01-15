package io.qameta.allure;

import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.schedulers.Schedulers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.reactivex.Observable.interval;

/**
 * @author charlie (Dmitry Baev).
 */
public class DirectoryWatcher {

    private static final Logger LOGGER = LoggerFactory.getLogger(DirectoryWatcher.class);

    private final Set<Path> processedFiles = new CopyOnWriteArraySet<>();
    private final Set<Path> indexedFiles = new CopyOnWriteArraySet<>();

    private final AtomicBoolean stop = new AtomicBoolean();
    private final AtomicBoolean completed = new AtomicBoolean();

    private int batchSize = 10;

    private int processInterval = 2;
    private TimeUnit processIntervalUnit = TimeUnit.SECONDS;

    private int indexInterval = 1;
    private TimeUnit indexIntervalUnit = TimeUnit.SECONDS;

    public void watch(final Consumer<Path> fileConsumer, final Path... resultsDirectories) {
        final List<Observable<Path>> list = Stream.of(resultsDirectories)
                .map(this::getIndexer)
                .map(Observable::create)
                .collect(Collectors.toList());

        interval(indexInterval, indexIntervalUnit, Schedulers.newThread())
                .takeUntil(aLong -> {
                    return stop.get();
                })
                .flatMap(aLong -> Observable.concat(list))
                .flatMap(Observable::just)
                .zipWith(
                        interval(processInterval, processIntervalUnit, Schedulers.newThread()),
                        (paths, aLong) -> paths
                )
                .subscribeOn(Schedulers.computation())
                .subscribe(
                        path -> {
                            processedFiles.add(path);
                            fileConsumer.accept(path);
                        },
                        throwable -> LOGGER.error("Could not process files", throwable),
                        () -> completed.set(true)
                );
    }

    private ObservableOnSubscribe<Path> getIndexer(final Path resultsDirectory) {
        return emitter -> {
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(resultsDirectory)) {
                for (Path path : stream) {
                    if (!indexedFiles.contains(path)) {
                        indexedFiles.add(path);
                        emitter.onNext(path);
                    }
                }
                emitter.onComplete();
            } catch (Exception e) {
                emitter.onError(e);
            }
        };
    }

    public void stop() {
        stop.set(true);
    }

    public void waitCompletion() throws InterruptedException {
        while (!completed.get()) {
            Thread.sleep(100);
        }
    }

    public Set<Path> getProcessedFiles() {
        return processedFiles;
    }

    public Set<Path> getIndexedFiles() {
        return indexedFiles;
    }

    public void setBatchSize(final int batchSize) {
        this.batchSize = batchSize;
    }

    public void setProcessInterval(final int interval, final TimeUnit unit) {
        this.processInterval = interval;
        this.processIntervalUnit = unit;
    }

    public void setReadInterval(final int interval, final TimeUnit unit) {
        this.indexInterval = interval;
        this.indexIntervalUnit = unit;
    }

    public int getBatchSize() {
        return batchSize;
    }

    public int getProcessInterval() {
        return processInterval;
    }

    public TimeUnit getProcessIntervalUnit() {
        return processIntervalUnit;
    }

    public int getIndexInterval() {
        return indexInterval;
    }

    public TimeUnit getIndexIntervalUnit() {
        return indexIntervalUnit;
    }
}
