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
package io.qameta.allure.option;

import com.beust.jcommander.Parameter;
import io.qameta.allure.convert.PathConverter;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static java.util.Collections.singletonList;

/**
 * Contains results options.
 *
 * @since 2.0
 */
@SuppressWarnings("PMD.ImmutableField")
public class ResultsOptions {

    @Parameter(
            description = "The directories with allure results",
            converter = PathConverter.class
    )
    private List<Path> resultsDirectories = new ArrayList<>(singletonList(Paths.get("allure-results")));

    public List<Path> getResultsDirectories() {
        return Collections.unmodifiableList(resultsDirectories);
    }
}
