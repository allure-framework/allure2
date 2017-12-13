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
