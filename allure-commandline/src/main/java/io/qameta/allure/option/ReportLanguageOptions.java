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
package io.qameta.allure.option;

import com.beust.jcommander.Parameter;
import io.qameta.allure.validator.LanguageValidator;

/**
 * Contains profile options.
 *
 * @since 2.0
 */
public class ReportLanguageOptions {

    @Parameter(
            names = {"--lang", "--report-language"},
            description = "The report language.",
            validateWith = LanguageValidator.class
    )
    private String reportLanguage;

    public String getReportLanguage() {
        return reportLanguage;
    }

}
