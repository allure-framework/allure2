package io.qameta.allure;

import io.qameta.allure.config.ReportConfig;
import io.qameta.allure.entity.AttachmentLink;
import io.qameta.allure.entity.CustomField;
import io.qameta.allure.entity.EnvironmentVariable;
import io.qameta.allure.entity.TestParameter;
import io.qameta.allure.entity.TestResult;
import io.qameta.allure.entity.TestResultExecution;
import io.qameta.allure.entity.TestResultStep;
import io.qameta.allure.entity.TestResultType;
import io.qameta.allure.entity.TestTag;
import io.qameta.allure.event.TestResultCreated;
import io.qameta.allure.exception.NotFoundException;
import io.qameta.allure.listener.TestResultListener;
import io.qameta.allure.service.TestResultService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.qameta.allure.entity.EntityComparators.comparingEnvironmentByKeyAndValue;
import static io.qameta.allure.entity.EntityComparators.comparingParametersByNameAndValue;
import static io.qameta.allure.entity.EntityComparators.comparingTestResultsByStartAsc;
import static io.qameta.allure.util.Hashing.md5;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

/**
 * @author charlie (Dmitry Baev).
 */
@SuppressWarnings("PMD.ExcessiveImports")
public class DefaultTestResultService implements TestResultService {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultTestResultService.class);

    private final AtomicLong testResultId = new AtomicLong();

    private final Map<Long, TestResult> results = new ConcurrentHashMap<>();

    private final Map<Long, TestResultExecution> executions = new ConcurrentHashMap<>();

    private final Map<Long, List<AttachmentLink>> attachments = new ConcurrentHashMap<>();

    private final ReportConfig config;

    private final TestResultListener notifier;

    public DefaultTestResultService(final ReportConfig config,
                                    final TestResultListener notifier) {
        this.config = config;
        this.notifier = notifier;
    }

    @Override
    public TestResult create(final TestResult testResult) {
        synchronized (this) {
            testResult.setId(testResultId.incrementAndGet());
            if (isNull(testResult.getType())) {
                testResult.setType(TestResultType.TEST);
            }
            if (isNull(testResult.getTestId())) {
                //TODO dynamic tests?
                testResult.setTestId(UUID.randomUUID().toString());
            }

            testResult.getTags().addAll(getTestTags(testResult));
            testResult.getCustomFields().addAll(getCustomFields(testResult));
            testResult.getEnvironmentVariables().addAll(getEnvironmentVariables(testResult));

            if (isNull(testResult.getHistoryKey())) {
                calculateHistoryKey(testResult).ifPresent(testResult::setHistoryKey);
            }

            processRetries(testResult);
            results.put(testResult.getId(), testResult);
            notifier.onTestResultCreated(new TestResultCreated(testResult));
            return testResult;
        }
    }

    @Override
    public void addExecution(final Long testResultId, final TestResultExecution execution) {
        executions.put(testResultId, execution);
    }

    @Override
    public Optional<TestResultExecution> findExecution(final Long id) {
        return Optional.ofNullable(executions.get(id));
    }

    @Override
    public Optional<TestResult> findOneById(final Long id) {
        return Optional.ofNullable(results.get(id));
    }

    @Override
    public List<TestResult> findAll(final boolean includeHidden) {
        final Predicate<TestResult> predicate = includeHidden
                ? (result) -> true
                : TestResult::isNotHidden;

        return results.values().stream()
                .filter(predicate)
                .collect(Collectors.toList());
    }

    @Override
    public List<TestResult> findAllTests(final boolean includeHidden) {
        final Predicate<TestResult> predicate = includeHidden
                ? (result) -> true
                : TestResult::isNotHidden;

        return results.values().stream()
                .filter(predicate)
                .filter(TestResult::isTest)
                .collect(Collectors.toList());

    }

    @Override
    public List<AttachmentLink> findAttachments(final Long id) {
        final TestResultExecution execution = findExecution(id).orElseGet(TestResultExecution::new);
        final Stream<AttachmentLink> stepAttachments = execution.getSteps().stream()
                .map(this::extractAttachments)
                .flatMap(Collection::stream);

        return Stream.concat(stepAttachments, execution.getAttachments().stream())
                .collect(Collectors.toList());
    }

    @Override
    public List<TestResult> findHistory(final Long id) {
        final Optional<TestResult> found = findOneById(id);
        if (!found.isPresent()) {
            return Collections.emptyList();
        }

        final TestResult testResult = found.get();
        if (isNull(testResult.getHistoryKey())) {
            return Collections.emptyList();
        }
        return Collections.emptyList();
    }

    @Override
    public List<TestResult> findRetries(final Long id) {
        final TestResult result = Optional.ofNullable(results.get(id))
                .orElseThrow(() -> new NotFoundException("Result with id %d not found", id));

        return results.values().stream()
                .filter(item -> Objects.equals(item.getHistoryKey(), result.getHistoryKey()))
                .filter(item -> !Objects.equals(item.getId(), result.getId()))
                .sorted(comparingTestResultsByStartAsc().reversed())
                .collect(Collectors.toList());
    }

    @Override
    public List<TestResult> findAllParents(final Long id) {
        return Collections.emptyList();
    }

    private void processRetries(final TestResult testResult) {
        final Optional<TestResult> found = results.values().stream()
                .filter(candidate -> !candidate.isHidden())
                .filter(candidate -> nonNull(candidate.getHistoryKey()))
                .filter(candidate -> Objects.equals(testResult.getHistoryKey(), candidate.getHistoryKey()))
                .findAny();


        if (found.isPresent()) {
            final TestResult retryCandidate = found.get();
            if (isNull(testResult.getStart()) || isNull(retryCandidate.getStart())
                    || retryCandidate.getStart() <= testResult.getStart()) {
                retryCandidate.setHidden(true);
                retryCandidate.setRetry(true);
            } else {
                testResult.setHidden(true);
                testResult.setRetry(true);
            }
        }
    }

    private List<EnvironmentVariable> getEnvironmentVariables(final TestResult testResult) {
        return config.getEnvironmentVariables().stream()
                .flatMap(testResult::findAllLabelsStream)
                .map(label -> new EnvironmentVariable().setKey(label.getName()).setValue(label.getValue()))
                .collect(Collectors.toList());
    }

    private List<CustomField> getCustomFields(final TestResult testResult) {
        return config.getCustomFields().stream()
                .flatMap(testResult::findAllLabelsStream)
                .map(label -> new CustomField().setName(label.getName()).setValue(label.getValue()))
                .collect(Collectors.toList());
    }

    private List<TestTag> getTestTags(final TestResult testResult) {
        return config.getTags().stream()
                .map(testResult::findAllLabels)
                .flatMap(Collection::stream)
                .map(tag -> new TestTag().setValue(tag))
                .collect(Collectors.toList());
    }

    private Optional<String> calculateHistoryKey(final TestResult testResult) {
        if (isNull(testResult.getTestId())) {
            return Optional.empty();
        }
        final MessageDigest digest = md5();
        digest.update(testResult.getType().value().getBytes(UTF_8));
        digest.update(testResult.getTestId().getBytes(UTF_8));
        testResult.getParameters().stream()
                .filter(TestParameter::isNotHidden)
                .sorted(comparingParametersByNameAndValue())
                .forEachOrdered(parameter -> {
                    digest.update(Objects.toString(parameter.getName()).getBytes(UTF_8));
                    digest.update(Objects.toString(parameter.getValue()).getBytes(UTF_8));
                });
        testResult.getEnvironmentVariables().stream()
                .sorted(comparingEnvironmentByKeyAndValue())
                .forEachOrdered(variable -> {
                    digest.update(Objects.toString(variable.getKey()).getBytes(UTF_8));
                    digest.update(Objects.toString(variable.getValue()).getBytes(UTF_8));
                });

        final byte[] bytes = digest.digest();
        return Optional.of(new BigInteger(1, bytes).toString(16));
    }

    private List<AttachmentLink> extractAttachments(final TestResultStep step) {
        final Stream<AttachmentLink> childrenAttachments = step.getSteps().stream()
                .map(this::extractAttachments)
                .flatMap(Collection::stream);

        return Stream.concat(childrenAttachments, step.getAttachments().stream())
                .collect(Collectors.toList());
    }

}
