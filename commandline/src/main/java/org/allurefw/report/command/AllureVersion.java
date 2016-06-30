package org.allurefw.report.command;

import io.airlift.airline.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Artem Eroshenko <eroshenkoam@qameta.io>
 */
@Command(name = "version", description = "Display version")
public class AllureVersion extends AbstractCommand {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReportOpen.class);

    /**
     * {@inheritDoc}
     */
    @Override
    protected void runUnsafe() {
        String toolVersion = getClass().getPackage().getImplementationVersion();
        LOGGER.info(toolVersion);
    }
}
