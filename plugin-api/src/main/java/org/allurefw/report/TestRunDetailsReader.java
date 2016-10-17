package org.allurefw.report;

import org.allurefw.report.entity.TestRun;

import java.nio.file.Path;
import java.util.function.Consumer;

/**
 * @author charlie (Dmitry Baev).
 */
public interface TestRunDetailsReader {

    Consumer<TestRun> readDetails(Path source);

}
