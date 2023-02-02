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

/**
 * @author etki
 */
public enum ExitCode {

    NO_ERROR(0),
    GENERIC_ERROR(1),
    ARGUMENT_PARSING_ERROR(127);

    private final int code;

    ExitCode(final int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public boolean isSuccess() {
        return 0 == code;
    }

}
