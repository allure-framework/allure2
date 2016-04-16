package org.allurefw.report.defects;

import org.allurefw.report.Aggregator;
import org.allurefw.report.Defect;
import org.allurefw.report.DefectsData;
import org.allurefw.report.entity.Failure;
import org.allurefw.report.entity.Status;
import org.allurefw.report.entity.TestCase;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Supplier;

import static org.allurefw.report.ReportApiUtils.generateUid;
import static org.allurefw.report.entity.Status.BROKEN;
import static org.allurefw.report.entity.Status.FAILED;

/**
 * @author Dmitry Baev charlie@yandex-team.ru
 *         Date: 16.04.16
 */
public class DefectsAggregator implements Aggregator<DefectsData> {

    @Override
    public Supplier<DefectsData> supplier() {
        return DefectsData::new;
    }

    @Override
    public BinaryOperator<DefectsData> combiner() {
        return (left, right) -> {
            left.getProductDefects().addAll(right.getProductDefects());
            left.getTestDefects().addAll(right.getTestDefects());
            return left;
        };
    }

    @Override
    public BiConsumer<DefectsData, TestCase> accumulator() {
        return (identity, testCase) -> {
            Status status = testCase.getStatus();
            if (!FAILED.equals(status) && !BROKEN.equals(status)) {
                return;
            }

            List<Defect> defects = status == FAILED ? identity.getProductDefects() : identity.getTestDefects();
            String defectMessage = testCase.getFailureIfExists()
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

            defect.getTestCases().add(testCase.toInfo());
        };
    }
}
