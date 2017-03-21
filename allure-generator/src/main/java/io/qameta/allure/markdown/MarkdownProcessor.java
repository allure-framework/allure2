package io.qameta.allure.markdown;

import io.qameta.allure.Processor;
import io.qameta.allure.entity.TestCase;
import io.qameta.allure.entity.TestCaseResult;
import io.qameta.allure.entity.TestRun;

import javax.inject.Inject;

import static org.parboiled.common.StringUtils.isEmpty;
import static org.parboiled.common.StringUtils.isNotEmpty;

/**
 * @author charlie (Dmitry Baev).
 */
public class MarkdownProcessor implements Processor {

    private final MarkdownSupport markdownSupport;

    @Inject
    public MarkdownProcessor(final MarkdownSupport markdownSupport) {
        this.markdownSupport = markdownSupport;
    }

    @Override
    public void process(final TestRun testRun, final TestCase testCase, final TestCaseResult result) {
        if (isNotEmpty(result.getDescriptionHtml()) || isEmpty(result.getDescription())) {
            return;
        }
        final String html = markdownSupport.getProcessor().markdownToHtml(result.getDescription());
        result.setDescriptionHtml(html);
    }
}
