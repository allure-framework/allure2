package io.qameta.allure.csv;

import com.opencsv.bean.CsvBindByName;
import com.opencsv.bean.CsvBindByPosition;
import io.qameta.allure.entity.Status;
import io.qameta.allure.entity.TestResult;

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
    private int failed;

    @CsvBindByName(column = "BROKEN")
    @CsvBindByPosition(position = 2)
    private int broken;

    @CsvBindByName(column = "PASSED")
    @CsvBindByPosition(position = 3)
    private int passed;

    @CsvBindByName(column = "SKIPPED")
    @CsvBindByPosition(position = 4)
    private int skipped;

    @CsvBindByName(column = "UNKNOWN")
    @CsvBindByPosition(position = 5)
    private int unknown;

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public int getFailed() {
        return failed;
    }

    public int getBroken() {
        return broken;
    }

    public int getPassed() {
        return passed;
    }

    public int getSkipped() {
        return skipped;
    }

    public int getUnknown() {
        return unknown;
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
