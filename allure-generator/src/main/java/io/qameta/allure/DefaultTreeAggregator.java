package io.qameta.allure;

import io.qameta.allure.config.Group;
import io.qameta.allure.entity.TestResult;
import io.qameta.allure.service.TestResultService;
import io.qameta.allure.tree.Classifier;
import io.qameta.allure.tree.TestResultTree;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static io.qameta.allure.tree.TreeUtils.groupByLabels;

/**
 * @author charlie (Dmitry Baev).
 */
public class DefaultTreeAggregator extends AbstractJsonAggregator {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultTreeAggregator.class);

    private final String id;
    private final Group group;

    public DefaultTreeAggregator(final String id, final Group group) {
        super(String.format("%s.json", id));
        this.group = group;
        this.id = id;
    }

    @Override
    protected TestResultTree getData(final ReportContext context,
                                     final TestResultService service) {
        LOGGER.info("Aggregates data for {}: {}", id, group);

        final Classifier<TestResult> classifier = testResult -> groupByLabels(testResult, group.getFields());
        final TestResultTree tree = new TestResultTree(id, classifier);
        service.findAllTests(false).forEach(tree::add);
        return tree;
    }
}
