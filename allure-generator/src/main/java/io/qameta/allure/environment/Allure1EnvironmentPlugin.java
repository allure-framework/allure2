package io.qameta.allure.environment;

import io.qameta.allure.CommonWidgetAggregator;
import io.qameta.allure.CompositeAggregator;
import io.qameta.allure.core.Configuration;
import io.qameta.allure.core.LaunchResults;
import io.qameta.allure.entity.EnvironmentItem;

import java.util.Arrays;
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
public class Allure1EnvironmentPlugin extends CompositeAggregator {

    public Allure1EnvironmentPlugin() {
        super(Arrays.asList(
                new WidgetAggregator()
        ));
    }

    @SuppressWarnings("PMD.DefaultPackage")
    /* default */ static List<EnvironmentItem> getData(final List<LaunchResults> launches) {
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

    private static class WidgetAggregator extends CommonWidgetAggregator {

        WidgetAggregator() {
            super("environment.json");
        }

        @Override
        public Object getData(Configuration configuration, List<LaunchResults> launches) {
            List<EnvironmentItem> environmentItems = Allure1EnvironmentPlugin.getData(launches);
            return new WidgetCollection<>(environmentItems.size(), environmentItems);
        }




    }
}
