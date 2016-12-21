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
