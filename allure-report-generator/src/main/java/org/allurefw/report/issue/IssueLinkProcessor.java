package org.allurefw.report.issue;

import com.google.inject.Inject;
import org.allurefw.report.Processor;
import org.allurefw.report.ReportConfig;
import org.allurefw.report.entity.LabelName;
import org.allurefw.report.entity.Link;
import org.allurefw.report.entity.TestCase;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Dmitry Baev charlie@yandex-team.ru
 *         Date: 18.02.16
 */
public class IssueLinkProcessor implements Processor {

    private final ReportConfig config;

    @Inject
    public IssueLinkProcessor(ReportConfig config) {
        this.config = config;
    }

    @Override
    public TestCase process(TestCase testCase) {
        List<Link> links = testCase.findAll(LabelName.ISSUE).stream()
                .map(s -> new Link()
                        .withName(s)
                        .withUrl(String.format(config.getIssueTrackerPattern(), s))
                        .withType(LabelName.ISSUE.value())
                ).collect(Collectors.toList());

        testCase.getLinks().addAll(links);
        return testCase;
    }
}
