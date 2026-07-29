/*
 *  Copyright 2016-2026 Qameta Software Inc
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package io.qameta.allure.allure2;

import io.qameta.allure.model.Label;
import io.qameta.allure.model.Parameter;
import io.qameta.allure.model.TestResult;

import javax.xml.bind.DatatypeConverter;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

/**
 * Calculates history and retry identifiers for Allure 2 result files using the Allure 3 contract.
 */
final class Allure2RetryHashCalculator {

    private static final String ALLURE_ID_LABEL = "ALLURE_ID";
    private static final String AS_ID_LABEL = "AS_ID";
    private static final String DEFAULT_ENVIRONMENT = "default";
    private static final String HISTORY_ID_SEPARATOR = ".";
    private static final String HASH_PART_SEPARATOR = ":";
    private static final String UNKNOWN_VALUE = "#___unknown_value___#";

    private static final Comparator<Parameter> PARAMETER_COMPARATOR = Comparator
            .comparing(Parameter::getName)
            .thenComparing(Allure2RetryHashCalculator::getParameterValue);

    private Allure2RetryHashCalculator() {
        throw new IllegalStateException("Do not instance");
    }

    static Identifiers calculate(final TestResult result) {
        final String testCaseIdentity = getTestCaseIdentity(result);
        if (isNull(testCaseIdentity)) {
            return new Identifiers(null, null);
        }

        final String testCaseId = md5(testCaseIdentity);
        final String parametersHash = md5(stringifyRetryParameters(result.getParameters()));
        final String historyId = testCaseId + HISTORY_ID_SEPARATOR + parametersHash;
        final String retryHash = md5(
                testCaseId
                        + HASH_PART_SEPARATOR
                        + parametersHash
                        + HASH_PART_SEPARATOR
                        + DEFAULT_ENVIRONMENT
        );
        return new Identifiers(historyId, retryHash);
    }

    private static String getTestCaseIdentity(final TestResult result) {
        final String allureId = getAllureId(result.getLabels());
        if (nonNull(allureId)) {
            return ALLURE_ID_LABEL + "=" + allureId;
        }
        if (isNotEmpty(result.getTestCaseId())) {
            return result.getTestCaseId();
        }
        if (isNotEmpty(result.getFullName())) {
            return result.getFullName();
        }
        return null;
    }

    private static String getAllureId(final List<Label> labels) {
        if (isNull(labels)) {
            return null;
        }
        return labels.stream()
                .filter(Objects::nonNull)
                .filter(label -> ALLURE_ID_LABEL.equals(label.getName()) || AS_ID_LABEL.equals(label.getName()))
                .findFirst()
                .map(Label::getValue)
                .filter(Allure2RetryHashCalculator::isNotEmpty)
                .orElse(null);
    }

    private static String stringifyRetryParameters(final List<Parameter> parameters) {
        if (isNull(parameters)) {
            return "";
        }
        return parameters.stream()
                .filter(Objects::nonNull)
                .filter(parameter -> isNotEmpty(parameter.getName()))
                .filter(parameter -> !Boolean.TRUE.equals(parameter.getExcluded()))
                .sorted(PARAMETER_COMPARATOR)
                .map(parameter -> parameter.getName() + HASH_PART_SEPARATOR + getParameterValue(parameter))
                .collect(Collectors.joining(","));
    }

    private static String getParameterValue(final Parameter parameter) {
        return Objects.toString(parameter.getValue(), UNKNOWN_VALUE);
    }

    private static boolean isNotEmpty(final String value) {
        return nonNull(value) && !value.isEmpty();
    }

    private static String md5(final String value) {
        try {
            final MessageDigest digest = MessageDigest.getInstance("MD5");
            final byte[] bytes = digest.digest(value.getBytes(StandardCharsets.UTF_8));
            return DatatypeConverter.printHexBinary(bytes).toLowerCase(Locale.ROOT);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("Could not find MD5 hashing algorithm", e);
        }
    }

    static final class Identifiers {

        private final String historyId;
        private final String retryHash;

        private Identifiers(final String historyId, final String retryHash) {
            this.historyId = historyId;
            this.retryHash = retryHash;
        }

        String getHistoryId() {
            return historyId;
        }

        String getRetryHash() {
            return retryHash;
        }
    }
}
