package io.qameta.allure;

import io.qameta.allure.core.LaunchResults;
import io.qameta.allure.entity.Attachment;
import io.qameta.allure.entity.TestCaseResult;

import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Default implementation of {@link LaunchResults}. Stores all the results
 * into memory.
 *
 * @since 2.0
 */
public class DefaultLaunchResults implements LaunchResults {

    private final Set<TestCaseResult> results;

    private final Map<Path, Attachment> attachments;

    private final Map<String, Object> extra;

    public DefaultLaunchResults(final Set<TestCaseResult> results,
                                final Map<Path, Attachment> attachments,
                                final Map<String, Object> extra) {
        this.results = results;
        this.attachments = attachments;
        this.extra = extra;
    }

    @Override
    public Set<TestCaseResult> getAllResults() {
        return results;
    }

    @Override
    public Map<Path, Attachment> getAttachments() {
        return attachments;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> Optional<T> getExtra(final String name) {
        return Optional.ofNullable((T) extra.get(name));
    }
}
