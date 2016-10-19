package org.allurefw.report.executor;

import org.allurefw.report.TestRunAggregator;
import org.allurefw.report.entity.ExecutorInfo;
import org.allurefw.report.entity.TestRun;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static org.allurefw.report.executor.ExecutorPlugin.EXECUTOR_BLOCK_NAME;

/**
 * @author charlie (Dmitry Baev).
 */
public class ExecutorAggregator implements TestRunAggregator<List<ExecutorInfo>> {

    @Override
    public Supplier<List<ExecutorInfo>> supplier() {
        return ArrayList::new;
    }

    @Override
    public Consumer<List<ExecutorInfo>> aggregate(TestRun testRun) {
        return executors -> {
            if (testRun.hasExtraBlock(EXECUTOR_BLOCK_NAME)) {
                executors.add(testRun.getExtraBlock(EXECUTOR_BLOCK_NAME));
            }
        };
    }
}
