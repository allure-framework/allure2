package io.qameta.allure.environment;

import io.qameta.allure.Widget;
import io.qameta.allure.core.Configuration;
import io.qameta.allure.core.LaunchResults;
import io.qameta.allure.entity.EnvironmentItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

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
        final List<EnvironmentItem> launchEnvironments = launches.stream()
                .flatMap(launch ->
                        launch.getExtra(ENVIRONMENT_BLOCK_NAME,
                                (Supplier<ArrayList<EnvironmentItem>>) ArrayList::new).stream())
                .collect(toList());

        return launchEnvironments.stream()
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
