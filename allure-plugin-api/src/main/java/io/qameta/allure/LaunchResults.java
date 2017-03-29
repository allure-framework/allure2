package io.qameta.allure;

import io.qameta.allure.entity.Attachment;
import io.qameta.allure.entity.TestCaseResult;

import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;

/**
 * @author charlie (Dmitry Baev).
 */
public interface LaunchResults {

    Set<TestCaseResult> getResults();

    Map<Path, Attachment> getAttachments();

    <T> Optional<T> getExtra(String name);

    default <T> T getExtra(String name, Supplier<T> defaultValue) {
        final Optional<T> extra = getExtra(name);
        return extra.orElseGet(defaultValue);
    }

}
