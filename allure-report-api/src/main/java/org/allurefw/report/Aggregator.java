package org.allurefw.report;

import org.allurefw.report.entity.TestCase;

import java.io.Serializable;

/**
 * @author Dmitry Baev charlie@yandex-team.ru
 *         Date: 04.03.16
 */
public interface Aggregator<T extends Serializable> {

    /**
     * an associative, non-interfering, stateless function for
     * incorporating an additional element into a result.
     *
     * @param identity the identity value for the accumulating function.
     * @param testCase the additional element to add it into a result.
     */
    void aggregate(T identity, TestCase testCase);

}
