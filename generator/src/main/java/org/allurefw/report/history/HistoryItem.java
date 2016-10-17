package org.allurefw.report.history;

import org.allurefw.report.entity.Status;

/**
 * @author charlie (Dmitry Baev).
 */
public class HistoryItem {

    private Status status;

    private String statusDetails;

    private long timestamp;

    public HistoryItem(Status status, String statusDetails, long timestamp) {
        this.status = status;
        this.statusDetails = statusDetails;
        this.timestamp = timestamp;
    }

    public Status getStatus() {
        return status;
    }

    public String getStatusDetails() {
        return statusDetails;
    }

    public long getTimestamp() {
        return timestamp;
    }
}
