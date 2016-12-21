package io.qameta.allure.junit;

import com.google.inject.multibindings.Multibinder;
import io.qameta.allure.AbstractPlugin;
import io.qameta.allure.ResultsReader;

/**
 * @author charlie (Dmitry Baev).
 */
public class JunitResultsPlugin extends AbstractPlugin {

    @Override
    protected void configure() {
        Multibinder.newSetBinder(binder(), ResultsReader.class)
                .addBinding().to(JunitResultsReader.class);
    }
}
