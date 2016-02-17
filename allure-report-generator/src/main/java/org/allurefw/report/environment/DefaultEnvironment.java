package org.allurefw.report.environment;

import org.allurefw.report.Environment;

import java.util.Collections;
import java.util.Map;

/**
 * @author Dmitry Baev charlie@yandex-team.ru
 *         Date: 17.02.16
 */
public class DefaultEnvironment implements Environment {

    private final String id;

    private final String name;

    private final String url;

    private final Map<String, String> parameters;

    public DefaultEnvironment(String id, String name, String url, Map<String, String> parameters) {
        this.id = id;
        this.name = name;
        this.url = url;
        this.parameters = parameters;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getUrl() {
        return url;
    }

    @Override
    public Map<String, String> getParameters() {
        return Collections.unmodifiableMap(parameters);
    }
}
