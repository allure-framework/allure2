package io.qameta.allure.core;

import io.qameta.allure.Aggregator;
import io.qameta.allure.Context;
import io.qameta.allure.Reader;
import io.qameta.allure.Widget;

import java.util.List;

/**
 * @author charlie (Dmitry Baev).
 */
public interface Plugin {

    String getName();

    List<Aggregator> getAggregators();

    List<Reader> getReaders();

    List<Widget> getWidgets();

    List<Context> getContexts();

}
