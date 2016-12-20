package org.allurefw.report.markdown;

import org.pegdown.Extensions;
import org.pegdown.PegDownProcessor;

/**
 * @author charlie (Dmitry Baev).
 */
public class MarkdownSupport {

    private final PegDownProcessor processor = new PegDownProcessor(Extensions.ALL);

    public PegDownProcessor getProcessor() {
        return processor;
    }
}
