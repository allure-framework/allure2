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
package io.qameta.allure.validator;

import com.beust.jcommander.IParameterValidator;
import com.beust.jcommander.ParameterException;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * @author charlie (Dmitry Baev).
 */
public class LanguageValidator implements IParameterValidator {

    private static final Set<String> SUPPORTED_LANGUAGES = Collections.unmodifiableSet(
            new HashSet<>(Arrays.asList(
                    "en",
                    "ru",
                    "zh",
                    "de",
                    "nl",
                    "he",
                    "br",
                    "pl",
                    "ja",
                    "es",
                    "kr",
                    "fr",
                    "az",
                    "tr",
                    "sv",
                    "isv"
            ))
    );

    @Override
    public void validate(final String name, final String value) {
        if (!SUPPORTED_LANGUAGES.contains(value)) {
            throw new ParameterException(
                    "invalid language value. Supported values are: "
                    + String.join(", ", SUPPORTED_LANGUAGES)
            );
        }
    }
}
