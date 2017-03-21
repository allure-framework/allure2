package io.qameta.allure.owner;

import io.qameta.allure.AbstractPlugin;

/**
 * @author charlie (Dmitry Baev).
 */
public class OwnerPlugin extends AbstractPlugin {

    public static final String OWNER = "owner";

    @Override
    protected void configure() {
        processor(OwnerProcessor.class);
    }
}
