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
package io.qameta.allure.core;

import io.qameta.allure.entity.Attachment;
import io.qameta.allure.entity.TestResult;

import java.nio.file.Path;

/**
 * Visitor that stores results data to internal storage.
 * Creates {@link LaunchResults} from all visited data.
 *
 * @since 2.0
 */
public interface ResultsVisitor {

    /**
     * Process attachment file. Returns {@link Attachment} that can be
     * used to get attachment in the report.
     *
     * @param attachmentFile the attachment file to process.
     * @return created {@link Attachment}.
     */
    Attachment visitAttachmentFile(Path attachmentFile);

    /**
     * Process test result.
     *
     * @param result the result to process.
     */
    void visitTestResult(TestResult result);

    /**
     * Visit extra block. You can access this block using {@link LaunchResults#getExtra(String)}.
     *
     * @param name   the name of block to add.
     * @param object the block to add.
     */
    void visitExtra(String name, Object object);

    /**
     * Notifies about error during results parse.
     *
     * @param message the error message.
     * @param e       exception. Should not be null. If no exception
     *                is present use {@link #error(String)} instead.
     */
    void error(String message, Exception e);

    /**
     * Notifies about error during results parse.
     *
     * @param message the error message.
     */
    void error(String message);

}
