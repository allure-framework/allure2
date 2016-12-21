package io.qameta.allure.executor;

import io.qameta.allure.TestRunAggregator;
import io.qameta.allure.entity.TestRun;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static io.qameta.allure.executor.ExecutorPlugin.EXECUTOR_BLOCK_NAME;

/**
 * @author charlie (Dmitry Baev).
 */
public class ExecutorAggregator implements TestRunAggregator<List<ExecutorWidgetItem>> {

    @Override
    public Supplier<List<ExecutorWidgetItem>> supplier() {
        return ArrayList::new;
    }

    @Override
    public Consumer<List<ExecutorWidgetItem>> aggregate(TestRun testRun) {
        return items -> {
            ExecutorWidgetItem item = new ExecutorWidgetItem();
            item.setName(testRun.getName());
            item.setInfo(testRun.getExtraBlock(EXECUTOR_BLOCK_NAME, null));
            items.add(item);
        };
    }
}
