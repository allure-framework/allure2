package org.allurefw.report.summary;

import org.allurefw.report.entity.Status;
import org.allurefw.report.entity.TestCase;
import org.allurefw.report.entity.TestCaseResult;
import org.allurefw.report.entity.TestRun;
import org.junit.Test;

import java.util.function.Supplier;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isEmptyOrNullString;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;

/**
 * @author charlie (Dmitry Baev).
 */
public class SummaryAggregatorTest {

    @Test
    public void shouldSupply() throws Exception {
        SummaryAggregator aggregator = new SummaryAggregator();
        Supplier<SummaryData> supplier = aggregator.supplier(null, null);
        assertThat(supplier, notNullValue());
        SummaryData data = supplier.get();
        assertThat(data, notNullValue());
        assertThat(data.getTestRuns(), empty());
        assertThat(data.getReportName(), isEmptyOrNullString());
    }

    @Test
    public void shouldAggregate() throws Exception {
        SummaryAggregator aggregator = new SummaryAggregator();
        SummaryData data = new SummaryData();
        TestRun testRun = new TestRun().withUid("a").withName("name");
        TestCase testCase = new TestCase();
        TestCaseResult result = new TestCaseResult()
                .withStatus(Status.FAILED);
        result.setTime(4, 10);
        aggregator.aggregate(testRun, testCase, result).accept(data);
        assertThat(data, notNullValue());
        assertThat(data.getReportName(), is("name"));
        assertThat(data.getTestRuns(), hasSize(1));
        assertThat(data.getTestRuns(), hasItem("a"));
        assertThat(data.getStatistic(), notNullValue());
        assertThat(data.getStatistic(), hasProperty("failed", equalTo(1L)));
        assertThat(data.getStatistic().getTotal(), is(1L));
        assertThat(data.getTime(), allOf(
                hasProperty("start", equalTo(4L)),
                hasProperty("stop", equalTo(10L)),
                hasProperty("duration", equalTo(6L)),
                hasProperty("maxDuration", equalTo(6L)),
                hasProperty("sumDuration", equalTo(6L))
        ));

    }
}