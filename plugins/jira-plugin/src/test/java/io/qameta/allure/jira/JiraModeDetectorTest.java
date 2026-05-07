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
package io.qameta.allure.jira;

import io.qameta.allure.Allure;
import io.qameta.allure.Description;
import org.junit.jupiter.api.Test;

import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class JiraModeDetectorTest {

    /**
     * Verifies Jira mode detection falls back to server when cloud credentials are absent.
     * The test checks a regular server info response produces server mode.
     */
    @Description
    @Test
    void shouldDetectServerWhenNoCloudCredentials() {
        final Supplier<JiraCloudService> cloudSupplier = () -> null;

        final JiraService serverService = mock(JiraService.class);
        final JiraServerInfo serverInfo = new JiraServerInfo();
        serverInfo.setVersion("8.20.1");
        serverInfo.setServerTitle("Company Jira");

        when(serverService.getServerInfo()).thenReturn(serverInfo);
        attachServerInfo("server-info.txt", serverInfo);

        final String mode = detectMode(cloudSupplier, () -> serverService);

        assertThat(mode).isEqualTo("server");
    }

    /**
     * Verifies Jira mode detection recognizes cloud from the server info deployment type.
     * The test checks a Cloud deployment response produces cloud mode.
     */
    @Description
    @Test
    void shouldDetectCloudViaServerApiWhenDeploymentTypeIsCloud() {
        final Supplier<JiraCloudService> cloudSupplier = () -> null;

        final JiraService serverService = mock(JiraService.class);
        final JiraServerInfo serverInfo = new JiraServerInfo();
        serverInfo.setDeploymentType("Cloud");
        serverInfo.setVersion("1001.0.0");

        when(serverService.getServerInfo()).thenReturn(serverInfo);
        attachServerInfo("server-info.txt", serverInfo);

        final String mode = detectMode(cloudSupplier, () -> serverService);

        assertThat(mode).isEqualTo("cloud");
    }

    /**
     * Verifies Jira mode detection is conservative when service calls fail.
     * The test checks exceptions from both suppliers still produce server mode.
     */
    @Description
    @Test
    void shouldDefaultToServerOnException() {
        final Supplier<JiraCloudService> cloudSupplier = () -> {
            throw new RuntimeException("Connection failed");
        };

        final JiraService serverService = mock(JiraService.class);
        when(serverService.getServerInfo()).thenThrow(new RuntimeException("Server connection failed"));

        final String mode = detectMode(cloudSupplier, () -> serverService);

        assertThat(mode).isEqualTo("server");
    }

    /**
     * Verifies Jira mode detection treats missing server info as server mode.
     * The test checks a null server info response does not switch to cloud mode.
     */
    @Description
    @Test
    void shouldDefaultToServerWhenServerInfoIsNull() {
        final Supplier<JiraCloudService> cloudSupplier = () -> null;

        final JiraService serverService = mock(JiraService.class);
        when(serverService.getServerInfo()).thenReturn(null);

        final String mode = detectMode(cloudSupplier, () -> serverService);

        assertThat(mode).isEqualTo("server");
    }

    private String detectMode(final Supplier<JiraCloudService> cloudSupplier,
                              final Supplier<JiraService> serverSupplier) {
        return Allure.step("Detect Jira mode", () -> {
            final String mode = JiraModeDetector.detectMode(cloudSupplier, serverSupplier);
            Allure.addAttachment("jira-mode-output.txt", "text/plain", "mode=" + mode);
            return mode;
        });
    }

    private void attachServerInfo(final String fileName, final JiraServerInfo serverInfo) {
        Allure.step(
                "Attach Jira server info", () -> Allure.addAttachment(
                        fileName, "text/plain", String.format(
                                "deploymentType=%s%nversion=%s%nserverTitle=%s",
                                serverInfo.getDeploymentType(),
                                serverInfo.getVersion(),
                                serverInfo.getServerTitle()
                        )
                )
        );
    }
}
