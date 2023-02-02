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
package io.qameta.allure.csv;

import com.opencsv.bean.CsvBindByName;
import com.opencsv.bean.CsvBindByPosition;
import io.qameta.allure.entity.Status;
import io.qameta.allure.entity.TestResult;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;

/**
 * Class contains the information for the behavior csv export.
 *
 */
public class CsvExportBehavior implements Serializable {

    @CsvBindByName(column = "Epic")
    @CsvBindByPosition(position = 0)
    private final String epic;

    @CsvBindByName(column = "Feature")
    @CsvBindByPosition(position = 1)
    private final String feature;

    @CsvBindByName(column = "Story")
    @CsvBindByPosition(position = 2)
    private final String story;

    @CsvBindByName(column = "FAILED")
    @CsvBindByPosition(position = 3)
    private long failed;

    @CsvBindByName(column = "BROKEN")
    @CsvBindByPosition(position = 4)
    private long broken;

    @CsvBindByName(column = "PASSED")
    @CsvBindByPosition(position = 5)
    private long passed;

    @CsvBindByName(column = "SKIPPED")
    @CsvBindByPosition(position = 6)
    private long skipped;

    @CsvBindByName(column = "UNKNOWN")
    @CsvBindByPosition(position = 7)
    private long unknown;

    public CsvExportBehavior(final String epic, final String feature, final String story) {
        this.epic = epic;
        this.feature = feature;
        this.story = story;
    }

    public String getEpic() {
        return epic;
    }

    public String getFeature() {
        return feature;
    }

    public String getStory() {
        return story;
    }

    public long getFailed() {
        return failed;
    }

    public long getBroken() {
        return broken;
    }

    public long getPassed() {
        return passed;
    }

    public long getSkipped() {
        return skipped;
    }

    public long getUnknown() {
        return unknown;
    }

    public boolean isPassed(final String epic, final String feature, final String story) {
        return StringUtils.equals(this.epic, epic)
                && StringUtils.equals(this.feature, feature)
                && StringUtils.equals(this.story, story);
    }

    public void addTestResult(final TestResult result) {
        if (Status.FAILED.equals(result.getStatus())) {
            this.failed++;
        }
        if (Status.BROKEN.equals(result.getStatus())) {
            this.broken++;
        }
        if (Status.PASSED.equals(result.getStatus())) {
            this.passed++;
        }
        if (Status.SKIPPED.equals(result.getStatus())) {
            this.skipped++;
        }
        if (Status.UNKNOWN.equals(result.getStatus())) {
            this.unknown++;
        }
    }
}
