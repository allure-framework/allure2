package io.qameta.allure.environment;

import io.qameta.allure.CommonJsonAggregator;
import io.qameta.allure.core.LaunchResults;
import io.qameta.allure.entity.EnvironmentItem;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import static io.qameta.allure.allure1.Allure1Plugin.ENVIRONMENT_BLOCK_NAME;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;

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
                        (Supplier<Map<String, String>>) HashMap::new).entrySet().stream())
                .collect(toList());

        return launchEnvironments.stream()
                .collect(groupingBy(Map.Entry::getKey, toList()))
                .entrySet().stream().map(Allure1EnvironmentPlugin::aggregateItem).collect(toList());
    }

    private static EnvironmentItem aggregateItem(final Map.Entry<String, List<Map.Entry<String, String>>> entry) {
        return new EnvironmentItem()
                .setName(entry.getKey())
                .setValues(entry.getValue().stream().map(Map.Entry::getValue).collect(toList()));
    }
}
