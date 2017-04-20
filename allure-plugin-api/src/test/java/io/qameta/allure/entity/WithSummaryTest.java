package io.qameta.allure.entity;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author charlie (Dmitry Baev).
 */
public class WithSummaryTest {

    @Test
    public void shouldCountSteps() throws Exception {
        final Step step = new Step().withSteps(new Step(), new Step().withSteps(new Step()));
        assertThat(step.getStepsCount())
                .isEqualTo(3L);
    }

    @Test
    public void shouldCountAttachments() throws Exception {
        final Step step = new Step().withSteps(
                new Step().withAttachments(new Attachment(), new Attachment()),
                new Step().withAttachments(new Attachment()).withSteps(new Step())
        ).withAttachments(new Attachment());
        assertThat(step.getAttachmentsCount())
                .isEqualTo(4L);
    }

    @Test
    public void shouldCalculateHasContent() throws Exception {
        final Step step = new Step();
        assertThat(step.hasContent())
                .isFalse();
    }

    @Test
    public void shouldCountAttachmentsForHasContent() throws Exception {
        final Step step = new Step().withAttachments(new Attachment());
        assertThat(step.hasContent())
                .isTrue();
    }

    @Test
    public void shouldCountStepsForHasContent() throws Exception {
        final Step step = new Step().withSteps(new Step());
        assertThat(step.hasContent())
                .isTrue();
    }

    @Test
    public void shouldCountParametersForHasContent() throws Exception {
        final Step step = new Step().withParameters(new Parameter());
        assertThat(step.hasContent())
                .isTrue();
    }
}