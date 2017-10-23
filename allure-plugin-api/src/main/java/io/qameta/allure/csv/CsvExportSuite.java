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
    private final String status;

    @CsvBindByName(column = "Name")
    @CsvBindByPosition(position = 1)
    private final String name;

    @CsvBindByName(column = "Duration in ms")
    @CsvBindByPosition(position = 2)
    private final String duration;

    @CsvBindByName(column = "Description")
    @CsvBindByPosition(position = 3)
    private final String message;

    public CsvExportSuite(final TestResult result) {
        this.status = result.getStatus() != null ? result.getStatus().value() : null;
        this.name = result.getFullName();
        this.duration = result.getTime().getDuration() != null ? result.getTime().getDuration().toString() : null;
        this.message = result.getDescription();
    }

    public String getMessage() {
        return message;
    }

    public String getDuration() {
        return duration;
    }

    public String getName() {
        return name;
    }

    public String getStatus() {
        return status;
    }
}
