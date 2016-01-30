package org.allurefw.report;

import org.pegdown.Extensions;
import org.pegdown.PegDownProcessor;

/**
 * @author Dmitry Baev charlie@yandex-team.ru
 *         Date: 30.01.16
 */
public final class ReportApiUtils {

    ReportApiUtils() {
    }

    //TODO think about markdown
    // maybe should be moved to generator as internal plugin.
    // In this case description should be typed (type is some string, I guess)
    public static String processMarkdown(String rawText) {
        return new PegDownProcessor(Extensions.ALL + Extensions.SUPPRESS_ALL_HTML)
                .markdownToHtml(rawText);
    }
}
