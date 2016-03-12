package org.allurefw.report;

import org.allurefw.report.entity.TestCase;

import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Supplier;

/**
 * @author Dmitry Baev charlie@yandex-team.ru
 *         Date: 05.03.16
 */
public interface DataCollector<T> {

    Supplier<T> supplier();

    BinaryOperator<T> combiner();

    BiConsumer<T, TestCase> accumulator();

}
