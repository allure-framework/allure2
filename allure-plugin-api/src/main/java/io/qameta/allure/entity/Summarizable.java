package io.qameta.allure.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static java.util.Collections.emptyList;
import static java.util.Objects.isNull;

/**
 * @author charlie (Dmitry Baev).
 */
public interface Summarizable {

    StatusDetails getStatusDetails();

    List<Step> getSteps();

    List<Attachment> getAttachments();

    List<Parameter> getParameters();

    @JsonProperty
    default long getStepsCount() {
        final List<Step> steps = isNull(getSteps()) ? emptyList() : getSteps();
        final long stepsCount = steps.size();
        return steps.stream()
                .map(Step::getStepsCount)
                .reduce(stepsCount, Long::sum);
    }

    @JsonProperty
    default long getAttachmentsCount() {
        final List<Attachment> attachments = isNull(getAttachments()) ? emptyList() : getAttachments();
        final List<Step> steps = isNull(getSteps()) ? emptyList() : getSteps();
        final long attachmentsCount = isNull(attachments) ? 0 : attachments.size();
        return steps.stream()
                .map(Step::getAttachmentsCount)
                .reduce(attachmentsCount, Long::sum);
    }

    @JsonProperty
    default boolean shouldDisplayMessage() {
        final Optional<String> message = Optional.ofNullable(getStatusDetails())
                .map(StatusDetails::getMessage);
        return message.isPresent() && getSteps().stream()
                .noneMatch(step -> step.hasMessage(message.get()));
    }

    default boolean hasMessage(String message) {
        final Optional<String> current = Optional.ofNullable(getStatusDetails())
                .map(StatusDetails::getMessage)
                .filter(s -> Objects.equals(s, message));
        return current.isPresent() || getSteps().stream()
                .anyMatch(step -> step.hasMessage(message));
    }

    @JsonProperty
    default boolean hasContent() {
        final List<Attachment> attachments = isNull(getAttachments()) ? emptyList() : getAttachments();
        final List<Step> steps = isNull(getSteps()) ? emptyList() : getSteps();
        final List<Parameter> parameters = isNull(getParameters()) ? emptyList() : getParameters();
        return steps.size() + attachments.size() + parameters.size() > 0 || shouldDisplayMessage();
    }
}
