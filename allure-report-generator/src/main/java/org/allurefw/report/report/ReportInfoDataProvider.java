package org.allurefw.report.report;

import com.google.inject.Inject;
import com.google.inject.Provider;
import org.allurefw.report.Environment;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Dmitry Baev charlie@yandex-team.ru
 *         Date: 17.02.16
 */
public class ReportInfoDataProvider implements Provider<Object> {

    private final Environment environment;

    @Inject
    public ReportInfoDataProvider(Environment environment) {
        this.environment = environment;
    }

    @Override
    public Object get() {
        Map<String, String> data = new HashMap<>();
        data.put("name", environment.getName());
        data.put("url", environment.getUrl());
        return data;
    }
}
