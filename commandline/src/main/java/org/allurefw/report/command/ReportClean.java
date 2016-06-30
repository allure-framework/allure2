package org.allurefw.report.command;

import io.airlift.airline.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.allurefw.report.utils.DeleteVisitor;

import java.nio.file.Files;
import java.nio.file.Path;

/**
 * @author Artem Eroshenko <eroshenkoam@qameta.io>
 */
@Command(name = "clean", description = "Clean report")
public class ReportClean extends ReportCommand {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReportClean.class);

    /**
     * Remove the report directory.
     */
    @Override
    protected void runUnsafe() throws Exception {
        Path reportDirectory = getReportDirectoryPath();
        Files.walkFileTree(reportDirectory, new DeleteVisitor());
        LOGGER.info("Report directory <{}> was successfully cleaned.", reportDirectory);
    }
}
