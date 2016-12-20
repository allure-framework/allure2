package org.allurefw.report.executor;

import com.google.inject.multibindings.Multibinder;
import org.allurefw.report.AbstractPlugin;
import org.allurefw.report.TestRunDetailsReader;

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
