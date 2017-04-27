package io.qameta.allure.environment;

import io.qameta.allure.Reader;
import io.qameta.allure.Widget;
import io.qameta.allure.allure1.Allure1FilesReader;
import io.qameta.allure.core.Configuration;
import io.qameta.allure.core.LaunchResults;
import io.qameta.allure.core.ResultsVisitor;
import io.qameta.allure.entity.Environment;
import io.qameta.allure.entity.EnvironmentItem;
import ru.yandex.qatools.allure.model.ParameterKind;

import javax.xml.bind.JAXB;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

/**
 * @author Egor Borisov ehborisov@gmail.com
 */
public class EnvironmentPlugin implements Reader, Widget {

    private static final String ENVIRONMENT_BLOCK_NAME = "environment";

    @Override
    public void readResults(final Configuration configuration,
                            final ResultsVisitor visitor,
                            final Path directory) {
        final Environment environment = new Environment();
        final List<EnvironmentItem> testEnvVariables = getEnvironmentVariables(directory);
        environment.withEnvironmentItems(testEnvVariables);
        final Path envPropsFile = directory.resolve("environment.properties");
        if (Files.exists(envPropsFile)) {
            try (InputStream is = Files.newInputStream(envPropsFile)) {
                final Properties properties = new Properties();
                properties.load(is);
                environment.withEnvironmentItems(convertItems(properties));
            } catch (IOException e) {
                visitor.error("Could not read environments.properties file " + envPropsFile, e);
            }
        }
        final Path envXmlFile = directory.resolve("environment.xml");
        if (Files.exists(envXmlFile)) {
            try (FileInputStream fis = new FileInputStream(envXmlFile.toFile())) {
                final ru.yandex.qatools.commons.model.Environment result =
                        JAXB.unmarshal(fis, ru.yandex.qatools.commons.model.Environment.class);
                final List<EnvironmentItem> fromXml = result.getParameter().stream()
                        .map(param -> new EnvironmentItem().withName(param.getKey()).withValues(param.getValue()))
                        .collect(Collectors.toList());
                environment.withEnvironmentItems(fromXml);
            } catch (Exception e) {
                visitor.error("Could not read environment.xml file " + envXmlFile.toAbsolutePath(), e);
            }
        }

        if (!environment.getEnvironmentItems().isEmpty()) {
            visitor.visitExtra(ENVIRONMENT_BLOCK_NAME, environment);
        }
    }

    @Override
    public Environment getData(final Configuration configuration,
                               final List<LaunchResults> launches) {
        final List<Environment> launchEnvironments = launches.stream().map(launch ->
                launch.getExtra(ENVIRONMENT_BLOCK_NAME)
                        .filter(Environment.class::isInstance)
                        .map(Environment.class::cast)
                        .orElse(new Environment())).collect(Collectors.toList());

        final Map<String, EnvironmentItem> globalEnvValues = new HashMap<>();
        launchEnvironments
                .forEach(env -> env.getEnvironmentItems()
                        .forEach(item -> {
                            globalEnvValues.computeIfPresent(item.getName(),
                                (key, value) -> value.withValues(item.getValues()));
                            globalEnvValues.putIfAbsent(item.getName(), item);
                        }));
        return new Environment().withEnvironmentItems(globalEnvValues.values());
    }

    @Override
    public String getName() {
        return "environment";
    }

    public static Collection<EnvironmentItem> convertItems(final Properties properties) {
        return properties.keySet().stream().map(key ->
                new EnvironmentItem().withName(key.toString())
                        .withValues(properties.getProperty(key.toString()))
        ).collect(Collectors.toSet());
    }

    private List<EnvironmentItem> getEnvironmentVariables(final Path directory) {
        final Allure1FilesReader reader = new Allure1FilesReader(directory);
        return reader.getStreamOfAllure1Results()
                .flatMap(suite -> suite.getTestCases().stream())
                .flatMap(result -> result.getParameters().stream().filter(this::hasEnvType))
                .map(parameter -> new EnvironmentItem()
                        .withName(parameter.getName())
                        .withValues(parameter.getValue())
                ).collect(Collectors.toList());
    }

    private boolean hasEnvType(final ru.yandex.qatools.allure.model.Parameter parameter) {
        return ParameterKind.ENVIRONMENT_VARIABLE.equals(parameter.getKind());
    }
}
