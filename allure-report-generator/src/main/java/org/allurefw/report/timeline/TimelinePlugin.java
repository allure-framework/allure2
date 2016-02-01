package org.allurefw.report.timeline;

import com.google.inject.Inject;
import org.allurefw.LabelName;
import org.allurefw.report.Host;
import org.allurefw.report.TestCaseProcessor;
import org.allurefw.report.Thread;
import org.allurefw.report.TimelineData;
import org.allurefw.report.entity.TestCase;

/**
 * @author Dmitry Baev charlie@yandex-team.ru
 *         Date: 01.02.16
 */
public class TimelinePlugin implements TestCaseProcessor {

    @Inject
    protected TimelineData data;

    @Override
    public void process(TestCase testCase) {
        String hostName = testCase.findOne(LabelName.HOST).orElse("Default hostname");
        String threadName = testCase.findOne(LabelName.THREAD).orElse("Default thread");

        Host host = data.getHosts().stream()
                .filter(hostName::equals)
                .findAny()
                .orElseGet(() -> {
                    Host newOne = new Host().withName(hostName);
                    data.getHosts().add(newOne);
                    return newOne;
                });

        Thread thread = host.getThreads().stream()
                .filter(threadName::equals)
                .findAny()
                .orElseGet(() -> {
                    Thread newOne = new Thread().withName(threadName);
                    host.getThreads().add(newOne);
                    return newOne;
                });

        thread.getTestCases().add(testCase.toInfo());
    }
}
