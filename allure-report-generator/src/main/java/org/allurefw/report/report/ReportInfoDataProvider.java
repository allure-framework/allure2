package org.allurefw.report.report;

import com.google.inject.Inject;
import org.allurefw.report.Environment;
import org.allurefw.report.ReportDataProvider;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Dmitry Baev charlie@yandex-team.ru
 *         Date: 17.02.16
 */
public class ReportInfoDataProvider implements ReportDataProvider {

    private final Environment environment;

    @Inject
    public ReportInfoDataProvider(Environment environment) {
        this.environment = environment;
    }

    @Override
    public Object provide() {
        Map<String, String> data = new HashMap<>();
        data.put("name", environment.getName());
        data.put("url", environment.getUrl());
        return data;
    }

    @Override
    public String getFileName() {
        return "report.json";
    }
}
