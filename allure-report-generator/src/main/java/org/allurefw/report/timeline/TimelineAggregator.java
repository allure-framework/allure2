package org.allurefw.report.timeline;

import org.allurefw.report.Aggregator;
import org.allurefw.report.Host;
import org.allurefw.report.Thread;
import org.allurefw.report.TimelineData;
import org.allurefw.report.entity.LabelName;
import org.allurefw.report.entity.TestCase;

import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Supplier;

/**
 * @author Dmitry Baev charlie@yandex-team.ru
 *         Date: 17.04.16
 */
public class TimelineAggregator implements Aggregator<TimelineData> {

    @Override
    public Supplier<TimelineData> supplier() {
        return TimelineData::new;
    }

    @Override
    public BinaryOperator<TimelineData> combiner() {
        return (left, right) -> left.withHosts(right.getHosts());
    }

    @Override
    public BiConsumer<TimelineData, TestCase> accumulator() {
        return (identity, testCase) -> {
            String hostName = testCase.findOne(LabelName.HOST).orElse("Default hostname");
            String threadName = testCase.findOne(LabelName.THREAD).orElse("Default thread");

            Host host = identity.getHosts().stream()
                    .filter(item -> hostName.equals(item.getName()))
                    .findAny()
                    .orElseGet(() -> {
                        Host newOne = new Host().withName(hostName);
                        identity.getHosts().add(newOne);
                        return newOne;
                    });

            Thread thread = host.getThreads().stream()
                    .filter(item -> threadName.equals(item.getName()))
                    .findAny()
                    .orElseGet(() -> {
                        Thread newOne = new Thread().withName(threadName);
                        host.getThreads().add(newOne);
                        return newOne;
                    });

            thread.getTestCases().add(testCase.toInfo());
        };
    }
}
