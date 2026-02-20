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
package io.qameta.allure.jira;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.qameta.allure.entity.Status;

import java.io.Serializable;

public class JiraCloudTestSummary implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final String STATUS_UNKNOWN = "UNKNOWN";

    @JsonProperty("status")
    private String status;

    @JsonProperty("passed")
    private int passed;

    @JsonProperty("failed")
    private int failed;

    @JsonProperty("broken")
    private int broken;

    @JsonProperty("skipped")
    private int skipped;

    @JsonProperty("unknown")
    private int unknown;

    @JsonProperty("reportUrl")
    private String reportUrl;

    public JiraCloudTestSummary() {
        this.status = STATUS_UNKNOWN;
    }

    public void addTestResult(final Status testStatus) {
        if (testStatus == null) {
            unknown++;
            updateOverallStatus();
            return;
        }

        switch (testStatus) {
            case PASSED:
                passed++;
                break;
            case FAILED:
                failed++;
                break;
            case BROKEN:
                broken++;
                break;
            case SKIPPED:
                skipped++;
                break;
            default:
                unknown++;
                break;
        }

        updateOverallStatus();
    }

    private void updateOverallStatus() {
        if (failed > 0) {
            status = Status.FAILED.value();
        } else if (broken > 0) {
            status = Status.BROKEN.value();
        } else if (passed > 0) {
            status = Status.PASSED.value();
        } else if (skipped > 0) {
            status = Status.SKIPPED.value();
        } else {
            status = Status.UNKNOWN.value();
        }
    }
    public String getStatus() {
        return status;
    }

    public void setStatus(final String status) {
        this.status = status;
    }

    public int getPassed() {
        return passed;
    }

    public void setPassed(final int passed) {
        this.passed = passed;
    }

    public int getFailed() {
        return failed;
    }

    public void setFailed(final int failed) {
        this.failed = failed;
    }

    public int getBroken() {
        return broken;
    }

    public void setBroken(final int broken) {
        this.broken = broken;
    }

    public int getSkipped() {
        return skipped;
    }

    public void setSkipped(final int skipped) {
        this.skipped = skipped;
    }

    public int getUnknown() {
        return unknown;
    }

    public void setUnknown(final int unknown) {
        this.unknown = unknown;
    }

    public String getReportUrl() {
        return reportUrl;
    }

    public void setReportUrl(final String reportUrl) {
        this.reportUrl = reportUrl;
    }

    @Override
    public String toString() {
        return new StringBuilder("JiraCloudTestSummary{")
            .append("status='").append(status).append('\'')
            .append(", passed=").append(passed)
            .append(", failed=").append(failed)
            .append(", broken=").append(broken)
            .append(", skipped=").append(skipped)
            .append(", unknown=").append(unknown)
            .append(", reportUrl='").append(reportUrl).append('\'')
            .append('}')
            .toString();
    }
}
