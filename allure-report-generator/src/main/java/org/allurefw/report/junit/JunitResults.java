package org.allurefw.report.junit;

import org.allurefw.Label;
import org.allurefw.ModelUtils;
import org.allurefw.Status;
import org.allurefw.report.ReportDataManager;
import org.allurefw.report.Results;
import org.allurefw.report.entity.Attachment;
import org.allurefw.report.entity.Failure;
import org.allurefw.report.entity.TestCase;
import org.allurefw.report.entity.Time;
import ru.yandex.qatools.allure.BadXmlCharacterFilterReader;

import javax.xml.bind.JAXB;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import static org.allurefw.report.ReportApiUtils.generateUid;
import static org.allurefw.report.ReportApiUtils.listFilesSafe;

/**
 * @author Dmitry Baev charlie@yandex-team.ru
 *         Date: 14.02.16
 */
public class JunitResults implements Results {

    private ReportDataManager manager;

    @Override
    public void process(Path resultDirectory) {
        List<Path> results = listFilesSafe("TEST-*.xml", resultDirectory);

        for (Path result : results) {
            Optional<Testsuite> unmarshal = unmarshal(result);
            if (!unmarshal.isPresent()) {
                continue;
            }

            Testsuite testSuite = unmarshal.get();
            Label suiteLabel = ModelUtils.createSuiteLabel(testSuite.getName());
            Path attachmentFile = resultDirectory.resolve(testSuite.getName() + ".txt");
            Optional<Attachment> log = Optional.of(attachmentFile)
                    .filter(Files::exists)
                    .map(manager::addAttachment)
                    .map(source -> new Attachment()
                            .withUid(generateUid())
                            .withName("Test log")
                            .withSource(source)
                            .withType("text/plain")
                    );

            for (Testsuite.Testcase testCaseRow : testSuite.getTestcase()) {
                TestCase testCase = convert(testCaseRow);

                if (log.isPresent()) {
                    testCase.getAttachments().add(log.get());
                }
                testCase.getLabels().add(suiteLabel);
                manager.addTestCase(testCase);
            }
        }
    }

    @Override
    public void setReportDataManager(ReportDataManager manager) {
        this.manager = manager;
    }

    protected TestCase convert(Testsuite.Testcase source) {
        TestCase dest = new TestCase();
        dest.setUid(generateUid());
        dest.setName(source.getName());
        dest.setTime(new Time()
                .withDuration(source.getTime().multiply(new BigDecimal(1000)).longValue())
        );
        dest.setStatus(getStatus(source));
        dest.setFailure(getFailure(source));
        return dest;
    }

    protected Status getStatus(Testsuite.Testcase source) {
        if (source.getFailure() != null) {
            return Status.FAILED;
        }
        if (source.getError() != null) {
            return Status.BROKEN;
        }
        return Status.PASSED;
    }

    protected Failure getFailure(Testsuite.Testcase source) {
        if (source.getFailure() != null) {
            return new Failure().withMessage(source.getFailure().getMessage());
        }
        if (source.getError() != null) {
            return new Failure().withMessage(source.getError().getMessage());
        }
        return null;
    }

    protected Optional<Testsuite> unmarshal(Path path) {
        try (BadXmlCharacterFilterReader reader = new BadXmlCharacterFilterReader(path)) {
            return Optional.of(JAXB.unmarshal(reader, Testsuite.class));
        } catch (IOException e) {
            return Optional.empty();
        }
    }
}
