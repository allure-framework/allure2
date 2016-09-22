package org.allurefw.report.command;

import com.github.rvesse.airline.annotations.Command;
import org.allurefw.report.Plugin;

import javax.inject.Inject;

import static org.allurefw.report.utils.CommandUtils.createMain;

/**
 * @author charlie (Dmitry Baev).
 */
@Command(name = "plugins", description = "Show installed plugins")
public class ListPlugins implements AllureCommand {

    @Inject
    private VerboseOptions verboseOptions = new VerboseOptions();

    @Override
    public void run(Context context) throws Exception {
        verboseOptions.configureLogLevel();
        createMain(context.getPluginsDirectory(), context.getWorkDirectory())
                .loadPlugins()
                .forEach(this::printPluginInfo);
    }

    protected void printPluginInfo(Plugin plugin) {
        System.out.println(plugin.getDescriptor().getName());
    }
}
