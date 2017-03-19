package io.qameta.allure.command;

import com.github.rvesse.airline.annotations.Arguments;
import com.github.rvesse.airline.annotations.Command;
import com.github.rvesse.airline.annotations.restrictions.Path;
import com.github.rvesse.airline.annotations.restrictions.PathKind;
import org.eclipse.jetty.server.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.nio.file.Paths;

import static io.qameta.allure.utils.CommandUtils.openBrowser;
import static io.qameta.allure.utils.CommandUtils.setUpServer;

/**
 * @author Artem Eroshenko <eroshenkoam@qameta.io>
 */
@SuppressWarnings("SpringAutowiredFieldsWarningInspection")
@Command(name = "open", description = "Open generated report")
public class ReportOpen implements AllureCommand {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReportOpen.class);

    @Path(mustExist = true, kind = PathKind.DIRECTORY)
    @Arguments(
            title = "Report directory",
            description = "The directory with the report to open")
    protected String reportDirectory = "allure-report";

    @Inject
    private final PortOptions portOptions = new PortOptions();

    @Inject
    private final VerboseOptions verboseOptions = new VerboseOptions();

    @Override
    public void run(final Context context) throws Exception {
        verboseOptions.configureLogLevel();

        LOGGER.info("Starting web server...");
        Server server = setUpServer(portOptions.getPort(), Paths.get(reportDirectory));
        server.start();

        openBrowser(server.getURI());
        LOGGER.info("Server started at <{}>. Press <Ctrl+C> to exit ...", server.getURI());
        server.join();
    }
}