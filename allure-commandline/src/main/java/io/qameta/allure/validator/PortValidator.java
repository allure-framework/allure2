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
package io.qameta.allure.validator;

import com.beust.jcommander.IParameterValidator;
import com.beust.jcommander.ParameterException;

/**
 * @author charlie (Dmitry Baev).
 */
public class PortValidator implements IParameterValidator {

    private static final int MAX_PORT_VALUE = 65_535;
    private static final String MESSAGE = "invalid port value. Should be an integer between 0 and 65535";

    @Override
    public void validate(final String name, final String value) {
        try {
            final int port = Integer.parseInt(value);
            if (port < 0 || port > MAX_PORT_VALUE) {
                throw new ParameterException(MESSAGE);
            }
        } catch (NumberFormatException e) {
            throw new ParameterException(MESSAGE, e);
        }
    }
}
