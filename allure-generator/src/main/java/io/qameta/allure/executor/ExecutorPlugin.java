package io.qameta.allure.executor;

import com.google.inject.multibindings.Multibinder;
import io.qameta.allure.AbstractPlugin;
import io.qameta.allure.TestRunDetailsReader;

/**
 * @author charlie (Dmitry Baev).
 */
public class ExecutorPlugin extends AbstractPlugin {

    public static final String EXECUTOR_BLOCK_NAME = "executor";

    public static final String EXECUTOR_FILE_NAME = "executor.json";

    @Override
    protected void configure() {
        Multibinder.newSetBinder(binder(), TestRunDetailsReader.class)
                .addBinding().to(ExecutorReader.class);

        aggregateTestRuns(ExecutorAggregator.class)
                .toWidget("executors");
    }
}
