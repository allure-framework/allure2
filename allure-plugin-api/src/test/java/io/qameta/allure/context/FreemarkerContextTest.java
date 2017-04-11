package io.qameta.allure.context;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class FreemarkerContextTest {

    @Test
    public void shouldCreateFreemarkerContext() throws Exception {
        final FreemarkerContext context = new FreemarkerContext();
        assertThat(context.getValue())
                .isNotNull();
    }
}