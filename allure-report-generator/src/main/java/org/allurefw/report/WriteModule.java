package org.allurefw.report;

import com.google.inject.AbstractModule;

import java.util.List;

/**
 * @author Dmitry Baev charlie@yandex-team.ru
 *         Date: 26.02.16
 */
public class WriteModule extends AbstractModule {

    private final List<AbstractPlugin> writePlugins;

    public WriteModule(List<AbstractPlugin> writePlugins) {
        this.writePlugins = writePlugins;
    }

    @Override
    protected void configure() {
        writePlugins.forEach(this::install);
    }
}
