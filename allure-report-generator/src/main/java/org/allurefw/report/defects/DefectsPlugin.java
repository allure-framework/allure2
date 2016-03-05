package org.allurefw.report.defects;

import org.allurefw.Status;
import org.allurefw.report.AbstractPlugin;
import org.allurefw.report.Defect;
import org.allurefw.report.DefectsData;
import org.allurefw.report.Plugin;
import org.allurefw.report.PluginScope;
import org.allurefw.report.entity.Failure;
import org.allurefw.report.entity.TestCase;

import java.util.List;

import static org.allurefw.Status.BROKEN;
import static org.allurefw.Status.FAILED;
import static org.allurefw.report.ReportApiUtils.generateUid;

/**
 * @author Dmitry Baev charlie@yandex-team.ru
 *         Date: 01.02.16
 */
@Plugin(name = "defects", scope = PluginScope.PROCESS)
public class DefectsPlugin extends AbstractPlugin {

    @Override
    protected void configure() {
        aggregator(new DefectsData(), () -> this::aggregate);
    }

    protected void aggregate(DefectsData identity, TestCase testCase) {
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
    }
}
