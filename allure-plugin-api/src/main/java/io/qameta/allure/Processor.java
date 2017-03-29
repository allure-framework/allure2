package io.qameta.allure;

import java.util.List;

/**
 * @author charlie (Dmitry Baev).
 */
public interface Processor {

    void process(ReportConfiguration configuration, List<LaunchResults> launches);

}
