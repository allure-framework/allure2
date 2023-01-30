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
import io.qameta.allure.validator.PortValidator;

/**
 * Contains port options.
 *
 * @since 2.0
 */
@SuppressWarnings("PMD.ImmutableField")
public class HostPortOptions {

    @Parameter(
            names = {"-p", "--port"},
            description = "This port will be used to start web server for the report.",
            validateWith = PortValidator.class
    )
    private int port;

    @Parameter(
            names = {"-h", "--host"},
            description = "This host will be used to start web server for the report."
    )
    private String host;

    public int getPort() {
        return port;
    }

    public String getHost() {
        return host;
    }
}
