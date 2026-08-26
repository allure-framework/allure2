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
package io.qameta.allure.globals;

import io.qameta.allure.Allure;
import io.qameta.allure.ConfigurationBuilder;
import io.qameta.allure.DefaultLaunchResults;
import io.qameta.allure.Description;
import io.qameta.allure.ReportStorage;
import io.qameta.allure.core.Configuration;
import io.qameta.allure.entity.Attachment;
import io.qameta.allure.entity.GlobalAttachment;
import io.qameta.allure.entity.GlobalError;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class GlobalsPluginTest {

    /**
     * Verifies aggregating run errors and only attachments backed by report content.
     */
    @Description
    @Test
    void shouldAggregateGlobalsAndExcludeAttachmentsWithoutContent(@TempDir final Path temp) {
        final GlobalError error = new GlobalError()
                .setMessage("Run setup failed");
        final GlobalAttachment available = new GlobalAttachment()
                .setUid("available")
                .setName("Run log")
                .setSource("available.txt")
                .setType("text/plain")
                .setSize(12L);
        final GlobalAttachment missing = new GlobalAttachment()
                .setUid("missing")
                .setName("Missing log")
                .setSource("missing.txt")
                .setType("text/plain");
        final Attachment stored = new Attachment()
                .setUid("available")
                .setName("Run log")
                .setSource("available.txt")
                .setType("text/plain")
                .setSize(12L);
        final DefaultLaunchResults launch = new DefaultLaunchResults(
                Set.of(),
                Map.of(temp.resolve("run.log"), stored),
                Map.of(),
                List.of(error),
                List.of(available, missing)
        );
        final Configuration configuration = ConfigurationBuilder.empty().build();
        final ReportStorage storage = mock();

        Allure.step(
                "Aggregate run-level errors and attachments",
                () -> new GlobalsPlugin().aggregate(configuration, List.of(launch), storage)
        );

        final ArgumentCaptor<Object> dataCaptor = ArgumentCaptor.captor();
        verify(storage, times(1)).addDataJson(eq("widgets/globals.json"), dataCaptor.capture());
        assertThat(dataCaptor.getValue()).isInstanceOf(GlobalsData.class);

        final GlobalsData data = (GlobalsData) dataCaptor.getValue();
        assertThat(data.getErrors()).containsExactly(error);
        assertThat(data.getAttachments()).containsExactly(available);
    }

}
