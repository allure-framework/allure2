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

import io.qameta.allure.Aggregator2;
import io.qameta.allure.Constants;
import io.qameta.allure.ReportStorage;
import io.qameta.allure.core.Configuration;
import io.qameta.allure.core.LaunchResults;
import io.qameta.allure.entity.Attachment;
import io.qameta.allure.entity.GlobalAttachment;

import java.util.List;
import java.util.Objects;

/**
 * Generates the run-level data consumed by the report UI.
 */
public class GlobalsPlugin implements Aggregator2 {

    public static final String JSON_FILE_NAME = "globals.json";

    @Override
    public void aggregate(final Configuration configuration,
                          final List<LaunchResults> launchesResults,
                          final ReportStorage storage) {
        final GlobalsData data = new GlobalsData();
        launchesResults.forEach(launch -> {
            data.getErrors().addAll(launch.getGlobalErrors());
            launch.getGlobalAttachments().stream()
                    .filter(attachment -> hasContent(launch, attachment))
                    .forEach(data.getAttachments()::add);
        });

        storage.addDataJson(Constants.widgetsPath(JSON_FILE_NAME), data);
    }

    private static boolean hasContent(final LaunchResults launch, final GlobalAttachment globalAttachment) {
        return launch.getAttachments().values().stream()
                .map(Attachment::getSource)
                .anyMatch(source -> Objects.equals(source, globalAttachment.getSource()));
    }

}
