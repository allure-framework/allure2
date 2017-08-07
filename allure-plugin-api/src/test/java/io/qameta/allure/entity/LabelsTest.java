package io.qameta.allure.entity;

import org.junit.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author charlie (Dmitry Baev).
 */
public class LabelsTest {

    @Test
    public void shouldFindLabelsInEmptyArray() throws Exception {
        final Optional<String> found = new TestResult().findOneLabel("hey");
        assertThat(found)
                .isEmpty();
    }

    @Test
    public void shouldFindOneWithNullValue() throws Exception {
        final TestResult result = new TestResult();
        result.getLabels().add(new Label().setName("hey").setValue(null));
        final Optional<String> found = result.findOneLabel("hey");
        assertThat(found)
                .isEmpty();
    }

    @Test
    public void shouldFindAllWithNullValue() throws Exception {
        final TestResult result = new TestResult();
        result.getLabels().add(new Label().setName("hey").setValue(null));
        result.getLabels().add(new Label().setName("hey").setValue("a"));
        result.getLabels().add(new Label().setName("hey").setValue("b"));
        final List<String> found = result.findAllLabels("hey");
        assertThat(found)
                .containsExactlyInAnyOrder(null, "a", "b");
    }
}
