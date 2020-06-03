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
