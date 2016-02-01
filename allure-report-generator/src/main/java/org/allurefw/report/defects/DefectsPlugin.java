package org.allurefw.report.defects;

import com.google.inject.Inject;
import org.allurefw.Status;
import org.allurefw.report.Defect;
import org.allurefw.report.DefectsData;
import org.allurefw.report.TestCaseProcessor;
import org.allurefw.report.entity.Failure;
import org.allurefw.report.entity.TestCase;
import sun.plugin.dom.exception.InvalidStateException;

import java.util.List;

import static org.allurefw.Status.BROKEN;
import static org.allurefw.Status.FAILED;
import static org.allurefw.report.ReportApiUtils.generateUid;

/**
 * @author Dmitry Baev charlie@yandex-team.ru
 *         Date: 01.02.16
 */
public class DefectsPlugin implements TestCaseProcessor {

    @Inject
    protected DefectsData data;

    @Override
    public void process(TestCase testCase) {
        Status status = testCase.getStatus();
        if (!FAILED.equals(status) && !BROKEN.equals(status)) {
            return;
        }

        List<Defect> defects = getDefects(status);
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
    }

    protected List<Defect> getDefects(Status status) {
        switch (status) {
            case FAILED:
                return data.getProductDefects();
            case BROKEN:
                return data.getTestDefects();
            default:
                throw new InvalidStateException("Defects are supported only " +
                        "for failed and broken tests");
        }
    }
}
