package io.qameta.allure.markdown;

import io.qameta.allure.LaunchResults;
import io.qameta.allure.Processor;
import io.qameta.allure.ReportConfiguration;

import java.util.List;

import static org.parboiled.common.StringUtils.isEmpty;
import static org.parboiled.common.StringUtils.isNotEmpty;

/**
 * @author charlie (Dmitry Baev).
 */
public class MarkdownProcessor implements Processor {

    @Override
    public void process(ReportConfiguration configuration, List<LaunchResults> launches) {
        configuration.getContext(MarkdownContext.class).ifPresent(markdownContext -> launches.stream()
                .flatMap(launch -> launch.getResults().stream())
                .filter(result -> isNotEmpty(result.getDescriptionHtml()) || isEmpty(result.getDescription()))
                .forEach(result -> {
                    final String html = markdownContext.getValue().markdownToHtml(result.getDescription());
                    result.setDescriptionHtml(html);
                }));
    }

}
