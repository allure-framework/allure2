package io.qameta.allure.entity;

import java.util.Comparator;

import static java.util.Comparator.comparing;
import static java.util.Comparator.naturalOrder;
import static java.util.Comparator.nullsFirst;

/**
 * @author charlie (Dmitry Baev).
 */
public final class EntityComparators {

    private EntityComparators() {
        throw new IllegalStateException("Do not instance");
    }

    public static Comparator<TestResult> comparingTestResultsByStartAsc() {
        return comparing(TestResult::getStart, nullsFirst(naturalOrder()));
    }
}
