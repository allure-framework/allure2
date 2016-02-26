package org.allurefw.report.groups;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;

/**
 * @author Dmitry Baev charlie@yandex-team.ru
 *         Date: 26.02.16
 */
public class GroupsModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(GroupsStorage.class).in(Scopes.SINGLETON);

    }
}
