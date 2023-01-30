/*
 *  Copyright 2016-2023 Qameta Software OÜ
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
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
