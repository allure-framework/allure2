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

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.json.JsonMapper;
import io.qameta.allure.ReportGenerationException;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;

final class StaticAssetManifest {

    private static final JsonMapper JSON_MAPPER = JsonMapper.builder().build();
    private static final TypeReference<List<String>> STATIC_ASSET_MANIFEST_TYPE = new TypeReference<List<String>>() {
    };

    private StaticAssetManifest() {
    }

    static List<String> load(final String resourceName) {
        final URL resource = Thread.currentThread()
                .getContextClassLoader()
                .getResource(resourceName);
        if (resource == null) {
            throw new ReportGenerationException("Static asset manifest " + resourceName + " not found");
        }

        try (InputStream input = resource.openStream()) {
            return JSON_MAPPER.readValue(input, STATIC_ASSET_MANIFEST_TYPE);
        } catch (IOException e) {
            throw new ReportGenerationException("Can't read static asset manifest " + resourceName, e);
        }
    }

}
