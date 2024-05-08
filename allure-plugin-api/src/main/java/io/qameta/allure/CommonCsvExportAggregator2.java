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
package io.qameta.allure;

import com.opencsv.bean.HeaderColumnNameMappingStrategy;
import com.opencsv.bean.HeaderColumnNameMappingStrategyBuilder;
import com.opencsv.bean.StatefulBeanToCsv;
import com.opencsv.bean.StatefulBeanToCsvBuilder;
import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;
import io.qameta.allure.core.Configuration;
import io.qameta.allure.core.LaunchResults;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Csv exporter extension. Can be used to process results in csv file
 *
 * @param <T> type of result bean.
 * @since 2.0
 */
public abstract class CommonCsvExportAggregator2<T> implements Aggregator2 {

    private final String fileName;

    private final Class<T> type;

    public CommonCsvExportAggregator2(final String fileName, final Class<T> type) {
        this.fileName = fileName;
        this.type = type;
    }

    @SuppressWarnings("PMD.ExceptionAsFlowControl")
    @Override
    public void aggregate(final Configuration configuration,
                          final List<LaunchResults> launchesResults,
                          final ReportStorage storage) {
        final ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (Writer writer = new OutputStreamWriter(bos, StandardCharsets.UTF_8)) {
            final StatefulBeanToCsvBuilder<T> builder = new StatefulBeanToCsvBuilder<>(writer);
            final HeaderColumnNameMappingStrategy<T> mappingStrategy =
                    new HeaderColumnNameMappingStrategyBuilder<T>().build();
            mappingStrategy.setType(type);
            final StatefulBeanToCsv<T> beanWriter = builder.withMappingStrategy(mappingStrategy).build();
            try {
                beanWriter.write(getData(launchesResults));
            } catch (CsvRequiredFieldEmptyException | CsvDataTypeMismatchException e) {
                throw new IOException(e);
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        storage.addDataBinary(
                Constants.dataPath(fileName),
                bos.toByteArray()
        );
    }

    protected abstract List<T> getData(List<LaunchResults> launchesResults);
}
