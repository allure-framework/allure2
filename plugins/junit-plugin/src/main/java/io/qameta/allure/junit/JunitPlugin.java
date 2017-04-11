package io.qameta.allure.junit;

import io.qameta.allure.Reader;
import io.qameta.allure.context.RandomUidContext;
import io.qameta.allure.core.Configuration;
import io.qameta.allure.core.ResultsVisitor;
import io.qameta.allure.entity.Attachment;
import io.qameta.allure.entity.LabelName;
import io.qameta.allure.entity.StageResult;
import io.qameta.allure.entity.Status;
import io.qameta.allure.entity.StatusDetails;
import io.qameta.allure.entity.TestCaseResult;
import io.qameta.allure.entity.Time;
import org.apache.maven.plugins.surefire.report.ReportTestCase;
import org.apache.maven.plugins.surefire.report.ReportTestSuite;
import org.apache.maven.plugins.surefire.report.TestSuiteXmlParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.Files.newDirectoryStream;

/**
 * Plugin that reads data in JUnit.xml format.
 *
 * @since 2.0
 */
public class JunitPlugin implements Reader {

    private static final Logger LOGGER = LoggerFactory.getLogger(JunitPlugin.class);

    public static final BigDecimal MULTIPLICAND = new BigDecimal(1000);

    private final TestSuiteXmlParser parser = new TestSuiteXmlParser();

    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    @Override
    public void readResults(final Configuration configuration, final ResultsVisitor visitor, final Path directory) {
        final RandomUidContext context = configuration.requireContext(RandomUidContext.class);
        listResults(directory).stream()
                .flatMap(this::parse)
                .forEach(testSuite -> {
                    final Path attachmentFile = directory.resolve(testSuite.getFullClassName() + ".txt");
                    final Optional<Attachment> log = Optional.of(attachmentFile)
                            .filter(Files::exists)
                            .map(visitor::visitAttachmentFile)
                            .map(attachment -> attachment.withName("System out"));

                    for (ReportTestCase testCase : testSuite.getTestCases()) {
                        final TestCaseResult result = convert(context, testCase);
                        log.ifPresent(attachment -> {
                            result.setTestStage(new StageResult());
                            result.getTestStage().getAttachments().add(attachment);
                        });
                        result.addLabelIfNotExists(LabelName.SUITE, testSuite.getFullClassName());
                        result.addLabelIfNotExists(LabelName.TEST_CLASS, testSuite.getFullClassName());
                        result.addLabelIfNotExists(LabelName.PACKAGE, testSuite.getFullClassName());
                        visitor.visitTestResult(result);
                    }
                });
    }

    protected TestCaseResult convert(final RandomUidContext context, final ReportTestCase source) {
        final TestCaseResult dest = new TestCaseResult();
        dest.setTestCaseId(String.format("%s#%s", source.getFullClassName(), source.getName()));
        dest.setUid(context.getValue().get());
        dest.setName(source.getName());
        dest.setTime(new Time()
                .withDuration(BigDecimal.valueOf(source.getTime()).multiply(MULTIPLICAND).longValue())
        );
        dest.setStatus(getStatus(source));
        dest.setStatusDetails(getStatusDetails(source));
        return dest;
    }

    protected Status getStatus(final ReportTestCase source) {
        if (!source.hasFailure()) {
            return Status.PASSED;
        }
        if ("java.lang.AssertionError".equalsIgnoreCase(source.getFailureType())) {
            return Status.FAILED;
        }
        if ("skipped".equalsIgnoreCase(source.getFailureType())) {
            return Status.SKIPPED;
        }
        return Status.BROKEN;
    }

    protected StatusDetails getStatusDetails(final ReportTestCase source) {
        if (source.hasFailure()) {
            return new StatusDetails()
                    .withMessage(source.getFailureMessage())
                    .withTrace(source.getFailureDetail());
        }
        return null;
    }

    protected Stream<ReportTestSuite> parse(final Path source) {
        try (InputStreamReader reader = new InputStreamReader(Files.newInputStream(source), UTF_8)) {
            return parser.parse(reader).stream();
        } catch (Exception e) {
            LOGGER.debug("Could not parse result {}: {}", source, e);
            return Stream.empty();
        }
    }

    private static List<Path> listResults(final Path directory) {
        List<Path> result = new ArrayList<>();
        if (!Files.isDirectory(directory)) {
            return result;
        }

        try (DirectoryStream<Path> directoryStream = newDirectoryStream(directory, "TEST-*.xml")) {
            for (Path path : directoryStream) {
                if (!Files.isDirectory(path)) {
                    result.add(path);
                }
            }
        } catch (IOException e) {
            LOGGER.error("Could not read data from {}: {}", directory, e);
        }
        return result;
    }
}
