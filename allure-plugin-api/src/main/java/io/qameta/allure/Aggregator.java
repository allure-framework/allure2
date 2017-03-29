package io.qameta.allure;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

/**
 * @author charlie (Dmitry Baev).
 */
public interface Aggregator {

    void aggregate(ReportConfiguration configuration,
                   List<LaunchResults> launches,
                   Path outputDirectory) throws IOException;

}
