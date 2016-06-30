package org.allurefw.report;

import org.allurefw.report.entity.Attachment;
import org.allurefw.report.entity.TestCaseResult;
import org.allurefw.report.entity.TestGroup;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author charlie (Dmitry Baev).
 */
public class InMemoryTestsResults implements TestsResults {

    private List<TestCaseResult> testCases = new ArrayList<>();

    private List<TestGroup> testGroups = new ArrayList<>();

    private Map<Path, Attachment> attachments = new HashMap<>();

    public InMemoryTestsResults() {
    }

    public InMemoryTestsResults addTestCaseResult(TestCaseResult result) {
        testCases.add(result);
        return this;
    }

    public InMemoryTestsResults addTestGroup(TestGroup testGroup) {
        testGroups.add(testGroup);
        return this;
    }

    public InMemoryTestsResults addAttachment(Path attachmentPath, Attachment attachment) {
        attachments.put(attachmentPath, attachment);
        return this;
    }

    @Override
    public List<TestCaseResult> getTestCases() {
        return Collections.unmodifiableList(testCases);
    }

    @Override
    public List<TestGroup> getTestGroups() {
        return Collections.unmodifiableList(testGroups);
    }

    @Override
    public Map<Path, Attachment> getAttachments() {
        return Collections.unmodifiableMap(attachments);
    }

}
