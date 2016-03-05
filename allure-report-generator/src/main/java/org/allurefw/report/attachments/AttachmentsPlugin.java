package org.allurefw.report.attachments;

import com.google.inject.Scopes;
import org.allurefw.report.AbstractPlugin;
import org.allurefw.report.Plugin;
import org.allurefw.report.PluginScope;

/**
 * @author Dmitry Baev charlie@yandex-team.ru
 *         Date: 26.02.16
 */
@Plugin(name = "attachments-support", scope = PluginScope.CORE)
public class AttachmentsPlugin extends AbstractPlugin {

    @Override
    protected void configure() {
        bind(AttachmentsStorage.class).in(Scopes.SINGLETON);
    }
}
