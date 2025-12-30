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
package io.qameta.allure.jira;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class JiraRetryInterceptor implements Interceptor {

    private static final long MAX_RETRY_AFTER_MILLIS = 60_000L;
    private static final Logger LOGGER = LoggerFactory.getLogger(JiraRetryInterceptor.class);
    private final int maxAttempts;
    private final long baseBackoffMillis;

    public JiraRetryInterceptor(final int maxAttempts, final long baseBackoffMillis) {
        this.maxAttempts = Math.max(1, maxAttempts);
        this.baseBackoffMillis = Math.max(0, baseBackoffMillis);
    }

    @Override
    public Response intercept(final Chain chain) throws IOException {
        Request request = chain.request();
        Response response = null;

        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            response = chain.proceed(request);

            if (!shouldRetry(response)) {
                return response;
            }


            if (attempt < maxAttempts) {
                long sleepMs = computeSleepMillis(response, attempt);
                LOGGER.warn("Jira request to {} failed with code {}. Retrying after {}ms (attempt {}/{})",
                        request.url(), response.code(), sleepMs, attempt, maxAttempts);
                response.close();
                sleepQuietly(sleepMs);
            }
        }

        LOGGER.error("Jira request to {} failed after {} attempts with code {}",
                request.url(), maxAttempts, response != null ? response.code() : "unknown");
        return response;
    }

    private static boolean shouldRetry(final Response response) {
        int code = response.code();
        return code == 429 || code == 502 || code == 503 || code == 504;
    }

    private long computeSleepMillis(final Response response, final int attempt) {
        if (response.code() == 429) {
            String retryAfter = response.header("Retry-After");
            Long retryAfterSeconds = parseLongOrNull(retryAfter);
            if (retryAfterSeconds != null && retryAfterSeconds >= 0) {
                long retryMs = retryAfterSeconds * 1000L;
                return Math.min(retryMs, MAX_RETRY_AFTER_MILLIS);
            }
        }

        return baseBackoffMillis * (1L << (attempt - 1));
    }

    private static Long parseLongOrNull(final String value) {
        if (value == null) return null;
        try {
            return Long.parseLong(value.trim());
        } catch (Exception ignored) {
            return null;
        }
    }

    private static void sleepQuietly(final long millis) {
        if (millis <= 0) return;
        try {
            Thread.sleep(millis);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        }
    }
}
