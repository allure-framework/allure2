package org.allurefw.report.executor;

import org.allurefw.report.entity.ExecutorInfo;

/**
 * @author charlie (Dmitry Baev).
 */
public class ExecutorWidgetItem {

    private String name;

    private ExecutorInfo info;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ExecutorInfo getInfo() {
        return info;
    }

    public void setInfo(ExecutorInfo info) {
        this.info = info;
    }
}
