package io.qameta.allure.owner;

import io.qameta.allure.Processor;
import io.qameta.allure.entity.*;

import java.util.Optional;

/**
 * Created by bvo2002 on 19.02.17.
 */
public class OwnerProcessor implements Processor {
    @Override
    public void process(TestRun testRun, TestCase testCase, TestCaseResult result) {
        Optional<String> owner = result.findOne(LabelName.OWNER);
        owner.ifPresent(value -> result.addExtraBlock("owner", value));
    }
}
