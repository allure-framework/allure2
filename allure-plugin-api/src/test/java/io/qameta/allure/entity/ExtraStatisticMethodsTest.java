package io.qameta.allure.entity;

import org.junit.Test;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author charlie (Dmitry Baev).
 */
public class ExtraStatisticMethodsTest {

    @Test
    public void shouldGetStatusForStatistic() throws Exception {
        final Statistic first = new Statistic().withFailed(2L).withPassed(1L);
        final Statistic second = new Statistic().withPassed(4L).withBroken(1L);
        final Statistic third = new Statistic().withPassed(1L).withSkipped(4L);
        assertThat(Arrays.asList(first, second, third))
                .extracting(ExtraStatisticMethods::getStatus)
                .containsExactly(
                        Status.FAILED, Status.BROKEN, Status.PASSED
                );
    }

    @Test
    public void shouldGetByStatus() throws Exception {
        final Statistic statistic = new Statistic().withFailed(2L).withPassed(1L).withBroken(4L);
        assertThat(Arrays.asList(Status.FAILED, Status.BROKEN, Status.PASSED, Status.UNKNOWN))
                .extracting(statistic::get)
                .containsExactly(2L, 4L, 1L, 0L);
    }
}