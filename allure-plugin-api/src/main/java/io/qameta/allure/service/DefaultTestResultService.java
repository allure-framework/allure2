package io.qameta.allure.service;

import io.qameta.allure.config.ReportConfig;
import io.qameta.allure.entity.CustomField;
import io.qameta.allure.entity.EnvironmentVariable;
import io.qameta.allure.entity.TestParameter;
import io.qameta.allure.entity.TestResult;
import io.qameta.allure.entity.TestResultExecution;
import io.qameta.allure.entity.TestResultType;
import io.qameta.allure.entity.TestTag;
import io.qameta.allure.exception.NotFoundException;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import static io.qameta.allure.entity.EntityComparators.comparingEnvironmentByKeyAndValue;
import static io.qameta.allure.entity.EntityComparators.comparingParametersByNameAndValue;
import static io.qameta.allure.util.Hashing.md5;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Objects.isNull;

/**
 * @author charlie (Dmitry Baev).
 */
public class DefaultTestResultService implements TestResultService {

    private final AtomicLong testResultId = new AtomicLong();

    private final Map<Long, TestResult> results = new ConcurrentHashMap<>();

    private final Map<Long, TestResultExecution> executions = new ConcurrentHashMap<>();

    private final ReportConfig config;

    public DefaultTestResultService(final ReportConfig config) {
        this.config = config;
    }

    @Override
    public TestResult create(final TestResult testResult) {
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
            testResult.setHistoryKey(calculateHistoryKey(testResult));
        }

        processRetries(testResult);
        results.put(testResult.getId(), testResult);
        return testResult;
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
    public List<TestResult> findAll() {
        return Collections.unmodifiableList(new ArrayList<>(results.values()));
    }

    @Override
    public List<TestResult> findAllTests() {
        return results.values().stream()
                .filter(result -> TestResultType.TEST.equals(result.getType()))
                .collect(Collectors.toList());
    }

    @Override
    public List<TestResult> findHistory(final Long id) {
        return Collections.emptyList();
    }

    @Override
    public List<TestResult> findRetries(final Long id) {
        final TestResult result = Optional.ofNullable(results.get(id))
                .orElseThrow(() -> new NotFoundException("Result with id %d not found", id));

        return results.values().stream()
                .filter(item -> Objects.equals(item.getHistoryKey(), result.getHistoryKey()))
                .collect(Collectors.toList());
    }

    @Override
    public List<TestResult> findAllParents(final Long id) {
        return Collections.emptyList();
    }

    //TODO sync
    private void processRetries(final TestResult testResult) {
        final Optional<TestResult> found = results.values().stream()
                .filter(result -> !result.isHidden())
                .filter(result -> Objects.equals(result.getHistoryKey(), result.getHistoryKey()))
                .findAny();

        if (found.isPresent()) {
            final TestResult retryCandidate = found.get();
            if (isNull(testResult.getStart()) || isNull(retryCandidate.getStart())
                    || retryCandidate.getStart() <= testResult.getStart()) {
                retryCandidate.setHidden(true);
            } else {
                testResult.setHidden(true);
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

    private String calculateHistoryKey(final TestResult testResult) {
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
        return new BigInteger(1, bytes).toString(16);
    }

}
