/*
 *  Copyright 2016-2026 Qameta Software Inc
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

import io.qameta.allure.ReportStorage;

import java.nio.file.Path;

/**
 * Embeds report content while writing additional artifacts beside the HTML.
 * The storage method determines the destination independently of the file path.
 */
public class SingleFileReportStorage extends InMemoryReportStorage {

    private final ReportStorage additionalFiles;

    public SingleFileReportStorage(final Path outputDirectory) {
        this.additionalFiles = new FileSystemReportStorage(outputDirectory);
    }

    @Override
    public void addAdditionalJson(final String name, final Object data) {
        additionalFiles.addDataJson(name, data);
    }

    @Override
    public void addAdditionalBinary(final String name, final byte[] data) {
        additionalFiles.addDataBinary(name, data);
    }

    @Override
    public void addAdditionalFile(final String name, final Path file) {
        additionalFiles.addDataFile(name, file);
    }
}
