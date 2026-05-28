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
package io.qameta.allure.timeline;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import io.qameta.allure.Allure;
import io.qameta.allure.Description;
import io.qameta.allure.core.LaunchResults;
import io.qameta.allure.entity.LabelName;
import io.qameta.allure.entity.Status;
import io.qameta.allure.entity.TestResult;
import io.qameta.allure.entity.Time;
import io.qameta.allure.retry.RetryPlugin;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static io.qameta.allure.testdata.TestData.createSingleLaunchResults;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

class TimelinePluginTest {

    private static final String FIRST_UID = "first-attempt";
    private static final String SECOND_UID = "second-attempt";
    private static final String LATEST_UID = "latest-attempt";

    private final ObjectMapper mapper = JsonMapper.builder()
            .serializationInclusion(JsonInclude.Include.NON_NULL)
            .build();

    private final RetryPlugin retryPlugin = new RetryPlugin();

    private final TimelinePlugin timelinePlugin = new TimelinePlugin();

    /**
     * Verifies timeline JSON marks only retry attempts so the UI can render them as retry bars.
     */
    @Description
    @Test
    void shouldMarkRetriesInTimelineJson() throws Exception {
        Allure.label("component", "timeline");
        final String historyId = UUID.randomUUID().toString();
        final List<LaunchResults> launchResults = createSingleLaunchResults(
                createTestResult("first attempt", FIRST_UID, historyId, 1L, 9L),
                createTestResult("second attempt", SECOND_UID, historyId, 11L, 19L),
                createTestResult("latest attempt", LATEST_UID, historyId, 21L, 29L)
        );

        Allure.step("Aggregate retries before timeline generation", () -> {
            retryPlugin.aggregate(null, launchResults, null);
        });

        final JsonNode timeline = Allure.step(
                "Generate timeline tree after retry aggregation",
                () -> mapper.valueToTree(timelinePlugin.getData(launchResults))
        );
        Allure.addAttachment(
                "timeline-tree.json",
                "application/json",
                mapper.writerWithDefaultPrettyPrinter().writeValueAsString(timeline),
                ".json"
        );

        Allure.step("Verify only older attempts are marked as retry leaves", () -> {
            final JsonNode firstAttempt = findByUid(timeline, FIRST_UID);
            final JsonNode secondAttempt = findByUid(timeline, SECOND_UID);
            final JsonNode latestAttempt = findByUid(timeline, LATEST_UID);

            assertThat(firstAttempt.path("retry").asBoolean())
                    .as("first attempt retry flag")
                    .isTrue();
            assertThat(secondAttempt.path("retry").asBoolean())
                    .as("second attempt retry flag")
                    .isTrue();
            assertThat(latestAttempt.has("retry"))
                    .as("latest attempt retry flag")
                    .isFalse();
            assertThat(fieldNames(firstAttempt))
                    .as("retry attempt timeline fields")
                    .containsExactlyInAnyOrder("name", "uid", "status", "time", "retry");
            assertThat(fieldNames(latestAttempt))
                    .as("latest attempt timeline fields")
                    .containsExactlyInAnyOrder("name", "uid", "status", "time");
        });
    }

    private TestResult createTestResult(final String name,
                                        final String uid,
                                        final String historyId,
                                        final long start,
                                        final long stop) {
        return new TestResult()
                .setName(name)
                .setUid(uid)
                .setHistoryId(historyId)
                .setStatus(Status.BROKEN)
                .setLabels(
                        asList(
                                LabelName.HOST.label("agent.local"),
                                LabelName.THREAD.label("worker-1")
                        )
                )
                .setTime(new Time().setStart(start).setStop(stop).setDuration(stop - start));
    }

    private JsonNode findByUid(final JsonNode node, final String uid) {
        final JsonNode found = findByUidOrMissing(node, uid);
        assertThat(found.isMissingNode())
                .as("timeline node with uid %s", uid)
                .isFalse();
        return found;
    }

    private List<String> fieldNames(final JsonNode node) {
        final List<String> names = new ArrayList<>();
        node.fieldNames().forEachRemaining(names::add);
        return names;
    }

    private JsonNode findByUidOrMissing(final JsonNode node, final String uid) {
        if (uid.equals(node.path("uid").asText(null))) {
            return node;
        }

        for (JsonNode child : node.path("children")) {
            final JsonNode found = findByUidOrMissing(child, uid);
            if (!found.isMissingNode()) {
                return found;
            }
        }

        return mapper.getNodeFactory().missingNode();
    }
}
