package org.allurefw.report.defects;

import org.allurefw.report.Defect;
import org.allurefw.report.DefectType;
import org.allurefw.report.DefectsData;
import org.allurefw.report.DefectsWidget;
import org.allurefw.report.DefectsWidgetItem;
import org.allurefw.report.Finalizer;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Dmitry Baev baev@qameta.io
 *         Date: 23.04.16
 */
public class TestDefectsWidgetFinalizer implements Finalizer<DefectsData> {

    @Override
    public Object finalize(DefectsData identity) {
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
