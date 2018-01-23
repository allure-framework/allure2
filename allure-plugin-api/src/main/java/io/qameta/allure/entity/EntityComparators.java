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

    public static Comparator<TestParameter> comparingParametersByNameAndValue() {
        return comparing(TestParameter::getName, nullsFirst(naturalOrder()))
                .thenComparing(TestParameter::getValue, nullsFirst(naturalOrder()));
    }

    public static Comparator<EnvironmentVariable> comparingEnvironmentByKeyAndValue() {
        return comparing(EnvironmentVariable::getKey, nullsFirst(naturalOrder()))
                .thenComparing(EnvironmentVariable::getValue, nullsFirst(naturalOrder()));
    }
}
