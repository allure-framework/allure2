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
        final Statistic first = new Statistic().setFailed(2L).setPassed(1L);
        final Statistic second = new Statistic().setPassed(4L).setBroken(1L);
        final Statistic third = new Statistic().setPassed(1L).setSkipped(4L);
        assertThat(Arrays.asList(first, second, third))
                .extracting(Statistic::getStatus)
                .containsExactly(
                        Status.FAILED, Status.BROKEN, Status.PASSED
                );
    }

    @Test
    public void shouldGetByStatus() throws Exception {
        final Statistic statistic = new Statistic().setFailed(2L).setPassed(1L).setBroken(4L);
        assertThat(Arrays.asList(Status.FAILED, Status.BROKEN, Status.PASSED, Status.UNKNOWN))
                .extracting(statistic::get)
                .containsExactly(2L, 4L, 1L, 0L);
    }
}