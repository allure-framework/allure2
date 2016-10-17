package org.allurefw.report.defects;

import org.allurefw.report.Aggregator;
import org.allurefw.report.entity.Failure;
import org.allurefw.report.entity.Status;
import org.allurefw.report.entity.TestCase;
import org.allurefw.report.entity.TestCaseResult;
import org.allurefw.report.entity.TestRun;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static org.allurefw.report.ReportApiUtils.generateUid;
import static org.allurefw.report.entity.Status.BROKEN;
import static org.allurefw.report.entity.Status.FAILED;

/**
 * @author Dmitry Baev baev@qameta.io
 *         Date: 16.04.16
 */
public class DefectsAggregator implements Aggregator<DefectsData> {

    @Override
    public Supplier<DefectsData> supplier() {
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
