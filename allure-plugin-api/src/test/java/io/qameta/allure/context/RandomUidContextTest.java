package io.qameta.allure.context;

import org.junit.Test;

import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;

public class RandomUidContextTest {

    @Test
    public void shouldCreateRandomUidContext() throws Exception {
        final RandomUidContext context = new RandomUidContext();
        assertThat(context.getValue())
                .isNotNull();
    }

    @Test
    public void shouldGenerateRandomValues() throws Exception {
        final RandomUidContext context = new RandomUidContext();
        final Supplier<String> generator = context.getValue();

        final String first = generator.get();
        final String second = generator.get();
        assertThat(first)
                .isNotBlank()
                .isNotEqualTo(second);

        assertThat(second)
                .isNotBlank();
    }
}