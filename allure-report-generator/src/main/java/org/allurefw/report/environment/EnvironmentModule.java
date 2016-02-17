package org.allurefw.report.environment;

import com.google.common.collect.Maps;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import org.allurefw.report.Environment;
import org.allurefw.report.ResultsDirectories;

import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static org.allurefw.report.ReportApiUtils.loadProperties;

/**
 * @author Dmitry Baev charlie@yandex-team.ru
 *         Date: 17.02.16
 */
public class EnvironmentModule extends AbstractModule {

    @Override
    protected void configure() {
        //do nothing
    }

    @Provides
    @Singleton
    public Environment provide(@ResultsDirectories Path... resultsDirectories) {
        Properties properties = loadProperties("environment.properties", resultsDirectories);
        Map<String, String> map = new HashMap<>(Maps.fromProperties(properties));
        String id = map.remove("allure.test.run.id");
        String name = map.remove("allure.test.run.name");
        String url = map.remove("allure.test.run.url");

        Map<String, String> parameters = Collections.unmodifiableMap(map);
        return new DefaultEnvironment(id, name, url, parameters);
    }
}
