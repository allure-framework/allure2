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
import io.qameta.allure.entity.TestResult;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;

/**
 * Class contains the information for the suites csv export.
 *
 */
@SuppressWarnings("PMD.StringToString")
public class CsvExportSuite implements Serializable {

    @CsvBindByName(column = "Status")
    @CsvBindByPosition(position = 0)
    private final String status;

    @CsvBindByName(column = "Start Time")
    @CsvBindByPosition(position = 1)
    private final String start;

    @CsvBindByName(column = "Stop Time")
    @CsvBindByPosition(position = 2)
    private final String stop;

    @CsvBindByName(column = "Duration in ms")
    @CsvBindByPosition(position = 3)
    private final String duration;

    @CsvBindByName(column = "Parent Suite")
    @CsvBindByPosition(position = 4)
    private final String parentSuite;

    @CsvBindByName(column = "Suite")
    @CsvBindByPosition(position = 5)
    private final String suite;

    @CsvBindByName(column = "Sub Suite")
    @CsvBindByPosition(position = 6)
    private final String subSuite;

    @CsvBindByName(column = "Test Class")
    @CsvBindByPosition(position = 7)
    private final String testClass;

    @CsvBindByName(column = "Test Method")
    @CsvBindByPosition(position = 8)
    private final String testMethod;

    @CsvBindByName(column = "Name")
    @CsvBindByPosition(position = 9)
    private final String name;

    @CsvBindByName(column = "Description")
    @CsvBindByPosition(position = 10)
    private final String description;

    public CsvExportSuite(final TestResult result) {
        final Map<String, String> resultMap = result.toMap();
        this.status = result.getStatus() != null ? result.getStatus().value() : null;
        this.duration = result.getTime().getDuration() != null ? result.getTime().getDuration().toString() : null;
        this.start = result.getTime().getStart() != null ? new Date(result.getTime().getStart()).toString() : null;
        this.stop = result.getTime().getStop() != null ? new Date(result.getTime().getStop()).toString() : null;
        this.parentSuite = resultMap.getOrDefault("parentSuite", null);
        this.suite = resultMap.getOrDefault("suite", null);
        this.subSuite = resultMap.getOrDefault("subSuite", null);
        this.testClass = resultMap.getOrDefault("testClass", null);
        this.testMethod = resultMap.getOrDefault("testMethod", null);
        this.name = result.getName();
        this.description = result.getDescription();
    }

    public String getStatus() {
        return status;
    }

    public String getDuration() {
        return duration;
    }

    public String getStart() {
        return start;
    }

    public String getStop() {
        return stop;
    }

    public String getParentSuite() {
        return parentSuite;
    }

    public String getSuite() {
        return suite;
    }

    public String getSubSuite() {
        return subSuite;
    }

    public String getTestClass() {
        return testClass;
    }

    public String getTestMethod() {
        return testMethod;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }
}
