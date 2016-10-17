package org.allurefw.report;

import org.allurefw.report.entity.TestRun;

import java.nio.file.Path;

/**
 * @author charlie (Dmitry Baev).
 */
public interface TestRunReader {

    TestRun readTestRun(Path source);

}
