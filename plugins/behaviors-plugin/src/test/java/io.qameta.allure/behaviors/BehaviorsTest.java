package io.qameta.allure.behaviors;

import io.qameta.allure.ConfigurationBuilder;
import io.qameta.allure.allure2.Allure2Plugin;
import io.qameta.allure.core.Configuration;
import io.qameta.allure.core.LaunchResults;
import io.qameta.allure.entity.Statistic;
import io.qameta.allure.tree.TreeWidgetData;
import io.qameta.allure.tree.TreeWidgetItem;
import io.qameta.allure.util.TestDataProcessor;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import static io.qameta.allure.AllureUtils.generateTestResultName;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Egor Borisov ehborisov@gmail.com
 */
public class BehaviorsTest {

    private final Configuration configuration = new ConfigurationBuilder().useDefault().build();

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();
    private TestDataProcessor helper;

    @Before
    public void prepare() throws IOException {
        helper = new TestDataProcessor(folder.newFolder().toPath(), new Allure2Plugin());
    }

    @Test
    public void storiesPerFeatureResultsAggregation() throws IOException {
        final Statistic feature1 = new Statistic()
                .withPassed(2);
        final Statistic feature2 = new Statistic()
                .withPassed(1)
                .withFailed(2);
        final Statistic feature3 = new Statistic()
                .withFailed(2);

        LaunchResults results = helper.processResources(
                "storiesaggregation/test-1-result.json", generateTestResultName(),
                "storiesaggregation/test-2-result.json", generateTestResultName(),
                "storiesaggregation/test-3-result.json", generateTestResultName(),
                "storiesaggregation/test-4-result.json", generateTestResultName());

        TreeWidgetData behaviorsData = (TreeWidgetData) new BehaviorsPlugin().getData(configuration,
                Collections.singletonList(results));
        assertStatisticForFeature(behaviorsData.getItems(), "feature1", feature1);
        assertStatisticForFeature(behaviorsData.getItems(), "feature2", feature2);
        assertStatisticForFeature(behaviorsData.getItems(), "feature3", feature3);
    }

    private void assertStatisticForFeature(final List<TreeWidgetItem> items, final String featureName,
                                           final Statistic expected) {
        assertThat(items)
                .filteredOn(node -> node.getName().equals(featureName))
                .extracting(TreeWidgetItem::getStatistic).first()
                .as("Unexpected aggregated statistic for feature %s", featureName)
                .isEqualToComparingFieldByField(expected);
    }
}
