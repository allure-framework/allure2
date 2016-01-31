package org.allurefw.report;

import com.google.inject.Guice;
import com.google.inject.Injector;
import org.allurefw.report.allure1.Allure1Module;
import org.allurefw.report.junit.JunitModule;

import java.nio.file.Path;

/**
 * @author Dmitry Baev charlie@yandex-team.ru
 *         Date: 30.01.16
 */
public class ReportGenerator {

    private final Injector injector;

    public ReportGenerator(Path... inputs) {
        BootstrapModule bootstrap = new BootstrapModule(inputs);
        injector = Guice.createInjector(
                bootstrap,
                new Allure1Module(),
                new JunitModule()
        );
    }

    public void generate(Path output) {
        injector.getInstance(Lifecycle.class).generate();
    }
}
