package io.qameta.allure.csv;

import com.opencsv.bean.CsvBindByName;
import com.opencsv.bean.CsvBindByPosition;
import io.qameta.allure.entity.Status;
import io.qameta.allure.entity.TestResult;
import io.qameta.allure.tree.TreeWidgetItem;

import java.io.Serializable;

/**
 * Class contains the information for the category csv export.
 *
 */
public class CsvExportCategory implements Serializable {

    @CsvBindByName(column = "Category")
    @CsvBindByPosition(position = 0)
    private String name;

    @CsvBindByName(column = "FAILED")
    @CsvBindByPosition(position = 1)
    private long failed;

    @CsvBindByName(column = "BROKEN")
    @CsvBindByPosition(position = 2)
    private long broken;

    @CsvBindByName(column = "PASSED")
    @CsvBindByPosition(position = 3)
    private long passed;

    @CsvBindByName(column = "SKIPPED")
    @CsvBindByPosition(position = 4)
    private long skipped;

    @CsvBindByName(column = "UNKNOWN")
    @CsvBindByPosition(position = 5)
    private long unknown;

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
