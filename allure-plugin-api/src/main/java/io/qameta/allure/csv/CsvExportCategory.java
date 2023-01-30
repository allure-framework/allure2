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
import io.qameta.allure.tree.TreeWidgetItem;

import java.io.Serializable;

/**
 * Class contains the information for the category csv export.
 *
 */
public class CsvExportCategory implements Serializable {

    @CsvBindByName(column = "Category")
    @CsvBindByPosition(position = 0)
    private final String name;

    @CsvBindByName(column = "FAILED")
    @CsvBindByPosition(position = 1)
    private final long failed;

    @CsvBindByName(column = "BROKEN")
    @CsvBindByPosition(position = 2)
    private final long broken;

    @CsvBindByName(column = "PASSED")
    @CsvBindByPosition(position = 3)
    private final long passed;

    @CsvBindByName(column = "SKIPPED")
    @CsvBindByPosition(position = 4)
    private final long skipped;

    @CsvBindByName(column = "UNKNOWN")
    @CsvBindByPosition(position = 5)
    private final long unknown;

    public CsvExportCategory(final TreeWidgetItem item) {
        this.name = item.getName();
        this.failed = item.getStatistic().getFailed();
        this.broken = item.getStatistic().getBroken();
        this.passed = item.getStatistic().getPassed();
        this.skipped = item.getStatistic().getSkipped();
        this.unknown = item.getStatistic().getUnknown();
    }

    public String getName() {
        return name;
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
}
