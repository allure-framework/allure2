package org.allurefw.api;

/**
 * @author Dmitry Baev charlie@yandex-team.ru
 *         Date: 07.03.16
 */
public class ResultsReader {

    private final String include;

    private final String queueName;

    public ResultsReader(String include, String queueName) {
        this.include = include;
        this.queueName = queueName;
    }

    public String getInclude() {
        return include;
    }

    public String getQueueName() {
        return queueName;
    }
}
