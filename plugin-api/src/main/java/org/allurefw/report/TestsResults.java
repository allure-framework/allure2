package org.allurefw.report;

import org.allurefw.report.entity.Attachment;
import org.allurefw.report.entity.TestCaseResult;
import org.allurefw.report.entity.TestGroup;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

/**
 * @author charlie (Dmitry Baev).
 */
public interface TestsResults {

    List<TestCaseResult> getTestCases();

    List<TestGroup> getTestGroups();

    Map<Path, Attachment> getAttachments();

}
