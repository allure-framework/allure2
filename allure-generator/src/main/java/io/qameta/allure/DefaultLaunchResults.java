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
package io.qameta.allure;

import io.qameta.allure.core.LaunchResults;
import io.qameta.allure.entity.Attachment;
import io.qameta.allure.entity.TestResult;

import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Default implementation of {@link LaunchResults}. Stores all the results
 * into memory.
 *
 * @since 2.0
 */
public class DefaultLaunchResults implements LaunchResults {

    private final Set<TestResult> results;

    private final Map<Path, Attachment> attachments;

    private final Map<String, Object> extra;

    public DefaultLaunchResults(final Set<TestResult> results,
                                final Map<Path, Attachment> attachments,
                                final Map<String, Object> extra) {
        this.results = results;
        this.attachments = attachments;
        this.extra = extra;
    }

    @Override
    public Set<TestResult> getAllResults() {
        return results;
    }

    @Override
    public Map<Path, Attachment> getAttachments() {
        return attachments;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> Optional<T> getExtra(final String name) {
        return Optional.ofNullable((T) extra.get(name));
    }
}
