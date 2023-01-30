/*
 *  Copyright 2016-2023 Qameta Software OÜ
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
package io.qameta.allure.idea;

import io.qameta.allure.Aggregator;
import io.qameta.allure.core.Configuration;
import io.qameta.allure.core.LaunchResults;
import io.qameta.allure.entity.Label;
import io.qameta.allure.entity.Link;
import io.qameta.allure.entity.TestResult;

import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static io.qameta.allure.util.PropertyUtils.getProperty;

/**
 * Plugins adds link in test result to open in Idea Project.
 */
public class IdeaLinksPlugin implements Aggregator {

    private static final String ALLURE_IDEA_ENABLED = "ALLURE_IDEA_ENABLED";
    private static final String ALLURE_IDEA_PORT = "ALLURE_IDEA_PORT";

    private static final String IDEA_LINK_NAME = "Open in Idea";
    private static final String IDEA_LINK_TYPE = "idea";

    private final boolean enabled;
    private final int port;

    public IdeaLinksPlugin() {
        this(
                getProperty(ALLURE_IDEA_ENABLED).map(Boolean::parseBoolean).orElse(false),
                getProperty(ALLURE_IDEA_PORT).map(Integer::parseInt).orElse(63_342)
        );
    }

    public IdeaLinksPlugin(final boolean enabled, final int port) {
        this.enabled = enabled;
        this.port = port;
    }

    @Override
    public void aggregate(final Configuration configuration,
                          final List<LaunchResults> launchesResults,
                          final Path outputDirectory) {
        if (enabled) {
            launchesResults.stream()
                    .map(LaunchResults::getAllResults)
                    .flatMap(Collection::stream)
                    .forEach(this::addIdeaLink);
        }
    }

    private void addIdeaLink(final TestResult testResult) {
        final String fileExtension = "java";

        final Optional<String> testClassName = testResult.getLabels().stream()
                .filter(label -> "testClass".equals(label.getName()))
                .map(Label::getValue)
                .findFirst();

        testClassName.ifPresent(name -> {
            final String path = name.replace(".", "/");
            final String url = String.format("http://localhost:%d/api/file?file=%s.%s", port, path, fileExtension);
            testResult.getLinks().add(new Link().setName(IDEA_LINK_NAME).setType(IDEA_LINK_TYPE).setUrl(url));
        });
    }

}
