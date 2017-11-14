package io.qameta.allure.option;

import com.beust.jcommander.Parameter;
import io.qameta.allure.validator.PortValidator;

/**
 * Contains port options.
 *
 * @since 2.0
 */
@SuppressWarnings("PMD.ImmutableField")
public class HostPortOptions {

    @Parameter(
            names = {"-p", "--port"},
            description = "This port will be used to start web server for the report.",
            validateWith = PortValidator.class
    )
    private int port;

    @Parameter(
            names = {"-h", "--host"},
            description = "This host will be used to start web server for the report."
    )
    private String host;

    public int getPort() {
        return port;
    }

    public String getHost() {
        return host;
    }
}
