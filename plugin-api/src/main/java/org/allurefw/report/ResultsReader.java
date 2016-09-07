package org.allurefw.report;

import java.util.List;

/**
 * @author charlie (Dmitry Baev).
 */
public interface ResultsReader {

    List<Result> readResults(ResultsSource source);

}
