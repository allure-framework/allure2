package org.allurefw.report.junit;

import com.google.inject.Inject;
import org.allurefw.ModelUtils;
import org.allurefw.Status;
import org.allurefw.report.ResultDirectories;
import org.allurefw.report.entity.Failure;
import org.allurefw.report.entity.TestCase;
import org.allurefw.report.entity.TestSuiteInfo;
import org.allurefw.report.entity.Time;
import ru.yandex.qatools.allure.BadXmlCharacterFilterReader;

import javax.xml.bind.JAXB;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.allurefw.report.ReportApiUtils.generateUid;
import static org.allurefw.report.ReportApiUtils.listFilesSafe;

/**
 * @author Dmitry Baev charlie@yandex-team.ru
 *         Date: 11.02.16
 */
public class JunitResults {

    private final Path[] resultDirectories;

    private final List<Path> files;

    private final List<Testsuite> testSuites;

    @Inject
    public JunitResults(@ResultDirectories Path[] resultDirectories) {
        this.resultDirectories = resultDirectories;
        this.files = listFilesSafe("TEST-*.xml", this.resultDirectories);
        this.testSuites = files.stream()
                .map(this::unmarshal)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    //TODO filename for file in data? Should be the same as in TestCase in attachments
    public List<Path> getAttachments() {
        return testSuites.stream()
                .map(Testsuite::getName)
                .map(name -> name + ".txt")
                .flatMap(name -> listFilesSafe(name, resultDirectories).stream())
                .collect(Collectors.toList());
    }

    public List<TestCase> getTestCases() {
        Function<Testsuite, Stream<TestCase>> mapper = testSuite ->
                testSuite.getTestcase().stream().map(testCase -> convert(testCase, testSuite));

        return testSuites.stream()
                .flatMap(mapper)
                .collect(Collectors.toList());
    }

    public List<TestSuiteInfo> getTestSuites() {
        return testSuites.stream()
                .map(suite -> new TestSuiteInfo()
                        .withName(suite.getName())
                        .withUid(generateUid()))
                .collect(Collectors.toList());
    }

    protected TestCase convert(Testsuite.Testcase source, Testsuite group) {
        TestCase dest = new TestCase();
        dest.setUid(generateUid());
        dest.setName(source.getName());
        dest.setTime(new Time()
                .withDuration(source.getTime().multiply(new BigDecimal(1000)).longValue())
        );
        dest.setStatus(getStatus(source));
        dest.setFailure(getFailure(source));
        dest.getLabels().add(ModelUtils.createSuiteLabel(group.getName()));
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
