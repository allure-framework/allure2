package org.allurefw.routes;

import org.apache.camel.builder.RouteBuilder;

/**
 * @author Dmitry Baev charlie@yandex-team.ru
 *         Date: 08.03.16
 */
public class AttachmentRouteBuilder extends RouteBuilder {

    @Override
    public void configure() throws Exception {
        from("direct:attachment")
                .log("attachment enqueued");
    }
}
