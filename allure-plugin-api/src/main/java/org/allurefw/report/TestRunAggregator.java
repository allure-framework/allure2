package org.allurefw.report;

import org.allurefw.report.entity.TestRun;

import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * @author Dmitry Baev baev@qameta.io
 *         Date: 04.03.16
 */
public interface TestRunAggregator<T> {

    Supplier<T> supplier();

    Consumer<T> aggregate(TestRun testRun);

}
