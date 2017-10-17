package io.qameta.allure.csv;

import com.opencsv.bean.CsvBindByName;
import com.opencsv.bean.CsvBindByPosition;
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
    private String failed;

    @CsvBindByName(column = "BROKEN")
    @CsvBindByPosition(position = 2)
    private String broken;

    @CsvBindByName(column = "PASSED")
    @CsvBindByPosition(position = 3)
    private String passed;

    @CsvBindByName(column = "SKIPPED")
    @CsvBindByPosition(position = 4)
    private String skipped;

    @CsvBindByName(column = "UNKNOWN")
    @CsvBindByPosition(position = 5)
    private String unknown;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFailed() {
        return failed;
    }

    public void setFailed(String failed) {
        this.failed = failed;
    }

    public String getBroken() {
        return broken;
    }

    public void setBroken(String broken) {
        this.broken = broken;
    }

    public String getPassed() {
        return passed;
    }

    public void setPassed(String passed) {
        this.passed = passed;
    }

    public String getSkipped() {
        return skipped;
    }

    public void setSkipped(String skipped) {
        this.skipped = skipped;
    }

    public String getUnknown() {
        return unknown;
    }

    public void setUnknown(String unknown) {
        this.unknown = unknown;
    }
}
