package io.qameta.allure;

import com.opencsv.bean.BeanField;
import com.opencsv.bean.ColumnPositionMappingStrategy;
import com.opencsv.bean.CsvBindByName;
import com.opencsv.bean.StatefulBeanToCsv;
import com.opencsv.bean.StatefulBeanToCsvBuilder;
import io.qameta.allure.core.Configuration;
import io.qameta.allure.core.LaunchResults;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Csv exporter extension. Can be used to process results in csv file
 *
 * @since 2.0
 */
public abstract class CommonCsvExportAggregator<T> implements Aggregator {

    private final String fileName;

    private final Class<T> type;

    public CommonCsvExportAggregator(final String fileName, final Class<T> type) {
        this.fileName = fileName;
        this.type = type;
    }

    @Override
    public void aggregate(final Configuration configuration,
                          final List<LaunchResults> launchesResults,
                          final Path outputDirectory) throws IOException {
        final Path dataFolder = Files.createDirectories(outputDirectory.resolve("data"));
        final Path csv = dataFolder.resolve(fileName);

        try (Writer writer = Files.newBufferedWriter(csv)) {
            StatefulBeanToCsvBuilder<T> builder = new StatefulBeanToCsvBuilder<>(writer);
            CsvMappingStrategy<T> mappingStrategy = new CsvMappingStrategy<>();
            mappingStrategy.setType(type);
            StatefulBeanToCsv<T> beanWriter = builder.withMappingStrategy(mappingStrategy).build();
            try {
                beanWriter.write(getData(launchesResults));
            } catch (Exception e) {
                throw new IOException(e);
            }
        }
    }

    protected abstract List<T> getData(final List<LaunchResults> launchesResults);

    private static class CsvMappingStrategy<T> extends ColumnPositionMappingStrategy<T> {

        @Override
        public String[] generateHeader() {
            final int numColumns = findMaxFieldIndex();
            if (!isAnnotationDriven() || numColumns == -1) {
                return super.generateHeader();
            }
            header = new String[numColumns + 1];
            for (int i = 0; i <= numColumns; i++) {
                header[i] = extractHeaderName(findField(i));
            }
            return header;
        }

        private String extractHeaderName(final BeanField beanField) {
            if (beanField == null
                    || beanField.getField() == null
                    || beanField.getField().getDeclaredAnnotationsByType(CsvBindByName.class).length == 0) {
                return StringUtils.EMPTY;
            }
            return beanField.getField().getDeclaredAnnotationsByType(CsvBindByName.class)[0].column();
        }
    }
}
