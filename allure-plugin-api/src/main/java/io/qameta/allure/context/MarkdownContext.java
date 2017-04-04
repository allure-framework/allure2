package io.qameta.allure.context;

import io.qameta.allure.Context;
import org.pegdown.Extensions;
import org.pegdown.PegDownProcessor;

/**
 * @author charlie (Dmitry Baev).
 */
public class MarkdownContext implements Context<PegDownProcessor> {

    private final PegDownProcessor processor = new PegDownProcessor(Extensions.ALL);

    @Override
    public PegDownProcessor getValue() {
        return processor;
    }
}
