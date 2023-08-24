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

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.json.JsonMapper;
import io.qameta.allure.ReportGenerationException;
import io.qameta.allure.ReportStorage;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

/**
 * @author charlie (Dmitry Baev).
 */
public class FileSystemReportStorage implements ReportStorage {

    private final JsonMapper mapper = JsonMapper.builder()
            .serializationInclusion(JsonInclude.Include.NON_NULL)
            .build();

    private final Path dataDirectory;

    public FileSystemReportStorage(final Path reportDirectory) {
        this.dataDirectory = reportDirectory;
    }

    @Override
    public void addDataJson(final String name, final Object data) {
        final Path target = getPath(name);
        try (OutputStream os = Files.newOutputStream(target)) {
            mapper.writeValue(os, data);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public void addDataBinary(final String name, final byte[] data) {
        final Path target = getPath(name);
        try {
            Files.write(target, data);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public void addDataFile(final String name, final Path file) {
        final Path target = getPath(name);
        try {
            Files.copy(file, target, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private Path getPath(final String name) {
        final Path normalized = checkPath(name);
        final Path target = dataDirectory.resolve(normalized);
        createDirectories(target.getParent());
        return target;
    }

    private static void createDirectories(final Path target) {
        try {
            Files.createDirectories(target);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static Path checkPath(final String name) {
        final Path resource = Paths.get(name);
        if (resource.isAbsolute()) {
            throw new ReportGenerationException("absolute resource names are forbidden");
        }
        final Path normalized = resource.normalize();
        if (!normalized.equals(resource)) {
            throw new ReportGenerationException("only normalized resource names are supported");
        }
        return normalized;
    }
}
