package io.qameta.allure.plugins;

import io.qameta.allure.Aggregator;
import io.qameta.allure.Context;
import io.qameta.allure.Reader;
import io.qameta.allure.Widget;
import io.qameta.allure.core.Plugin;

import java.util.Collections;
import java.util.List;

/**
 * @author charlie (Dmitry Baev).
 */
public class DefaultPlugin implements Plugin {

    private final String name;

    private final List<Aggregator> aggregators;

    private final List<Reader> readers;

    private final List<Widget> widgets;

    private final List<Context> contexts;

    public DefaultPlugin(final String name,
                         final List<Aggregator> aggregators,
                         final List<Reader> readers,
                         final List<Widget> widgets,
                         final List<Context> contexts) {
        this.name = name;
        this.aggregators = aggregators;
        this.readers = readers;
        this.widgets = widgets;
        this.contexts = contexts;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public List<Aggregator> getAggregators() {
        return Collections.unmodifiableList(aggregators);
    }

    @Override
    public List<Reader> getReaders() {
        return Collections.unmodifiableList(readers);
    }

    @Override
    public List<Widget> getWidgets() {
        return Collections.unmodifiableList(widgets);
    }

    @Override
    public List<Context> getContexts() {
        return Collections.unmodifiableList(contexts);
    }
}
