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
        final TestResultStep step = new TestResultStep().setSteps(asList(
                new TestResultStep(),
                new TestResultStep().setSteps(singletonList(new TestResultStep()))
        ));
        assertThat(step.getStepsCount())
                .isEqualTo(3L);
    }

    @Test
    public void shouldCountAttachments() throws Exception {
        final TestResultStep step = new TestResultStep().setSteps(asList(
                new TestResultStep().setAttachments(asList(new Attachment(), new Attachment())),
                new TestResultStep().setAttachments(singletonList(new Attachment())).setSteps(singletonList(new TestResultStep()))
        )).setAttachments(singletonList(new Attachment()));
        assertThat(step.getAttachmentsCount())
                .isEqualTo(4L);
    }

    @Test
    public void shouldCalculateHasContent() throws Exception {
        final TestResultStep step = new TestResultStep();
        assertThat(step.hasContent())
                .isFalse();
    }

    @Test
    public void shouldCountAttachmentsForHasContent() throws Exception {
        final TestResultStep step = new TestResultStep().setAttachments(singletonList(new Attachment()));
        assertThat(step.hasContent())
                .isTrue();
    }

    @Test
    public void shouldCountStepsForHasContent() throws Exception {
        final TestResultStep step = new TestResultStep().setSteps(singletonList(new TestResultStep()));
        assertThat(step.hasContent())
                .isTrue();
    }

    @Test
    public void shouldCountParametersForHasContent() throws Exception {
        final TestResultStep step = new TestResultStep().setParameters(singletonList(new TestParameter()));
        assertThat(step.hasContent())
                .isTrue();
    }

    @Test
    public void shouldCountMessageForHasContent() throws Exception {
        final TestResultStep step = createStep("hey");
        assertThat(step.hasContent())
                .isTrue();
    }

    @Test
    public void shouldCalculateDisplayMessageFlagIfNoChildren() throws Exception {
        final TestResultStep step = createStep("hey");

        assertThat(step.shouldDisplayMessage())
                .isTrue();
    }

    @Test
    public void shouldCalculateDisplayMessageFlagIfNoMessage() throws Exception {
        final TestResultStep step = new TestResultStep();

        assertThat(step.shouldDisplayMessage())
                .isFalse();
    }

    @Test
    public void shouldCalculateShouldMessageFlagIfChildHasTheSameMessage() throws Exception {
        final TestResultStep step = createStep("hey")
                .setSteps(asList(
                        createStep("hey"),
                        createStep("oy"),
                        new TestResultStep()
                ));

        assertThat(step.shouldDisplayMessage())
                .isFalse();
    }

    @Test
    public void shouldCalculateDisplayMessageFlagIfChildrenHasDifferentMessages() throws Exception {
        final TestResultStep step = createStep("hey")
                .setSteps(asList(
                        createStep("ay"),
                        createStep("oy"),
                        new TestResultStep())
                );

        assertThat(step.shouldDisplayMessage())
                .isTrue();
    }

    @Test
    public void shouldCalculateDisplayMessageFlagInSubChild() throws Exception {
        final TestResultStep step = createStep("hey")
                .setSteps(asList(createStep("ay").setSteps(singletonList(createStep("hey"))),
                        createStep("oy"),
                        new TestResultStep())
                );

        assertThat(step.shouldDisplayMessage())
                .isFalse();
    }

    protected TestResultStep createStep(final String message) {
        return new TestResultStep().setMessage(message);
    }
}