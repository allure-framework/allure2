package io.qameta.allure.owner;

import io.qameta.allure.Configuration;
import io.qameta.allure.LaunchResults;
import io.qameta.allure.Plugin;
import io.qameta.allure.entity.LabelName;
import io.qameta.allure.entity.TestCaseResult;

import java.nio.file.Path;
import java.util.List;

/**
 * @author charlie (Dmitry Baev).
 */
public class OwnerPlugin implements Plugin {

    private static final String OWNER_BLOCK_NAME = "owner";

    @Override
    public void process(final Configuration configuration,
                        final List<LaunchResults> launches,
                        final Path outputDirectory) {
        launches.stream()
                .flatMap(results -> results.getResults().stream())
                .forEach(this::setOwner);
    }

    private void setOwner(final TestCaseResult result) {
        result.findOne(LabelName.OWNER)
                .ifPresent(owner -> result.addExtraBlock(OWNER_BLOCK_NAME, owner));
    }
}
