package io.qameta.allure;

import io.qameta.allure.entity.Attachment;
import io.qameta.allure.entity.TestCase;
import io.qameta.allure.entity.TestCaseResult;

import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * @author charlie (Dmitry Baev).
 */
public class DefaultLaunchResults implements LaunchResults {

    private final Set<TestCaseResult> results;

    private final Set<TestCase> testCases;

    private final Map<Path, Attachment> attachments;

    private final Map<String, Object> extra;

    public DefaultLaunchResults(Set<TestCaseResult> results,
                                Set<TestCase> testCases,
                                Map<Path, Attachment> attachments,
                                Map<String, Object> extra) {
        this.results = results;
        this.testCases = testCases;
        this.attachments = attachments;
        this.extra = extra;
    }

    @Override
    public Set<TestCaseResult> getResults() {
        return results;
    }

    @Override
    public Set<TestCase> getTestCases() {
        return testCases;
    }

    @Override
    public Map<Path, Attachment> getAttachments() {
        return attachments;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> Optional<T> getExtra(String name) {
        return Optional.ofNullable((T) extra.get(name));
    }
}
