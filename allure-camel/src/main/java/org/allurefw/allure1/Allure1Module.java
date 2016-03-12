package org.allurefw.allure1;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;
import org.allurefw.api.ResultsReader;

/**
 * @author Dmitry Baev charlie@yandex-team.ru
 *         Date: 07.03.16
 */
public class Allure1Module extends AbstractModule {

    @SuppressWarnings("PointlessBinding")
    @Override
    protected void configure() {
        bind(Allure1RouteBuilder.class);
        bind(Allure1Result.class);

        Multibinder.newSetBinder(binder(), ResultsReader.class)
                .addBinding().toProvider(() -> new ResultsReader(".*-testsuite.xml", "direct:allure1:testSuiteXml"));

        Multibinder.newSetBinder(binder(), ResultsReader.class)
                .addBinding().toProvider(() -> new ResultsReader(".*-testsuite.json", "direct:allure1:testSuiteJson"));
    }
}
