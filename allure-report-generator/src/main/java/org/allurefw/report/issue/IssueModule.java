package org.allurefw.report.issue;

import org.allurefw.report.AbstractPlugin;

/**
 * @author Dmitry Baev charlie@yandex-team.ru
 *         Date: 18.02.16
 */
public class IssueModule extends AbstractPlugin {

    @Override
    protected void configure() {
        processor(IssueLinkProcessor.class);
    }
}
