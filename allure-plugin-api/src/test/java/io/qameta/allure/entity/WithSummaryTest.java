/*
 *  Copyright 2016-2026 Qameta Software Inc
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package io.qameta.allure.entity;

import io.qameta.allure.Description;
import org.junit.jupiter.api.Test;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author charlie (Dmitry Baev).
 */
class WithSummaryTest {

    /**
     * Verifies recursive step counting for nested steps.
     * The test checks both direct and nested child steps are included in the total.
     */
    @Description
    @Test
    void shouldCountSteps() {
        final Step step = new Step().setSteps(
                asList(
                        new Step(),
                        new Step().setSteps(singletonList(new Step()))
                )
        );
        assertThat(step.getStepsCount())
                .isEqualTo(3L);
    }

    /**
     * Verifies recursive attachment counting for a step tree.
     * The test checks direct attachments and child-step attachments are included in the total.
     */
    @Description
    @Test
    void shouldCountAttachments() {
        final Step step = new Step().setSteps(
                asList(
                        new Step().setAttachments(asList(new Attachment(), new Attachment())),
                        new Step().setAttachments(singletonList(new Attachment())).setSteps(singletonList(new Step()))
                )
        ).setAttachments(singletonList(new Attachment()));
        assertThat(step.getAttachmentsCount())
                .isEqualTo(4L);
    }

    /**
     * Verifies an empty step is treated as having no displayable content.
     * The test checks a step without message, parameters, attachments, or children returns false.
     */
    @Description
    @Test
    void shouldCalculateHasContent() {
        final Step step = new Step();
        assertThat(step.hasContent())
                .isFalse();
    }

    /**
     * Verifies attachments make a step count as having content.
     * The test checks a step with one attachment returns true for content detection.
     */
    @Description
    @Test
    void shouldCountAttachmentsForHasContent() {
        final Step step = new Step().setAttachments(singletonList(new Attachment()));
        assertThat(step.hasContent())
                .isTrue();
    }

    /**
     * Verifies child steps make a step count as having content.
     * The test checks a step with one child returns true for content detection.
     */
    @Description
    @Test
    void shouldCountStepsForHasContent() {
        final Step step = new Step().setSteps(singletonList(new Step()));
        assertThat(step.hasContent())
                .isTrue();
    }

    /**
     * Verifies parameters make a step count as having content.
     * The test checks a step with one parameter returns true for content detection.
     */
    @Description
    @Test
    void shouldCountParametersForHasContent() {
        final Step step = new Step().setParameters(singletonList(new Parameter()));
        assertThat(step.hasContent())
                .isTrue();
    }

    /**
     * Verifies a status message makes a step count as having content.
     * The test checks a step with a message returns true for content detection.
     */
    @Description
    @Test
    void shouldCountMessageForHasContent() {
        final Step step = createStep("hey");
        assertThat(step.hasContent())
                .isTrue();
    }

    /**
     * Verifies a leaf step with a message should display that message.
     * The test checks message display is enabled when there are no child steps.
     */
    @Description
    @Test
    void shouldCalculateDisplayMessageFlagIfNoChildren() {
        final Step step = createStep("hey");

        assertThat(step.shouldDisplayMessage())
                .isTrue();
    }

    /**
     * Verifies a step without a message should not display message text.
     * The test checks message display is disabled when the message is absent.
     */
    @Description
    @Test
    void shouldCalculateDisplayMessageFlagIfNoMessage() {
        final Step step = new Step();

        assertThat(step.shouldDisplayMessage())
                .isFalse();
    }

    /**
     * Verifies parent message display is suppressed when a child repeats the same message.
     * The test checks duplicate message text is hidden at the parent level.
     */
    @Description
    @Test
    void shouldCalculateShouldMessageFlagIfChildHasTheSameMessage() {
        final Step step = createStep("hey")
                .setSteps(
                        asList(
                                createStep("hey"),
                                createStep("oy"),
                                new Step()
                        )
                );

        assertThat(step.shouldDisplayMessage())
                .isFalse();
    }

    /**
     * Verifies parent message display remains enabled when child messages are different.
     * The test checks no child duplicates the parent message, so the parent message is shown.
     */
    @Description
    @Test
    void shouldCalculateDisplayMessageFlagIfChildrenHasDifferentMessages() {
        final Step step = createStep("hey")
                .setSteps(
                        asList(
                                createStep("ay"),
                                createStep("oy"),
                                new Step()
                        )
                );

        assertThat(step.shouldDisplayMessage())
                .isTrue();
    }

    /**
     * Verifies duplicate message detection includes nested child steps.
     * The test checks a matching message in a grandchild suppresses display for the parent.
     */
    @Description
    @Test
    void shouldCalculateDisplayMessageFlagInSubChild() {
        final Step step = createStep("hey")
                .setSteps(
                        asList(
                                createStep("ay").setSteps(singletonList(createStep("hey"))),
                                createStep("oy"),
                                new Step()
                        )
                );

        assertThat(step.shouldDisplayMessage())
                .isFalse();
    }

    protected Step createStep(final String message) {
        return new Step().setStatusMessage(message);
    }
}
