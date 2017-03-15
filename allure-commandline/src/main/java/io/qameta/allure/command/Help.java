package io.qameta.allure.command;

import com.github.rvesse.airline.annotations.Arguments;
import com.github.rvesse.airline.annotations.Command;
import com.github.rvesse.airline.annotations.Option;
import com.github.rvesse.airline.model.GlobalMetadata;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

import static com.github.rvesse.airline.help.Help.help;

/**
 * @author charlie (Dmitry Baev).
 */
@Command(name = "help", description = "Display help information")
public class Help<T> implements AllureCommand {

    @Inject
    public GlobalMetadata<T> global;

    @Arguments
    public List<String> command = new ArrayList<>();

    @Option(
            name = {"--include-hidden"},
            description = "When set the help output will include hidden commands and options",
            hidden = true)
    public boolean includeHidden;

    @Override
    public void run(final Context context) throws Exception {
        help(global, command, this.includeHidden);
    }
}
