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
package io.qameta.allure.command;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.beust.jcommander.ParametersDelegate;
import io.qameta.allure.convert.PathConverter;
import io.qameta.allure.option.HostPortOptions;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Contains options for open command.
 *
 * @since 2.0
 */
@SuppressWarnings("PMD.ImmutableField")
@Parameters(commandDescription = "Open generated report")
public class OpenCommand {

    @Parameter(
            description = "The report directory",
            converter = PathConverter.class
    )
    private List<Path> reportDirectories = new ArrayList<>(Collections.singletonList(Paths.get("allure-report")));

    @ParametersDelegate
    private HostPortOptions hostPortOptions = new HostPortOptions();

    public List<Path> getReportDirectories() {
        return Collections.unmodifiableList(reportDirectories);
    }

    public HostPortOptions getHostPortOptions() {
        return hostPortOptions;
    }
}
