package org.allurefw.report.timeline;

import org.allurefw.LabelName;
import org.allurefw.report.AbstractPlugin;
import org.allurefw.report.Host;
import org.allurefw.report.Plugin;
import org.allurefw.report.Thread;
import org.allurefw.report.TimelineData;
import org.allurefw.report.entity.TestCase;

/**
 * @author Dmitry Baev charlie@yandex-team.ru
 *         Date: 01.02.16
 */
@Plugin(name = "timeline")
public class TimelinePlugin extends AbstractPlugin {

    @Override
    protected void configure() {
        TimelineData timelineData = new TimelineData();

        aggregator(timelineData, this::aggregate);
        reportData(timelineData);
    }

    protected void aggregate(TimelineData identity, TestCase testCase) {
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
    }
}
