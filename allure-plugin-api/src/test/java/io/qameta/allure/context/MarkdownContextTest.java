package io.qameta.allure.context;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class MarkdownContextTest {

    @Test
    public void shouldCreateMarkdownContext() throws Exception {
        final MarkdownContext context = new MarkdownContext();
        assertThat(context.getValue())
                .isNotNull();
    }
}