package org.allurefw.report.issue;

import com.google.inject.Inject;
import org.allurefw.LabelName;
import org.allurefw.report.ReportConfig;
import org.allurefw.report.TestCasePreparer;
import org.allurefw.report.entity.Link;
import org.allurefw.report.entity.TestCase;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Dmitry Baev charlie@yandex-team.ru
 *         Date: 18.02.16
 */
public class IssuePlugin implements TestCasePreparer {

    private final ReportConfig config;

    @Inject
    public IssuePlugin(ReportConfig config) {
        this.config = config;
    }

    @Override
    public void prepare(TestCase testCase) {
        List<Link> links = testCase.findAll(LabelName.ISSUE).stream()
                .map(s -> new Link()
                        .withName(s)
                        .withUrl(String.format(config.getIssueTrackerPattern(), s))
                        .withType(LabelName.ISSUE.value())
                ).collect(Collectors.toList());

        testCase.getLinks().addAll(links);
    }
}
