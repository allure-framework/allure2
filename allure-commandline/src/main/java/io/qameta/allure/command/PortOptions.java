package io.qameta.allure.command;

import com.github.rvesse.airline.annotations.Option;
import com.github.rvesse.airline.annotations.restrictions.Port;

/**
 * @author charlie (Dmitry Baev).
 */
public class PortOptions {

    @Port
    @Option(name = {"-p", "--port"},
            description = "This port will be used to start web server for the report")
    protected int port = 0;

    public int getPort() {
        return port;
    }
}
