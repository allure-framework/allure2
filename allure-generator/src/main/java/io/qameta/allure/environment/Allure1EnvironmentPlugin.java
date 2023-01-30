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
package io.qameta.allure.environment;

import io.qameta.allure.CommonJsonAggregator;
import io.qameta.allure.core.LaunchResults;
import io.qameta.allure.entity.EnvironmentItem;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

import static io.qameta.allure.allure1.Allure1Plugin.ENVIRONMENT_BLOCK_NAME;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

/**
 * @author Egor Borisov ehborisov@gmail.com
 */
public class Allure1EnvironmentPlugin extends CommonJsonAggregator {

    public Allure1EnvironmentPlugin() {
        super("widgets", "environment.json");
    }

    @Override
    protected List<EnvironmentItem> getData(final List<LaunchResults> launches) {
        final List<Map.Entry<String, String>> launchEnvironments = launches.stream()
                .flatMap(launch -> launch.getExtra(ENVIRONMENT_BLOCK_NAME,
                        (Supplier<Map<String, String>>) LinkedHashMap::new).entrySet().stream())
                .collect(toList());

        return launchEnvironments.stream()
                .collect(groupingBy(Map.Entry::getKey, LinkedHashMap::new, mapping(Map.Entry::getValue, toSet())))
                .entrySet().stream()
                .map(Allure1EnvironmentPlugin::aggregateItem)
                .collect(toList());
    }

    private static EnvironmentItem aggregateItem(final Map.Entry<String, Set<String>> entry) {
        return new EnvironmentItem()
                .setName(entry.getKey())
                .setValues(new ArrayList<>(entry.getValue()));
    }
}
