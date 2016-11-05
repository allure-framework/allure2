package org.allurefw.report.markdown;

import org.allurefw.report.Processor;
import org.allurefw.report.entity.TestCase;
import org.allurefw.report.entity.TestCaseResult;
import org.allurefw.report.entity.TestRun;

import javax.inject.Inject;

import static org.parboiled.common.StringUtils.isEmpty;
import static org.parboiled.common.StringUtils.isNotEmpty;

/**
 * @author charlie (Dmitry Baev).
 */
public class MarkdownProcessor implements Processor {

    private final MarkdownSupport markdownSupport;

    @Inject
    public MarkdownProcessor(MarkdownSupport markdownSupport) {
        this.markdownSupport = markdownSupport;
    }

    @Override
    public void process(TestRun testRun, TestCase testCase, TestCaseResult result) {
        if (isNotEmpty(result.getDescriptionHtml()) || isEmpty(result.getDescription())) {
            return;
        }
        String html = markdownSupport.getProcessor().markdownToHtml(result.getDescription());
        result.setDescriptionHtml(html);
    }
}
