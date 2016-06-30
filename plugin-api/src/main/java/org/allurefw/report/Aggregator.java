package org.allurefw.report;

import org.allurefw.report.entity.TestCaseResult;

import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * @author Dmitry Baev baev@qameta.io
 *         Date: 04.03.16
 */
public interface Aggregator<T> {

    Supplier<T> supplier();

    BinaryOperator<T> combiner();

    BiConsumer<T, TestCaseResult> accumulator();

}
