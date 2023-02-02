/*
 *  Copyright 2016-2023 Qameta Software OÜ
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

import org.junit.jupiter.api.Test;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author charlie (Dmitry Baev).
 */
class WithSummaryTest {

    @Test
    void shouldCountSteps() {
        final Step step = new Step().setSteps(asList(
                new Step(),
                new Step().setSteps(singletonList(new Step()))
        ));
        assertThat(step.getStepsCount())
                .isEqualTo(3L);
    }

    @Test
    void shouldCountAttachments() {
        final Step step = new Step().setSteps(asList(
                new Step().setAttachments(asList(new Attachment(), new Attachment())),
                new Step().setAttachments(singletonList(new Attachment())).setSteps(singletonList(new Step()))
        )).setAttachments(singletonList(new Attachment()));
        assertThat(step.getAttachmentsCount())
                .isEqualTo(4L);
    }

    @Test
    void shouldCalculateHasContent() {
        final Step step = new Step();
        assertThat(step.hasContent())
                .isFalse();
    }

    @Test
    void shouldCountAttachmentsForHasContent() {
        final Step step = new Step().setAttachments(singletonList(new Attachment()));
        assertThat(step.hasContent())
                .isTrue();
    }

    @Test
    void shouldCountStepsForHasContent() {
        final Step step = new Step().setSteps(singletonList(new Step()));
        assertThat(step.hasContent())
                .isTrue();
    }

    @Test
    void shouldCountParametersForHasContent() {
        final Step step = new Step().setParameters(singletonList(new Parameter()));
        assertThat(step.hasContent())
                .isTrue();
    }

    @Test
    void shouldCountMessageForHasContent() {
        final Step step = createStep("hey");
        assertThat(step.hasContent())
                .isTrue();
    }

    @Test
    void shouldCalculateDisplayMessageFlagIfNoChildren() {
        final Step step = createStep("hey");

        assertThat(step.shouldDisplayMessage())
                .isTrue();
    }

    @Test
    void shouldCalculateDisplayMessageFlagIfNoMessage() {
        final Step step = new Step();

        assertThat(step.shouldDisplayMessage())
                .isFalse();
    }

    @Test
    void shouldCalculateShouldMessageFlagIfChildHasTheSameMessage() {
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
    void shouldCalculateDisplayMessageFlagIfChildrenHasDifferentMessages() {
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
    void shouldCalculateDisplayMessageFlagInSubChild() {
        final Step step = createStep("hey")
                .setSteps(asList(createStep("ay").setSteps(singletonList(createStep("hey"))),
                        createStep("oy"),
                        new Step())
                );

        assertThat(step.shouldDisplayMessage())
                .isFalse();
    }

    protected Step createStep(final String message) {
        return new Step().setStatusMessage(message);
    }
}
