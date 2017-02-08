package io.qameta.allure.history;

import io.qameta.allure.entity.TestCase;
import io.qameta.allure.entity.TestCaseResult;
import io.qameta.allure.entity.TestRun;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.Collections;
import java.util.Map;

import static io.qameta.allure.history.HistoryPlugin.HISTORY;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * @author charlie (Dmitry Baev).
 */
public class HistoryProcessorTest {

    @Test
    public void shouldAddTestCaseHistoryBlock() throws Exception {
        String id = "some-id";
        HistoryData data = new HistoryData().withId(id);
        Map<String, HistoryData> history = Collections.singletonMap(id, data);

        TestRun testRun = mock(TestRun.class);

        doReturn(history).when(testRun).getExtraBlock(HISTORY);
        doReturn(history).when(testRun).getExtraBlock(eq(HISTORY), any());

        TestCase testCase = mock(TestCase.class);
        TestCaseResult result = mock(TestCaseResult.class);
        doReturn(id).when(result).getTestCaseId();

        HistoryProcessor processor = new HistoryProcessor();
        processor.process(testRun, testCase, result);

        verify(result, times(1)).addExtraBlock(eq(HISTORY), any(HistoryData.class));
    }

    @Test
    public void shouldNotAddTestCaseHistoryBlockWhenNoSuchHistory() throws Exception {
        Map<String, HistoryData> history = Collections.emptyMap();

        TestRun testRun = mock(TestRun.class);
        doReturn(history).when(testRun).getExtraBlock(HISTORY);
        doReturn(history).when(testRun).getExtraBlock(eq(HISTORY), any());

        TestCase testCase = mock(TestCase.class);
        TestCaseResult result = mock(TestCaseResult.class);
        doReturn("some-id").when(result).getTestCaseId();

        HistoryProcessor processor = new HistoryProcessor();
        processor.process(testRun, testCase, result);

        verify(result, never()).addExtraBlock(eq(HISTORY), any(HistoryData.class));
    }

    @Test
    public void shouldNotAddTestCaseHistoryBlockWhenNotSuchTestCase() throws Exception {
        Map<String, HistoryData> history = Collections.emptyMap();

        TestRun testRun = mock(TestRun.class);
        doReturn(history).when(testRun).getExtraBlock(HISTORY);
        doReturn(history).when(testRun).getExtraBlock(eq(HISTORY), any());

        TestCase testCase = mock(TestCase.class);
        TestCaseResult result = mock(TestCaseResult.class);
        doReturn(null).when(result).getTestCaseId();

        HistoryProcessor processor = new HistoryProcessor();
        processor.process(testRun, testCase, result);

        verify(result, never()).addExtraBlock(eq(HISTORY), any(HistoryData.class));
    }
}