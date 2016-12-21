package io.qameta.allure.command;

import com.github.rvesse.airline.annotations.Command;

/**
 * @author Artem Eroshenko <eroshenkoam@qameta.io>
 */
@Command(name = "version", description = "Display version")
public class Version implements AllureCommand {

    @Override
    public void run(Context context) throws Exception {
        System.out.println(context.getToolVersion());
    }
}
