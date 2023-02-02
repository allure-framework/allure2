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
 * @author Dmitry Baev baev@qameta.io
 * Date: 22.10.13
 * <p/>
 * Signals that an attempt to generate the reportData in specified directory has failed.
 */
public class ReportGenerationException extends RuntimeException {

    /**
     * Constructs the {@link ReportGenerationException} from given cause.
     *
     * @param cause given {@link java.lang.Throwable} cause
     */
    public ReportGenerationException(final Throwable cause) {
        super(cause);
    }

    /**
     * Constructs the {@link ReportGenerationException} from given cause.
     * and detail message.
     *
     * @param message the detail message.
     * @param cause   given {@link java.lang.Throwable} cause
     */
    public ReportGenerationException(final String message, final Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs the {@link ReportGenerationException} with specified detail message.
     *
     * @param message the detail message.
     */
    public ReportGenerationException(final String message) {
        super(message);
    }
}
