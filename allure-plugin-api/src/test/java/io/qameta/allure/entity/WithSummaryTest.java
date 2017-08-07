package io.qameta.allure.entity;

import org.junit.Test;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author charlie (Dmitry Baev).
 */
public class WithSummaryTest {

    @Test
    public void shouldCountSteps() throws Exception {
        final Step step = new Step().setSteps(asList(
                new Step(),
                new Step().setSteps(singletonList(new Step()))
        ));
        assertThat(step.getStepsCount())
                .isEqualTo(3L);
    }

    @Test
    public void shouldCountAttachments() throws Exception {
        final Step step = new Step().setSteps(asList(
                new Step().setAttachments(asList(new Attachment(), new Attachment())),
                new Step().setAttachments(singletonList(new Attachment())).setSteps(singletonList(new Step()))
        )).setAttachments(singletonList(new Attachment()));
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
        final Step step = new Step().setAttachments(singletonList(new Attachment()));
        assertThat(step.hasContent())
                .isTrue();
    }

    @Test
    public void shouldCountStepsForHasContent() throws Exception {
        final Step step = new Step().setSteps(singletonList(new Step()));
        assertThat(step.hasContent())
                .isTrue();
    }

    @Test
    public void shouldCountParametersForHasContent() throws Exception {
        final Step step = new Step().setParameters(singletonList(new Parameter()));
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
                .setStatusDetails(new StatusDetails());

        assertThat(step.shouldDisplayMessage())
                .isFalse();
    }

    @Test
    public void shouldCalculateShouldMessageFlagIfChildHasTheSameMessage() throws Exception {
        final Step step = createStep("hey")
                .setSteps(asList(
                        createStep("hey"),
                        createStep("oy"),
                        new Step()
                ));

        assertThat(step.shouldDisplayMessage())
                .isFalse();
    }

    @Test
    public void shouldCalculateDisplayMessageFlagIfChildrenHasDifferentMessages() throws Exception {
        final Step step = createStep("hey")
                .setSteps(asList(
                        createStep("ay"),
                        createStep("oy"),
                        new Step())
                );

        assertThat(step.shouldDisplayMessage())
                .isTrue();
    }

    @Test
    public void shouldCalculateDisplayMessageFlagInSubChild() throws Exception {
        final Step step = createStep("hey")
                .setSteps(asList(createStep("ay").setSteps(singletonList(createStep("hey"))),
                        createStep("oy"),
                        new Step())
                );

        assertThat(step.shouldDisplayMessage())
                .isFalse();
    }

    protected Step createStep(final String message) {
        return new Step().setStatusDetails(new StatusDetails().setMessage(message));
    }
}