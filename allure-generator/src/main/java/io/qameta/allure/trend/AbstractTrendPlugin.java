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
package io.qameta.allure.trend;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.qameta.allure.Aggregator;
import io.qameta.allure.CompositeAggregator;
import io.qameta.allure.Constants;
import io.qameta.allure.Reader;
import io.qameta.allure.context.JacksonContext;
import io.qameta.allure.core.Configuration;
import io.qameta.allure.core.LaunchResults;
import io.qameta.allure.core.ResultsVisitor;
import io.qameta.allure.entity.ExecutorInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Spliterator;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.qameta.allure.executor.ExecutorPlugin.EXECUTORS_BLOCK_NAME;
import static java.util.Comparator.comparing;
import static java.util.Comparator.naturalOrder;
import static java.util.Comparator.nullsFirst;
import static java.util.Spliterators.spliteratorUnknownSize;
import static java.util.stream.StreamSupport.stream;

/**
 * Encapsulates common for all trend plugins logic.
 *
 * @param <T> Trend item type
 */
public abstract class AbstractTrendPlugin<T> extends CompositeAggregator implements Reader {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractTrendPlugin.class);

    private final String jsonFileName;
    private final String trendBlockName;

    protected AbstractTrendPlugin(final List<Aggregator> aggregators, final String jsonFileName,
            final String trendBlockName) {
        super(aggregators);
        this.jsonFileName = jsonFileName;
        this.trendBlockName = trendBlockName;
    }

    @Override
    public void readResults(final Configuration configuration,
                            final ResultsVisitor visitor,
                            final Path directory) {
        final JacksonContext context = configuration.requireContext(JacksonContext.class);
        final Path historyFile = directory.resolve(Constants.HISTORY_DIR).resolve(jsonFileName);

        if (Files.exists(historyFile)) {
            try (InputStream is = Files.newInputStream(historyFile)) {
                final ObjectMapper mapper = context.getValue();
                final JsonNode jsonNode = mapper.readTree(is);
                final List<T> history;
                if (jsonNode != null) {
                    history = getStream(jsonNode)
                            .map(child -> parseItem(historyFile, mapper, child))
                            .filter(Optional::isPresent)
                            .map(Optional::get)
                            .collect(Collectors.toList());
                } else {
                    history = Collections.emptyList();
                }

                visitor.visitExtra(trendBlockName, history);
            } catch (IOException e) {
                visitor.error("Could not read " + trendBlockName + " file " + historyFile, e);
            }
        }
    }

    private Stream<JsonNode> getStream(final JsonNode jsonNode) {
        return stream(
                spliteratorUnknownSize(jsonNode.elements(), Spliterator.ORDERED),
                false);
    }

    private Optional<T> parseItem(final Path historyFile, final ObjectMapper mapper, final JsonNode child) {
        try {
            return parseItem(mapper, child);
        } catch (JsonProcessingException e) {
            LOGGER.warn("Could not read {}", historyFile, e);
            return Optional.empty();
        }
    }

    protected abstract Optional<T> parseItem(ObjectMapper mapper, JsonNode child) throws JsonProcessingException;

    protected static Optional<ExecutorInfo> extractLatestExecutor(final List<LaunchResults> launches) {
        final Comparator<ExecutorInfo> comparator = comparing(ExecutorInfo::getBuildOrder, nullsFirst(naturalOrder()));
        return launches.stream()
                .map(launch -> launch.getExtra(EXECUTORS_BLOCK_NAME))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .filter(ExecutorInfo.class::isInstance)
                .map(ExecutorInfo.class::cast)
                .max(comparator);
    }
}
