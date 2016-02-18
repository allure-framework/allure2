package org.allurefw.report.tms;

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
public class TmsPlugin implements TestCasePreparer {

    private final ReportConfig config;

    @Inject
    public TmsPlugin(ReportConfig config) {
        this.config = config;
    }

    @Override
    public void prepare(TestCase testCase) {
        List<Link> links = testCase.findAll(LabelName.TEST_ID).stream()
                .map(s -> new Link()
                        .withName(s)
                        .withUrl(String.format(config.getTmsPattern(), s))
                        .withType(LabelName.TEST_ID.value())
                ).collect(Collectors.toList());

        testCase.getLinks().addAll(links);
    }
}
