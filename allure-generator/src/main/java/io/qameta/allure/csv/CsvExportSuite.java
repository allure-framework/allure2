package io.qameta.allure.csv;

import com.opencsv.bean.CsvBindByName;
import com.opencsv.bean.CsvBindByPosition;
import io.qameta.allure.entity.TestResult;

import java.io.Serializable;

/**
 * Class contains the information for the suites csv export.
 *
 */
public class CsvExportSuite implements Serializable {

    @CsvBindByName(column = "Status")
    @CsvBindByPosition(position = 0)
    private String status;

    @CsvBindByName(column = "Name")
    @CsvBindByPosition(position = 1)
    private String name;

    @CsvBindByName(column = "Duration in ms")
    @CsvBindByPosition(position = 2)
    private String duration;

    @CsvBindByName(column = "Description")
    @CsvBindByPosition(position = 3)
    private String message;

    public CsvExportSuite(final TestResult result) {
        this.status = result.getStatus() != null ? result.getStatus().value() : null;
        this.name = result.getFullName();
        this.duration = result.getTime() != null ? result.getTime().getDuration().toString() : null;
        this.message = result.getDescription();
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(final String message) {
        this.message = message;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(final String duration) {
        this.duration = duration;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(final String status) {
        this.status = status;
    }
}
