package io.qameta.allure;

/**
 * Report context. Can be added via plugins and used from
 * report configuration.
 *
 * <code>
 * JacksonContext context = configuration.requireContext(JacksonContext.class)
 * ObjectMapper mapper = context.getValue();
 * </code>
 *
 * @param <T> the type of context value
 * @see io.qameta.allure.context.JacksonContext
 * @see io.qameta.allure.context.MarkdownContext
 * @see io.qameta.allure.context.FreemarkerContext
 * @see io.qameta.allure.context.RandomUidContext
 * @since 2.0
 */
@FunctionalInterface
public interface Context<T> extends Extension {

    /**
     * Returns the context value.
     *
     * @return the context value.
     */
    T getValue();

}
