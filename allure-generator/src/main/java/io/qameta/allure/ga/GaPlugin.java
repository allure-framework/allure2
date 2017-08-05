package io.qameta.allure.ga;

import io.qameta.allure.Aggregator;
import io.qameta.allure.core.Configuration;
import io.qameta.allure.core.LaunchResults;
import io.qameta.allure.entity.ExecutorInfo;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static io.qameta.allure.executor.ExecutorPlugin.EXECUTORS_BLOCK_NAME;

/**
 * @author charlie (Dmitry Baev).
 */
public class GaPlugin implements Aggregator {

    private static final Logger LOGGER = LoggerFactory.getLogger(GaPlugin.class);

    private static final String GA_DISABLE = "ALLURE_NO_ANALYTICS";

    private static final String GA_ID = "UA-88115679-3";
    private static final String GA_ENDPOINT = "https://www.google-analytics.com/collect";
    private static final String GA_API_VERSION = "1";

    @Override
    public void aggregate(final Configuration configuration,
                          final List<LaunchResults> launchesResults,
                          final Path outputDirectory) throws IOException {
        if (Objects.nonNull(System.getenv(GA_DISABLE))) {
            LOGGER.debug("analytics is disabled");
            return;
        }
        LOGGER.debug("send analytics");
        final int pluginsCount = configuration.getPlugins().size();
        final long testResultsCount = launchesResults.stream()
                .map(LaunchResults::getResults)
                .mapToLong(Collection::size)
                .sum();

        final String executor = launchesResults.stream()
                .map(results -> results.<ExecutorInfo>getExtra(EXECUTORS_BLOCK_NAME))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .findFirst()
                .map(ExecutorInfo::getType)
                .orElse("Local");

        final String allureVersion = Optional.of(getClass())
                .map(Class::getPackage)
                .map(Package::getImplementationVersion)
                .orElse("Undefined");

        try {
            CompletableFuture
                    .runAsync(() -> sendStats(allureVersion, testResultsCount, pluginsCount, executor))
                    .get(10, TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            LOGGER.debug("Could not send analytics within 10 seconds", e);
        }
    }

    @SuppressWarnings("EmptyTryBlock")
    protected void sendStats(final String allureVersion, final long testResultsCount,
                             final long pluginsCount, final String executor) {
        final HttpClientBuilder builder = HttpClientBuilder.create();
        try (CloseableHttpClient client = builder.build()) {
            List<NameValuePair> pairs = Arrays.asList(
                    pair("v", GA_API_VERSION),
                    pair("aip", GA_API_VERSION),
                    pair("tid", GA_ID),
                    pair("z", UUID.randomUUID().toString()),
                    pair("t", "event"),
                    pair("ds", "allure cli"),
                    pair("cid", UUID.randomUUID().toString()),
                    pair("an", "Allure Report"),
                    pair("ec", "Allure CLI events"),
                    pair("ea", "Report generate"),
                    pair("av", allureVersion),
                    pair("ds", "Report generator"),
                    pair("cd2", executor),
                    pair("cm1", String.valueOf(testResultsCount)),
            );
            final HttpPost post = new HttpPost(GA_ENDPOINT);
            final UrlEncodedFormEntity entity = new UrlEncodedFormEntity(pairs, StandardCharsets.UTF_8);
            post.setEntity(entity);
            client.execute(post).close();
            LOGGER.debug("GA done");
        } catch (IOException e) {
            LOGGER.debug("Could not send analytics", e);
        }
    }

    private static NameValuePair pair(final String v, final String value) {
        return new BasicNameValuePair(v, value);
    }

}
