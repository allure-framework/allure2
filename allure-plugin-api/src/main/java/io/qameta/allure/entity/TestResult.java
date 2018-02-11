package io.qameta.allure.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author charlie (Dmitry Baev).
 */
@Data
@Accessors(chain = true)
@SuppressWarnings("PMD.TooManyFields")
public class TestResult implements Serializable, Nameable, Statusable, Timeable {

    private static final long serialVersionUID = 1L;

    protected Long id;
    protected String name;
    protected String fullName;
    protected String historyKey;
    protected String testId;

    protected Long start;
    protected Long stop;
    protected Long duration;

    protected String description;
    protected String descriptionHtml;

    protected TestStatus status;
    protected String message;
    protected String trace;

    protected boolean flaky;

    protected TestResultType type = TestResultType.TEST;

    //    Markers
    protected Set<EnvironmentVariable> environmentVariables = new HashSet<>();
    protected Set<CustomField> customFields = new HashSet<>();
    protected Set<TestTag> tags = new HashSet<>();

    //    Meta
    protected Set<TestLabel> labels = new HashSet<>();
    protected Set<TestParameter> parameters = new HashSet<>();
    protected Set<TestLink> links = new HashSet<>();
    protected boolean hidden;
    protected boolean retry;
    protected final Map<String, Object> extra = new HashMap<>();

    public boolean isNotHidden() {
        return !isHidden();
    }

    public boolean isTest() {
        return TestResultType.TEST.equals(getType());
    }

    @JsonProperty
    public String getSource() {
        return getId() + ".json";
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

    public Stream<TestLabel> findAllLabelsStream(final String name) {
        final Set<TestLabel> labels = getLabels();
        final Stream<TestLabel> stream = Objects.isNull(labels) ? Stream.empty() : labels.stream();
        return stream
                .filter(label -> name.equals(label.getName()));
    }

    public Stream<String> findAllLabelValuesStream(final String name) {
        return findAllLabelsStream(name)
                .map(TestLabel::getValue);
    }

    public <T> T findAllLabelValues(final String name, final Collector<String, ?, T> collector) {
        return findAllLabelValuesStream(name).collect(collector);
    }

    public List<String> findAllLabelValues(final String name) {
        return findAllLabelValues(name, Collectors.toList());
    }

    public <T> T findAllLabels(final LabelName name, final Collector<String, ?, T> collector) {
        return findAllLabels(name.value(), collector);
    }

    public <T> T findAllLabels(final String name, final Collector<String, ?, T> collector) {
        return getLabels().stream()
                .filter(label -> name.equals(label.getName()))
                .map(TestLabel::getValue)
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
                .map(TestLabel::getValue);
    }

    public void addLabelIfNotExists(final LabelName name, final String value) {
        addLabelIfNotExists(name.value(), value);
    }

    public void addLabelIfNotExists(final String name, final String value) {
        if (value == null || name == null) {
            return;
        }
        Optional<String> any = getLabels().stream()
                .map(TestLabel::getName)
                .filter(name::equals)
                .findAny();
        if (!any.isPresent()) {
            addLabel(name, value);
        }
    }

    public void addLabel(final String name, final String value) {
        getLabels().add(new TestLabel().setName(name).setValue(value));
    }
}
