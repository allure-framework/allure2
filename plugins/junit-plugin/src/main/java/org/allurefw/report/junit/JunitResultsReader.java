package org.allurefw.report.junit;

import com.github.baev.BadXmlCharactersFilterReader;
import com.github.baev.junit.Testsuite;
import com.google.inject.Inject;
import org.allurefw.report.AttachmentsStorage;
import org.allurefw.report.ResultsReader;
import org.allurefw.report.entity.Attachment;
import org.allurefw.report.entity.Failure;
import org.allurefw.report.entity.Label;
import org.allurefw.report.entity.LabelName;
import org.allurefw.report.entity.Status;
import org.allurefw.report.entity.TestCaseResult;
import org.allurefw.report.entity.Time;

import javax.xml.bind.JAXB;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.allurefw.report.ModelUtils.createLabel;
import static org.allurefw.report.ReportApiUtils.generateUid;
import static org.allurefw.report.ReportApiUtils.listFiles;

/**
 * @author Dmitry Baev baev@qameta.io
 *         Date: 14.02.16
 */
public class JunitResultsReader implements ResultsReader {

    private final AttachmentsStorage storage;

    @Inject
    public JunitResultsReader(AttachmentsStorage storage) {
        this.storage = storage;
    }

    @Override
    public List<TestCaseResult> readResults(Path source) {
        return listFiles(source, "TEST-*.xml")
                .map(this::unmarshal)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .flatMap(testSuite -> {
                    Label suiteLabel = createLabel(LabelName.SUITE, testSuite.getName());
                    Path attachmentFile = source.resolve(testSuite.getName() + ".txt");
                    Optional<Attachment> log = Optional.of(attachmentFile)
                            .filter(Files::exists)
                            .map(storage::addAttachment)
                            .map(attachment -> attachment.withName("System out"));

                    List<TestCaseResult> results = new ArrayList<>();
                    for (Testsuite.Testcase testCaseRow : testSuite.getTestcase()) {
                        TestCaseResult testCase = convert(testCaseRow);
                        log.ifPresent(testCase.getAttachments()::add);
                        testCase.getLabels().add(suiteLabel);
                        results.add(testCase);
                    }
                    return results.stream();
                }).collect(Collectors.toList());
    }

    protected TestCaseResult convert(Testsuite.Testcase source) {
        TestCaseResult dest = new TestCaseResult();
        dest.setId(String.format("%s#%s", source.getClassname(), source.getName()));
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
        try (BadXmlCharactersFilterReader reader = new BadXmlCharactersFilterReader(path)) {
            return Optional.of(JAXB.unmarshal(reader, Testsuite.class));
        } catch (IOException e) {
            return Optional.empty();
        }
    }
}
