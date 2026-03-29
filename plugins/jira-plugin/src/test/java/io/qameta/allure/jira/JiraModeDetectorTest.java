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

import org.junit.jupiter.api.Test;

import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class JiraModeDetectorTest {

    @Test
    void shouldDetectServerWhenNoCloudCredentials() {
        final Supplier<JiraCloudService> cloudSupplier = () -> null;

        final JiraService serverService = mock(JiraService.class);
        final JiraServerInfo serverInfo = new JiraServerInfo();
        serverInfo.setVersion("8.20.1");
        serverInfo.setServerTitle("Company Jira");

        when(serverService.getServerInfo()).thenReturn(serverInfo);

        final String mode = JiraModeDetector.detectMode(cloudSupplier, () -> serverService);

        assertThat(mode).isEqualTo("server");
    }

    @Test
    void shouldDetectCloudViaServerApiWhenDeploymentTypeIsCloud() {
        final Supplier<JiraCloudService> cloudSupplier = () -> null;

        final JiraService serverService = mock(JiraService.class);
        final JiraServerInfo serverInfo = new JiraServerInfo();
        serverInfo.setDeploymentType("Cloud");
        serverInfo.setVersion("1001.0.0");

        when(serverService.getServerInfo()).thenReturn(serverInfo);

        final String mode = JiraModeDetector.detectMode(cloudSupplier, () -> serverService);

        assertThat(mode).isEqualTo("cloud");
    }

    @Test
    void shouldDefaultToServerOnException() {
        final Supplier<JiraCloudService> cloudSupplier = () -> {
            throw new RuntimeException("Connection failed");
        };

        final JiraService serverService = mock(JiraService.class);
        when(serverService.getServerInfo()).thenThrow(new RuntimeException("Server connection failed"));

        final String mode = JiraModeDetector.detectMode(cloudSupplier, () -> serverService);

        assertThat(mode).isEqualTo("server");
    }

    @Test
    void shouldDefaultToServerWhenServerInfoIsNull() {
        final Supplier<JiraCloudService> cloudSupplier = () -> null;

        final JiraService serverService = mock(JiraService.class);
        when(serverService.getServerInfo()).thenReturn(null);

        final String mode = JiraModeDetector.detectMode(cloudSupplier, () -> serverService);

        assertThat(mode).isEqualTo("server");
    }
}
