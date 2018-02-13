package io.qameta.allure;

import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.functions.Predicate;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.reactivex.Observable.interval;
import static java.util.Collections.unmodifiableSet;

/**
 * @author charlie (Dmitry Baev).
 */
public class DirectoryWatcher {

    private static final Logger LOGGER = LoggerFactory.getLogger(DirectoryWatcher.class);

    private final Set<Path> processedFiles = new CopyOnWriteArraySet<>();
    private final Set<Path> indexedFiles = new CopyOnWriteArraySet<>();
    private final Set<Path> errors = new CopyOnWriteArraySet<>();

    private final CountDownLatch completed = new CountDownLatch(1);
    private final AtomicBoolean stop = new AtomicBoolean();
    private final AtomicBoolean started = new AtomicBoolean();

    private int maxDepth = Integer.MAX_VALUE;
    private int batchSize = 10;

    private int processInterval = 2;
    private TimeUnit processIntervalUnit = TimeUnit.SECONDS;

    private int indexInterval = 1;
    private TimeUnit indexIntervalUnit = TimeUnit.SECONDS;

    private int uploadThreadsCount = 3;

    private DisposableObserver<List<Path>> observer;

    public void watch(final Consumer<List<Path>> filesConsumer, final Path... resultsDirectories) {
        started.set(true);

        final List<Observable<List<Path>>> list = Stream.of(resultsDirectories)
                .map(this::getIndexer)
                .map(Observable::create)
                .collect(Collectors.toList());

        final ExecutorService pool = Executors.newFixedThreadPool(getUploadThreadsCount());

        final Function<List<Path>, List<Path>> processing = files -> {
            try {
                filesConsumer.accept(files);
            } catch (Exception e) {
                LOGGER.error("Could not process files", e);
                errors.addAll(files);
                return Collections.emptyList();
            }
            return files;
        };

        observer = interval(indexInterval, indexIntervalUnit, Schedulers.io())
                .takeUntil(getStopPredicate())
                .flatMap(aLong -> Observable.concat(list))
                .flatMap(Observable::fromIterable)
                .buffer(processInterval, processIntervalUnit, batchSize)
                .flatMap(files -> Observable.just(files).subscribeOn(Schedulers.from(pool)).map(processing::apply))
                .subscribeWith(new DisposableObserver<List<Path>>() {
                    @Override
                    public void onNext(final List<Path> paths) {
                        processedFiles.addAll(paths);
                    }

                    @Override
                    public void onError(final Throwable e) {
                        LOGGER.error("Could not process files", e);
                    }

                    @Override
                    public void onComplete() {
                        completed.countDown();
                    }
                });
    }

    protected Predicate<Long> getStopPredicate() {
        return aLong -> isStopped();
    }

    private ObservableOnSubscribe<List<Path>> getIndexer(final Path resultsDirectory) {
        return emitter -> {
            if (Files.notExists(resultsDirectory)) {
                emitter.onComplete();
                return;
            }

            try (Stream<Path> stream = Files.walk(resultsDirectory, maxDepth)) {
                final List<Path> files = stream.filter(indexedFiles::add).collect(Collectors.toList());
                emitter.onNext(files);
                emitter.onComplete();
            } catch (Exception e) {
                emitter.onError(e);
            }
        };
    }

    public void shutdown() {
        stop.set(true);
    }

    public void shutdownNow() {
        stop.set(true);
        if (Objects.nonNull(observer)) {
            observer.dispose();
        }
    }

    public boolean awaitTermination(final long timeout, final TimeUnit unit) throws InterruptedException {
        return completed.await(timeout, unit);
    }

    public boolean isStopped() {
        return stop.get();
    }

    public boolean isCompleted() {
        return 0 == completed.getCount();
    }

    public Set<Path> getProcessedFiles() {
        return unmodifiableSet(processedFiles);
    }

    public Set<Path> getIndexedFiles() {
        return unmodifiableSet(indexedFiles);
    }

    public Set<Path> getErrors() {
        return unmodifiableSet(errors);
    }

    public void setBatchSize(final int batchSize) {
        this.batchSize = batchSize;
    }

    public void setProcessInterval(final int interval, final TimeUnit unit) {
        this.processInterval = interval;
        this.processIntervalUnit = unit;
    }

    public void setIndexInterval(final int interval, final TimeUnit unit) {
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

    public int getMaxDepth() {
        return maxDepth;
    }

    public void setMaxDepth(final int maxDepth) {
        this.maxDepth = maxDepth;
    }

    public int getUploadThreadsCount() {
        return uploadThreadsCount;
    }

    public void setUploadThreadsCount(final int uploadThreadsCount) {
        this.uploadThreadsCount = uploadThreadsCount;
    }
}
