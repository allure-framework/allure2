/*
 *  Copyright 2016-2024 Qameta Software Inc
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
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.json.JsonMapper;
import io.qameta.allure.ReportStorage;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author charlie (Dmitry Baev).
 */
public class InMemoryReportStorage implements ReportStorage {

    private final Map<String, String> reportDataFiles = new ConcurrentHashMap<>();
    private final JsonMapper mapper = JsonMapper.builder()
            .serializationInclusion(JsonInclude.Include.NON_NULL)
            .build();

    @Override
    public void addDataJson(final String name, final Object data) {
        try {
            final byte[] bytes = mapper.writeValueAsBytes(data);
            addDataBinary(name, bytes);
        } catch (JsonProcessingException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public void addDataBinary(final String name, final byte[] data) {
        reportDataFiles.put(
                name,
                Base64.getEncoder().encodeToString(data)
        );
    }

    @Override
    public void addDataFile(final String name, final Path file) {
        try {
            final byte[] bytes = Files.readAllBytes(file);
            addDataBinary(name, bytes);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public Map<String, String> getReportDataFiles() {
        return Collections.unmodifiableMap(reportDataFiles);
    }

}
