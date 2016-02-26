package org.allurefw.report.attachments;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;

/**
 * @author Dmitry Baev charlie@yandex-team.ru
 *         Date: 26.02.16
 */
public class AttachmentsModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(AttachmentsStorage.class).in(Scopes.SINGLETON);
    }
}
