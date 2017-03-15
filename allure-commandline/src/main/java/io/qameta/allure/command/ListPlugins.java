package io.qameta.allure.command;

import com.github.rvesse.airline.annotations.Command;
import io.qameta.allure.Plugin;

import javax.inject.Inject;

import static io.qameta.allure.utils.CommandUtils.createMain;

/**
 * @author charlie (Dmitry Baev).
 */
@SuppressWarnings("SpringAutowiredFieldsWarningInspection")
@Command(name = "plugins", description = "Show installed plugins")
public class ListPlugins implements AllureCommand {

    @Inject
    private final VerboseOptions verboseOptions = new VerboseOptions();

    @Override
    public void run(final Context context) throws Exception {
        verboseOptions.configureLogLevel();
        createMain(context)
                .loadPlugins()
                .forEach(this::printPluginInfo);
    }

    @SuppressWarnings("PMD.SystemPrintln")
    protected void printPluginInfo(final Plugin plugin) {
        System.out.println(String.format(
                "<%s> enabled: %s",
                plugin.getDescriptor().getName(),
                plugin.isEnabled()
        ));
    }
}
