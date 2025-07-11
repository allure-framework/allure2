/*
 *  Copyright 2016-2024 Qameta Software Inc
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
package io.qameta.allure.ga;

import com.fasterxml.jackson.databind.json.JsonMapper;
import io.qameta.allure.Aggregator2;
import io.qameta.allure.ReportStorage;
import io.qameta.allure.core.Configuration;
import io.qameta.allure.core.LaunchResults;
import io.qameta.allure.entity.ExecutorInfo;
import io.qameta.allure.entity.Label;
import io.qameta.allure.entity.LabelName;
import io.qameta.allure.executor.ExecutorPlugin;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

import static io.qameta.allure.Constants.NO_ANALYTICS;

/**
 * @author charlie (Dmitry Baev).
 */
public class GaPlugin implements Aggregator2 {

    private static final Logger LOGGER = LoggerFactory.getLogger(GaPlugin.class);

    private static final String LOCAL = "local";

    private static final String GA_ENDPOINT_FORMAT
            = "https://www.google-analytics.com/mp/collect?measurement_id=%s&api_secret=%s";

    private static final String MEASUREMENT_ID = "G-FVWC4GKEYS";
    private static final String GA_SECRET = "rboZz0HySdmCVIvtydmSTQ";

    private static final String GA_EVENT_NAME = "report_generated";

    @Override
    public void aggregate(final Configuration configuration,
                          final List<LaunchResults> launchesResults,
                          final ReportStorage storage) {
        if (Objects.nonNull(System.getenv(NO_ANALYTICS))) {
            LOGGER.debug("analytics is disabled");
            return;
        }

        LOGGER.debug("send analytics");
        final GaParameters parameters = new GaParameters()
                .setReportUuid(configuration.getUuid())
                .setAllureVersion(configuration.getVersion())
                .setExecutorType(getExecutorType(launchesResults))
                .setResultsCount(getTestResultsCount(launchesResults))
                .setResultsFormat(getLabelValuesAsString(launchesResults, LabelName.RESULT_FORMAT))
                .setFramework(getLabelValuesAsString(launchesResults, LabelName.FRAMEWORK))
                .setLanguage(getLabelValuesAsString(launchesResults, LabelName.LANGUAGE));

        final String cid = getClientId(launchesResults);

        try {
            CompletableFuture
                    .runAsync(() -> sendStats(cid, parameters))
                    .get(10, TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            LOGGER.debug("Could not send analytics within 10 seconds", e);
        }
    }

    protected void sendStats(final String clientId, final GaParameters parameters) {
        final HttpClientBuilder builder = HttpClientBuilder.create();
        try (CloseableHttpClient client = builder.build()) {
            final String uri = String.format(
                    GA_ENDPOINT_FORMAT,
                    MEASUREMENT_ID, GA_SECRET
            );
            final HttpPost post = new HttpPost(uri);
            final String stringBody = new JsonMapper().writeValueAsString(
                    new GaRequest()
                            .setClientId(clientId)
                            .setEvents(Collections.singletonList(new GaEvent()
                                    .setName(GA_EVENT_NAME)
                                    .setParams(parameters)
                            ))
            );
            final StringEntity entity = new StringEntity(
                    stringBody,
                    ContentType.APPLICATION_JSON.withCharset(StandardCharsets.UTF_8)
            );
            post.setEntity(entity);
            client.execute(post).close();
            LOGGER.debug("GA done");
        } catch (IOException e) {
            LOGGER.debug("Could not send analytics", e);
        }
    }

    private static String getClientId(final List<LaunchResults> launchesResults) {
        return ExecutorPlugin.getLatestExecutor(launchesResults)
                .map(ExecutorInfo::getBuildUrl)
                .flatMap(GaPlugin::getHostSafe)
                .map(DigestUtils::sha256Hex)
                .orElseGet(() -> getLocalHostName()
                        .map(DigestUtils::sha256Hex)
                        .orElse(UUID.randomUUID().toString())
                );
    }

    private static Optional<String> getHostSafe(final String url) {
        try {
            return Optional.of(URI.create(url))
                    .map(URI::getHost);
        } catch (Exception e) {
            LOGGER.debug("invalid build url", e);
        }
        return Optional.empty();
    }

    /* package-private */
    static String getExecutorType(final List<LaunchResults> launchesResults) {
        return ExecutorPlugin.getLatestExecutor(launchesResults)
                .map(ExecutorInfo::getType)
                .map(String::trim)
                .map(String::toLowerCase)
                .orElse(LOCAL);
    }

    private static long getTestResultsCount(final List<LaunchResults> launchesResults) {
        return launchesResults.stream()
                .map(LaunchResults::getResults)
                .mapToLong(Collection::size)
                .sum();
    }

    private static String getLabelValuesAsString(final List<LaunchResults> launchesResults,
                                                 final LabelName labelName) {
        final String values = launchesResults.stream()
                .flatMap(results -> results.getResults().stream())
                .flatMap(result -> result.getLabels().stream())
                .filter(label -> labelName.value().equals(label.getName()))
                .map(Label::getValue)
                .distinct()
                .sorted()
                .collect(Collectors.joining(" "))
                .toLowerCase();
        return values.isEmpty() ? "Undefined" : values;
    }

    private static Optional<String> getLocalHostName() {
        try {
            return Optional.ofNullable(InetAddress.getLocalHost().getHostName());
        } catch (UnknownHostException e) {
            LOGGER.debug("Could not get host name", e);
            return Optional.empty();
        }
    }

}
