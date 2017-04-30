package io.qameta.allure.environment;

import io.qameta.allure.Widget;
import io.qameta.allure.core.Configuration;
import io.qameta.allure.core.LaunchResults;
import io.qameta.allure.entity.Environment;
import io.qameta.allure.entity.EnvironmentItem;

import java.util.List;
import java.util.Optional;

import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.reducing;
import static java.util.stream.Collectors.toList;

/**
 * @author Egor Borisov ehborisov@gmail.com
 */
public class Allure1EnvironmentPlugin implements Widget {

    public static final String ENVIRONMENT_BLOCK_NAME = "environment";

    @Override
    public Environment getData(final Configuration configuration,
                               final List<LaunchResults> launches) {
        final List<Environment> launchEnvironments = launches.stream().map(launch ->
                launch.getExtra(ENVIRONMENT_BLOCK_NAME)
                        .filter(Environment.class::isInstance)
                        .map(Environment.class::cast)
                        .orElse(new Environment())).collect(toList());

        final List<EnvironmentItem> globalEnvValues = launchEnvironments.stream()
                .flatMap(env -> env.getEnvironmentItems().stream())
                .collect(collectingAndThen(groupingBy(EnvironmentItem::getName,
                        reducing((EnvironmentItem e1, EnvironmentItem e2) -> {
                            e1.getValues().addAll(e2.getValues());
                            return e1;
                        })), e -> e.values().stream().filter(Optional::isPresent).map(Optional::get).collect(toList())
                ));
        return new Environment().withEnvironmentItems(globalEnvValues);
    }

    @Override
    public String getName() {
        return "environment";
    }
}
