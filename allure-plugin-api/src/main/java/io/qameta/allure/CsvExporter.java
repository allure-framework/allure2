package io.qameta.allure;

import com.opencsv.bean.StatefulBeanToCsv;
import com.opencsv.bean.StatefulBeanToCsvBuilder;
import io.qameta.allure.core.LaunchResults;
import io.qameta.allure.util.CsvMappingStrategy;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Path;
import java.util.List;

/**
 * Csv exporter extension. Can be used to process results in csv file
 *
 * @since 2.0
 */
public abstract class CsvExporter<T> implements Extension {

    public void createCsvExportFile(List<LaunchResults> launchesResults, Path dataFolder, String fileName, Class<T> type) throws IOException {
        final Path csv = dataFolder.resolve(fileName);
        try(Writer writer = new FileWriter(csv.toFile())) {
            StatefulBeanToCsvBuilder<T> builder = new StatefulBeanToCsvBuilder<>(writer);
            CsvMappingStrategy<T> mappingStrategy = new CsvMappingStrategy<>();
            mappingStrategy.setType(type);
            StatefulBeanToCsv<T> beanWriter = builder.withMappingStrategy(mappingStrategy).build();
            try {
                beanWriter.write(getCollectionToCsvExport(launchesResults));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public abstract List<T> getCollectionToCsvExport(List<LaunchResults> launchesResults);
}
