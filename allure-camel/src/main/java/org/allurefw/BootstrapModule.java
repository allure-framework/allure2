package org.allurefw;

import com.google.inject.multibindings.Multibinder;
import org.allurefw.api.ResultsReader;
import org.allurefw.routes.AttachmentRouteBuilder;
import org.allurefw.routes.TestCaseRouteBuilder;
import org.allurefw.routes.TestSuiteRouteBuilder;
import org.apache.camel.guice.CamelModuleWithMatchingRoutes;

/**
 * @author Dmitry Baev charlie@yandex-team.ru
 *         Date: 07.03.16
 */
public class BootstrapModule extends CamelModuleWithMatchingRoutes {

    @Override
    protected void configure() {
        super.configure();

        bind(TestCaseRouteBuilder.class);
        bind(TestSuiteRouteBuilder.class);
        bind(AttachmentRouteBuilder.class);

        Multibinder.newSetBinder(binder(), ResultsReader.class);
    }
}
