package io.qameta.allure.owner;

import io.qameta.allure.AbstractPlugin;

/**
 * Created by bvo2002 on 19.02.17.
 */
public class OwnerPlugin extends AbstractPlugin {

    @Override
    protected void configure() {
        processor(OwnerProcessor.class);
    }
}
