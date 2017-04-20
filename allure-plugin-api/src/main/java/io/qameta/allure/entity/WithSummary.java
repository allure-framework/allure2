package io.qameta.allure.entity;

import javax.xml.bind.annotation.XmlElement;
import java.util.List;

import static java.util.Collections.emptyList;
import static java.util.Objects.isNull;

/**
 * @author charlie (Dmitry Baev).
 */
public interface WithSummary {

    List<Step> getSteps();

    List<Attachment> getAttachments();

    List<Parameter> getParameters();

    @XmlElement
    default long getStepsCount() {
        final List<Step> steps = isNull(getSteps()) ? emptyList() : getSteps();
        final long stepsCount = steps.size();
        return steps.stream()
                .map(Step::getStepsCount)
                .reduce(stepsCount, Long::sum);
    }

    @XmlElement
    default long getAttachmentsCount() {
        final List<Attachment> attachments = isNull(getAttachments()) ? emptyList() : getAttachments();
        final List<Step> steps = isNull(getSteps()) ? emptyList() : getSteps();
        final long attachmentsCount = isNull(attachments) ? 0 : attachments.size();
        return steps.stream()
                .map(Step::getAttachmentsCount)
                .reduce(attachmentsCount, Long::sum);
    }

    @XmlElement
    default boolean hasContent() {
        final List<Attachment> attachments = isNull(getAttachments()) ? emptyList() : getAttachments();
        final List<Step> steps = isNull(getSteps()) ? emptyList() : getSteps();
        final List<Parameter> parameters = isNull(getParameters()) ? emptyList() : getParameters();
        return steps.size() + attachments.size() + parameters.size() > 0;
    }
}
