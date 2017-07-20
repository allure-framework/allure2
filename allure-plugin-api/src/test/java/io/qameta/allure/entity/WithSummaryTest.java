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

    @Test
    public void shouldCountMessageForHasContent() throws Exception {
        final Step step = createStep("hey");
        assertThat(step.hasContent())
                .isTrue();
    }

    @Test
    public void shouldCalculateDisplayMessageFlagIfNoChildren() throws Exception {
        final Step step = createStep("hey");

        assertThat(step.shouldDisplayMessage())
                .isTrue();
    }

    @Test
    public void shouldCalculateDisplayMessageFlagIfNoMessage() throws Exception {
        final Step step = new Step()
                .withStatusDetails(new StatusDetails());

        assertThat(step.shouldDisplayMessage())
                .isFalse();
    }

    @Test
    public void shouldCalculateShouldMessageFlagIfChildHasTheSameMessage() throws Exception {
        final Step step = createStep("hey")
                .withSteps(
                        createStep("hey"),
                        createStep("oy"),
                        new Step()
                );

        assertThat(step.shouldDisplayMessage())
                .isFalse();
    }

    @Test
    public void shouldCalculateDisplayMessageFlagIfChildrenHasDifferentMessages() throws Exception {
        final Step step = createStep("hey")
                .withSteps(
                        createStep("ay"),
                        createStep("oy"),
                        new Step()
                );

        assertThat(step.shouldDisplayMessage())
                .isTrue();
    }

    @Test
    public void shouldCalculateDisplayMessageFlagInSubChild() throws Exception {
        final Step step = createStep("hey")
                .withSteps(
                        createStep("ay").withSteps(createStep("hey")),
                        createStep("oy"),
                        new Step()
                );

        assertThat(step.shouldDisplayMessage())
                .isFalse();
    }

    protected Step createStep(final String message) {
        return new Step().withStatusDetails(new StatusDetails().withMessage(message));
    }
}