package org.allurefw.report.defects;

import org.allurefw.report.Defect;
import org.allurefw.report.DefectsData;
import org.allurefw.report.DefectsWidgetData;
import org.allurefw.report.Finalizer;
import org.allurefw.report.entity.Status;

import java.util.Comparator;
import java.util.stream.Collectors;

/**
 * @author Dmitry Baev charlie@yandex-team.ru
 *         Date: 23.04.16
 */
public class TestDefectsWidgetFinalizer implements Finalizer<DefectsData> {

    @Override
    public Object finalize(DefectsData identity) {
        return identity.getTestDefects().stream()
                .sorted(Comparator
                        .<Defect>comparingInt(value -> value.getTestCases().size())
                        .reversed()
                        .thenComparing(Defect::getMessage, Comparator.naturalOrder()))
                .limit(10)
                .map(defect -> new DefectsWidgetData()
                        .withUid(defect.getUid())
                        .withMessage(defect.getMessage())
                        .withStatus(Status.BROKEN)
                        .withCount(defect.getTestCases().size()))
                .collect(Collectors.toList());
    }
}
