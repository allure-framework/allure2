package io.qameta.allure.owner;

import io.qameta.allure.Processor;
import io.qameta.allure.entity.LabelName;
import io.qameta.allure.entity.TestCase;
import io.qameta.allure.entity.TestCaseResult;
import io.qameta.allure.entity.TestRun;

import java.util.Optional;

/**
 * @author charlie (Dmitry Baev).
 */
public class OwnerProcessor implements Processor {

    @Override
    public void process(final TestRun testRun, final TestCase testCase, final TestCaseResult result) {
        Optional<String> owner = result.findOne(LabelName.OWNER);
        owner.ifPresent(value -> result.addExtraBlock(OwnerPlugin.OWNER, value));
    }
}
