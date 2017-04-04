package io.qameta.allure.option;

import com.beust.jcommander.Parameter;
import io.qameta.allure.validator.PortValidator;

/**
 * Contains port options.
 *
 * @since 2.0
 */
@SuppressWarnings("PMD.ImmutableField")
public class PortOptions {

    @Parameter(
            names = {"-p", "--port"},
            description = "This port will be used to start web server for the report.",
            validateWith = PortValidator.class
    )
    private int port;

    public int getPort() {
        return port;
    }
}
