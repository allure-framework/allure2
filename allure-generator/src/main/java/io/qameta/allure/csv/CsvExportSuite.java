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

    public CsvExportSuite(TestResult result) {
        this.status = result.getStatus().value();
        this.name = result.getFullName();
        this.duration = "" + result.getTime().getDuration();
        this.message = result.getDescription();
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
