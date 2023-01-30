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
package io.qameta.allure.exception;

/**
 * Notified about missed context.
 *
 * @see io.qameta.allure.Context
 * @since 2.0
 */
public class ContextNotFoundException extends RuntimeException {

    /**
     * Creates an exception by given context type.
     *
     * @param contextType the type of context.
     */
    public ContextNotFoundException(final Class<?> contextType) {
        super(String.format("Required context not found: %s", contextType));
    }
}
