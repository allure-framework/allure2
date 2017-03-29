package io.qameta.allure.junit;

import com.google.inject.multibindings.Multibinder;
import io.qameta.allure.AbstractPlugin;
import io.qameta.allure.ResultsProcessor;

/**
 * @author charlie (Dmitry Baev).
 */
public class JunitResultsPlugin extends AbstractPlugin {

    @Override
    protected void configure() {
        Multibinder.newSetBinder(binder(), ResultsProcessor.class)
                .addBinding().to(JunitResultsReader.class);
    }
}
