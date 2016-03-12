package org.allurefw.routes;

import org.apache.camel.builder.RouteBuilder;

/**
 * @author Dmitry Baev charlie@yandex-team.ru
 *         Date: 08.03.16
 */
public class TestSuiteRouteBuilder extends RouteBuilder {

    @Override
    public void configure() throws Exception {
        from("direct:testSuite")
                .log("test suite ${body.name} with uid ${body.uid} enqueued");
    }
}
