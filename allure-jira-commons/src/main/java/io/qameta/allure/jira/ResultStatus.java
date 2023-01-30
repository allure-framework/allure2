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
package io.qameta.allure.jira;

import io.qameta.allure.entity.Status;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.Accessors;

/**
 * Mutable Test Results Enum for Jira Integration.
 */

@AllArgsConstructor
@Accessors(fluent = true)
@Getter
public enum ResultStatus {
    FAILED(Status.FAILED, "f90602"),
    BROKEN(Status.BROKEN, "febe0d"),
    PASSED(Status.PASSED, "78b63c"),
    SKIPPED(Status.SKIPPED, "888888"),
    UNKNOWN(Status.UNKNOWN, "bf34a6");


    private final Status statusName;
    private final String color;


}
