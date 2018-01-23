package io.qameta.allure.history;

import java.util.List;

/**
 * @author charlie (Dmitry Baev).
 */
public interface HistoryStorage {

    <T> List<T> getHistory(String historyKey, Class<T> type);

}
