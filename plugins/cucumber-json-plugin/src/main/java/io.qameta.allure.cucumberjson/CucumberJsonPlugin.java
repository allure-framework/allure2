package io.qameta.allure.cucumberjson;

import com.google.inject.multibindings.Multibinder;
import io.qameta.allure.AbstractPlugin;
import io.qameta.allure.ResultsProcessor;

/**
 * @author Egor Borisov ehborisov@gmail.com
 */
public class CucumberJsonPlugin extends AbstractPlugin {

    @Override
    protected void configure() {
        Multibinder.newSetBinder(binder(), ResultsProcessor.class)
                .addBinding().to(CucumberJsonResultsReader.class);
    }
}
