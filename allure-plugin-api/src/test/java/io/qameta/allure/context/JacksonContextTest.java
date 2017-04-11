package io.qameta.allure.context;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class JacksonContextTest {

    @Test
    public void shouldCreateJacksonContext() throws Exception {
        final JacksonContext context = new JacksonContext();
        assertThat(context.getValue())
                .isNotNull();
    }
}