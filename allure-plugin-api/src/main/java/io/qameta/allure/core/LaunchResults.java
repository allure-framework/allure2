package io.qameta.allure.core;

import io.qameta.allure.entity.Attachment;
import io.qameta.allure.entity.TestCaseResult;

import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * @author charlie (Dmitry Baev).
 */
public interface LaunchResults {

    /**
     * Returns not hidden test results.
     *
     * @return the results that are not hidden.
     */
    default Set<TestCaseResult> getResults() {
        return getAllResults().stream()
                .filter(result -> !result.isHidden())
                .collect(Collectors.toSet());
    }

    Set<TestCaseResult> getAllResults();

    Map<Path, Attachment> getAttachments();

    <T> Optional<T> getExtra(String name);

    default <T> T getExtra(String name, Supplier<T> defaultValue) {
        final Optional<T> extra = getExtra(name);
        return extra.orElseGet(defaultValue);
    }

}
