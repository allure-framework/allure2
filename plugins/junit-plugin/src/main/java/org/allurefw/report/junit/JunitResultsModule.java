package org.allurefw.report.junit;

import com.google.inject.multibindings.Multibinder;
import org.allurefw.report.AbstractPlugin;
import org.allurefw.report.ResultsReader;

/**
 * @author charlie (Dmitry Baev).
 */
public class JunitResultsModule extends AbstractPlugin {

    @Override
    protected void configure() {
        Multibinder.newSetBinder(binder(), ResultsReader.class)
                .addBinding().to(JunitResultsReader.class);
    }
}
