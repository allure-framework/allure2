package io.qameta.allure.defects;

import io.qameta.allure.Finalizer;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Dmitry Baev baev@qameta.io
 *         Date: 23.04.16
 */
public class TestDefectsWidgetFinalizer implements Finalizer<DefectsData> {

    @Override
    public Object convert(DefectsData identity) {
        List<DefectsWidgetItem> items = identity.getTestDefects().stream()
                .sorted(Comparator
                        .<Defect>comparingInt(value -> value.getTestCases().size())
                        .reversed()
                        .thenComparing(Defect::getMessage, Comparator.naturalOrder()))
                .limit(10)
                .map(defect -> new DefectsWidgetItem()
                        .withUid(defect.getUid())
                        .withMessage(defect.getMessage())
                        .withCount(defect.getTestCases().size()))
                .collect(Collectors.toList());
        return new DefectsWidget()
                .withTotalCount(identity.getTestDefects().size())
                .withItems(items);
    }
}
