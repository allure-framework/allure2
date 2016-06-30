package org.allurefw.report.issue;

import org.allurefw.report.AbstractPlugin;
import org.allurefw.report.Plugin;

/**
 * @author Dmitry Baev baev@qameta.io
 *         Date: 18.02.16
 */
@Plugin(name = "issue-link")
public class IssueModule extends AbstractPlugin {

    @Override
    protected void configure() {
        processor(IssueLinkProcessor.class);
    }
}
