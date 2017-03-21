package io.qameta.allure.executor;

import io.qameta.allure.entity.ExecutorInfo;

/**
 * @author charlie (Dmitry Baev).
 */
public class ExecutorWidgetItem {

    private String name;

    private ExecutorInfo info;

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public ExecutorInfo getInfo() {
        return info;
    }

    public void setInfo(final ExecutorInfo info) {
        this.info = info;
    }
}
