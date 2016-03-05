package org.allurefw.report;

import com.google.inject.AbstractModule;

import java.util.List;

/**
 * @author Dmitry Baev charlie@yandex-team.ru
 *         Date: 26.02.16
 */
public class ProcessModule extends AbstractModule {

    private final List<AbstractPlugin> processPlugins;

    public ProcessModule(List<AbstractPlugin> processPlugins) {
        this.processPlugins = processPlugins;
    }

    @Override
    protected void configure() {
        processPlugins.forEach(this::install);
    }
}
