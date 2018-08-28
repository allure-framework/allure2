package io.qameta.allure.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import static java.util.Comparator.comparing;
import static java.util.Comparator.naturalOrder;
import static java.util.Comparator.nullsFirst;

/**
 * @author charlie (Dmitry Baev).
 */
@Data
@Accessors(chain = true)
@SuppressWarnings("PMD.TooManyFields")
public class TestResult implements Serializable, Nameable, Parameterizable, Statusable, Timeable {

    private static final long serialVersionUID = 1L;

    protected String uid;
    protected String name;
    protected String fullName;
    protected String historyId;
    protected String testId;
    protected Time time = new Time();
    protected String description;
    protected String descriptionHtml;
    protected Status status;
    protected String statusMessage;
    protected String statusTrace;

    protected boolean flaky;

    //    Execution
    protected List<StageResult> beforeStages = new ArrayList<>();
    protected StageResult testStage;
    protected List<StageResult> afterStages = new ArrayList<>();

    //    Meta
    protected List<Label> labels = new ArrayList<>();
    protected List<Parameter> parameters = new ArrayList<>();
    protected List<Link> links = new ArrayList<>();
    protected boolean hidden;
    protected boolean retry;
    protected final Map<String, Object> extra = new HashMap<>();

    @JsonProperty
    public String getSource() {
        return getUid() + ".json";
    }

    public void addExtraBlock(final String blockName, final Object block) {
        extra.put(blockName, block);
    }

    @SuppressWarnings("unchecked")
    public <T> T getExtraBlock(final String blockName, final T defaultValue) {
        return (T) extra.computeIfAbsent(blockName, name -> defaultValue);
    }

    @SuppressWarnings("unchecked")
    public <T> T getExtraBlock(final String blockName) {
        return (T) extra.get(blockName);
    }

    public boolean hasExtraBlock(final String blockName) {
        return extra.containsKey(blockName);
    }

    public <T> T findAllLabels(final LabelName name, final Collector<String, ?, T> collector) {
        return findAllLabels(name.value(), collector);
    }

    public <T> T findAllLabels(final String name, final Collector<String, ?, T> collector) {
        return getLabels().stream()
                .filter(label -> name.equals(label.getName()))
                .map(Label::getValue)
                .collect(collector);
    }

    public List<String> findAllLabels(final LabelName name) {
        return findAllLabels(name, Collectors.toList());
    }

    public List<String> findAllLabels(final String name) {
        return findAllLabels(name, Collectors.toList());
    }

    public Optional<String> findOneLabel(final LabelName name) {
        return findOneLabel(name.value());
    }

    public Optional<String> findOneLabel(final String name) {
        return getLabels().stream()
                .filter(label -> name.equals(label.getName()))
                .findAny()
                .map(Label::getValue);
    }

    public void addLabelIfNotExists(final LabelName name, final String value) {
        addLabelIfNotExists(name.value(), value);
    }

    public void addLabelIfNotExists(final String name, final String value) {
        if (value == null || name == null) {
            return;
        }
        final Optional<String> any = getLabels().stream()
                .map(Label::getName)
                .filter(name::equals)
                .findAny();
        if (!any.isPresent()) {
            addLabel(name, value);
        }
    }

    public void addLabel(final String name, final String value) {
        getLabels().add(new Label().setName(name).setValue(value));
    }

    public static Comparator<TestResult> comparingByTime() {
        return comparingByTimeAsc().reversed();
    }

    public static Comparator<TestResult> comparingByTimeAsc() {
        return comparing(
                TestResult::getTime,
                nullsFirst(comparing(Time::getStart, nullsFirst(naturalOrder())))
        );
    }
}
