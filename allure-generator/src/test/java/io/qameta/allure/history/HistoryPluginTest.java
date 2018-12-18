package io.qameta.allure.history;

import io.qameta.allure.core.Configuration;
import io.qameta.allure.entity.Status;
import io.qameta.allure.entity.TestResult;
import io.qameta.allure.entity.Time;
import org.junit.Test;

import java.util.*;

import static io.qameta.allure.testdata.TestData.*;
import static org.mockito.Mockito.mock;

public class HistoryPluginTest {

  private static final String HISTORY_BLOCK_NAME = "history";

  @Test
  public void shouldHasNewFailedMark() throws Exception {
    final Configuration configuration = mock(Configuration.class);

    String historyId = UUID.randomUUID().toString();
    final Map<String, Object> extra = new HashMap<>();
    final Map<String, HistoryData> historyDataMap = createHistoryDataMap(
            historyId,
            createHistoryItem(Status.PASSED, 1, 2)
    );

    extra.put(HISTORY_BLOCK_NAME, historyDataMap);
    TestResult testResult = createTestResult(Status.FAILED, historyId, 100, 101);
    new HistoryPlugin().getData(Arrays.asList(
            createLaunchResults(extra, testResult)
    ));
    assert testResult.isNewFailed();
    assert !testResult.isFlaky();
  }

  @Test
  public void shouldHasFlakyMark() throws Exception {
    final Configuration configuration = mock(Configuration.class);

    String historyId = UUID.randomUUID().toString();
    final Map<String, Object> extra = new HashMap<>();
    final Map<String, HistoryData> historyDataMap = createHistoryDataMap(
            historyId,
            createHistoryItem(Status.PASSED, 3, 4),
            createHistoryItem(Status.FAILED, 1, 2)
    );

    extra.put(HISTORY_BLOCK_NAME, historyDataMap);
    TestResult testResult = createTestResult(Status.FAILED, historyId, 100, 101);
    new HistoryPlugin().getData(Arrays.asList(
            createLaunchResults(extra, testResult)
    ));
    assert testResult.isFlaky();
    assert testResult.isNewFailed();
  }

  private TestResult createTestResult(Status status, String historyId, long start, long stop) {
    return randomTestResult()
            .setHistoryId(historyId)
            .setStatus(status)
            .setTime(new Time().setStart(start).setStop(stop));
  }

  private Map<String, HistoryData> createHistoryDataMap(String historyId, HistoryItem... historyItems) {
    Map<String, HistoryData> historyDataMap = new HashMap<>();
    historyDataMap.put(historyId, new HistoryData().setItems(Arrays.asList(historyItems)));
    return historyDataMap;
  }

  private HistoryItem createHistoryItem(Status status, long start, long stop) {
    return new HistoryItem()
            .setStatus(status)
            .setTime(new Time().setStart(start).setStop(stop));
  }


}
