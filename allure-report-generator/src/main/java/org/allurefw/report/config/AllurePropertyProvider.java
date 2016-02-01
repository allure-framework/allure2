package org.allurefw.report.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.qatools.properties.providers.SystemPropertyProvider;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

import static ru.yandex.qatools.allure.AllureConstants.REPORT_CONFIG_FILE_NAME;

/**
 * This property provider resolve properties from specified file
 * in given directories.
 *
 * @author Dmitry Baev charlie@yandex-team.ru
 *         Date: 05.10.15
 */
public class AllurePropertyProvider extends SystemPropertyProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(AllurePropertyProvider.class);

    private final Path[] inputDirectories;

    /**
     * Creates an instance of provider.
     */
    public AllurePropertyProvider(Path... inputDirectories) {
        this.inputDirectories = inputDirectories;
    }

    /**
     * Load properties from all report.properties files in {@link #inputDirectories}.
     * <p/>
     * {@inheritDoc}
     */
    @Override
    public Properties provide(ClassLoader classLoader, Class<?> beanClass) {
        Properties properties = loadProperties(REPORT_CONFIG_FILE_NAME, inputDirectories);
        properties.putAll(super.provide(classLoader, beanClass));
        return properties;
    }

    /**
     * Load properties from all files with given name in specified directories.
     */
    public Properties loadProperties(String fileName, Path... directories) {
        Properties properties = new Properties();

        for (Path path : directories) {
            Path env = path.resolve(fileName);
            if (Files.notExists(env)) {
                continue;
            }
            try (InputStream stream = Files.newInputStream(env)) {
                properties.load(stream);
            } catch (IOException e) {
                LOGGER.debug("Could not read properties from file " + path, e);
            }
        }

        return properties;
    }
}
