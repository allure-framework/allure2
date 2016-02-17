package org.allurefw.report.xunit;

import com.google.inject.Inject;
import org.allurefw.report.WidgetDataProvider;
import org.allurefw.report.XunitData;
import org.allurefw.report.XunitWidgetData;

import java.util.stream.Collectors;

/**
 * @author Dmitry Baev charlie@yandex-team.ru
 *         Date: 17.02.16
 */
public class XunitWidgetDataProvider implements WidgetDataProvider {

    protected final XunitData data;

    @Inject
    protected XunitWidgetDataProvider(XunitData data) {
        this.data = data;
    }

    @Override
    public Object provide() {
        return data.getTestSuites().stream()
                .limit(10)
                .map(testSuite -> new XunitWidgetData()
                        .withUid(testSuite.getUid())
                        .withName(testSuite.getName()))
                .collect(Collectors.toList());
    }

    @Override
    public String getWidgetId() {
        return "xunit";
    }
}
