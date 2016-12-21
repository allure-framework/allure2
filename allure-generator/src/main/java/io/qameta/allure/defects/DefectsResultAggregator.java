package io.qameta.allure.defects;

import io.qameta.allure.ResultAggregator;
import io.qameta.allure.entity.Failure;
import io.qameta.allure.entity.Status;
import io.qameta.allure.entity.TestCase;
import io.qameta.allure.entity.TestCaseResult;
import io.qameta.allure.entity.TestRun;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static io.qameta.allure.ReportApiUtils.generateUid;
import static io.qameta.allure.entity.Status.BROKEN;
import static io.qameta.allure.entity.Status.FAILED;

/**
 * @author Dmitry Baev baev@qameta.io
 *         Date: 16.04.16
 */
public class DefectsResultAggregator implements ResultAggregator<DefectsData> {

    @Override
    public Supplier<DefectsData> supplier(TestRun testRun, TestCase testCase) {
        return DefectsData::new;
    }

    @Override
    public Consumer<DefectsData> aggregate(TestRun testRun, TestCase testCase, TestCaseResult result) {
        return (identity) -> {
            Status status = result.getStatus();
            if (!FAILED.equals(status) && !BROKEN.equals(status)) {
                return;
            }

            List<Defect> defects = status == FAILED ? identity.getProductDefects() : identity.getTestDefects();
            String defectMessage = result.getFailureIfExists()
                    .map(Failure::getMessage)
                    .orElse("Unknown error");

            Defect defect = defects.stream()
                    .filter(item -> defectMessage.equals(item.getMessage()))
                    .findAny()
                    .orElseGet(() -> {
                        Defect newOne = new Defect().withMessage(defectMessage).withUid(generateUid());
                        defects.add(newOne);
                        return newOne;
                    });

            defect.getTestCases().add(result.toInfo());
        };
    }
}
