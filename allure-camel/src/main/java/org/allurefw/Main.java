package org.allurefw;

import com.google.inject.Guice;
import com.google.inject.Injector;
import org.allurefw.allure1.Allure1Module;

/**
 * @author Dmitry Baev charlie@yandex-team.ru
 *         Date: 07.03.16
 */
public class Main {

    public static void main(String[] args) throws Exception {
        Injector injector = Guice.createInjector(
                new BootstrapModule(),
                new Allure1Module()
        );

        Starter starter = injector.getInstance(Starter.class);
        starter.start();

        Thread.sleep(5000);
    }
}
