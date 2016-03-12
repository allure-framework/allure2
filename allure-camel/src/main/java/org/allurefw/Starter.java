package org.allurefw;

import com.google.inject.Inject;
import org.allurefw.api.ResultsReader;
import org.apache.camel.CamelContext;
import org.apache.camel.builder.RouteBuilder;

import java.util.Set;

/**
 * @author Dmitry Baev charlie@yandex-team.ru
 *         Date: 07.03.16
 */
public class Starter {

    public static final String PATH = "/Users/charlie/IdeaProjects/allure-report/allure-report-generator/src/test/java/org/allurefw/report/allure1data";

    @Inject
    protected Set<ResultsReader> readers;

    @Inject
    protected CamelContext camelContext;

    public void start() throws Exception {
        for (ResultsReader reader : readers) {
            camelContext.addRoutes(new RouteBuilder() {
                @Override
                public void configure() throws Exception {
                    fromF("file:%s?include=%s&noop=true", PATH, reader.getInclude())
                            .to(reader.getQueueName());
                }
            });
        }
    }
}
