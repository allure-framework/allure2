package org.allurefw.routes;

import org.apache.camel.builder.RouteBuilder;

/**
 * @author Dmitry Baev charlie@yandex-team.ru
 *         Date: 07.03.16
 */
public class TestCaseRouteBuilder extends RouteBuilder {

    @Override
    public void configure() throws Exception {
        from("direct:testCase")
            .log("test case ${body.name} with uid ${body.uid} enqueued");
    }
}
