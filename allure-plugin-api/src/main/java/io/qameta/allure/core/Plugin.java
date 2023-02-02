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

import io.qameta.allure.Extension;
import io.qameta.allure.PluginConfiguration;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

/**
 * Base plugin interface.
 *
 * @since 2.0
 */
public interface Plugin {

    PluginConfiguration getConfig();

    void unpackReportStatic(Path outputDirectory) throws IOException;

    List<Extension> getExtensions();

}
