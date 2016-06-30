package org.allurefw.report.config;

import ru.qatools.properties.providers.SystemPropertyProvider;

import java.nio.file.Path;
import java.util.Properties;

import static org.allurefw.allure1.AllureConstants.REPORT_CONFIG_FILE_NAME;
import static org.allurefw.report.ReportApiUtils.loadProperties;

/**
 * This property provider resolve properties from specified file
 * in given directories.
 *
 * @author Dmitry Baev baev@qameta.io
 *         Date: 05.10.15
 */
public class AllurePropertyProvider extends SystemPropertyProvider {

    private final Path[] inputDirectories;

    /**
     * Creates an instance of provider.
     */
    public AllurePropertyProvider(Path... inputDirectories) {
        this.inputDirectories = inputDirectories;
    }

    /**
     * Load properties from all reportData.properties files in {@link #inputDirectories}.
     * <p/>
     * {@inheritDoc}
     */
    @Override
    public Properties provide(ClassLoader classLoader, Class<?> beanClass) {
        Properties properties = loadProperties(new Properties(), REPORT_CONFIG_FILE_NAME, inputDirectories);
        properties.putAll(super.provide(classLoader, beanClass));
        return properties;
    }

}
