package io.qameta.allure.environment;

import io.qameta.allure.Widget;
import io.qameta.allure.core.Configuration;
import io.qameta.allure.core.LaunchResults;
import io.qameta.allure.entity.Environment;
import io.qameta.allure.entity.EnvironmentItem;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;

/**
 * @author Egor Borisov ehborisov@gmail.com
 */
public class Allure1EnvironmentPlugin implements Widget {

    public static final String ENVIRONMENT_BLOCK_NAME = "environment";

    @Override
    public List getData(final Configuration configuration,
                        final List<LaunchResults> launches) {
        final List<Environment> launchEnvironments = launches.stream().map(launch ->
                launch.getExtra(ENVIRONMENT_BLOCK_NAME))
                .filter(Optional::isPresent).map(Optional::get)
                .filter(Environment.class::isInstance)
                .map(Environment.class::cast).collect(toList());

        return launchEnvironments.stream()
                .flatMap(env -> env.getEnvironmentItems().stream())
                .collect(groupingBy(EnvironmentItem::getName, toList()))
                .entrySet().stream().map(this::aggregateItem).collect(toList());
    }

    private EnvironmentItem aggregateItem(final Map.Entry<String, List<EnvironmentItem>> entry) {
        return new EnvironmentItem()
                .withName(entry.getKey())
                .withValues(entry.getValue().stream().flatMap(e -> e.getValues().stream()).collect(toList()));
    }

    @Override
    public String getName() {
        return "environment";
    }
}
