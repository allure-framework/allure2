package io.qameta.allure.owner;

import io.qameta.allure.LaunchResults;
import io.qameta.allure.Processor;
import io.qameta.allure.ReportConfiguration;
import io.qameta.allure.entity.LabelName;
import io.qameta.allure.entity.TestCaseResult;

import java.util.List;

/**
 * @author charlie (Dmitry Baev).
 */
public class OwnerProcessor implements Processor {

    @Override
    public void process(ReportConfiguration configuration, List<LaunchResults> launches) {
        launches.stream()
                .flatMap(results -> results.getResults().stream())
                .forEach(this::setOwner);
    }

    private void setOwner(TestCaseResult result) {
        result.findOne(LabelName.OWNER)
                .ifPresent(owner -> result.addExtraBlock("owner", owner));
    }
}
