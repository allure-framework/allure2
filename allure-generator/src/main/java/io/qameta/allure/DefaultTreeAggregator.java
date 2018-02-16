package io.qameta.allure;

import io.qameta.allure.config.Group;
import io.qameta.allure.entity.TestResult;
import io.qameta.allure.service.TestResultService;
import io.qameta.allure.tree.Classifier;
import io.qameta.allure.tree.TestResultTree;

import java.util.List;

import static io.qameta.allure.tree.TreeUtils.groupByLabels;

/**
 * @author charlie (Dmitry Baev).
 */
public class DefaultTreeAggregator extends AbstractJsonAggregator {

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
        final Classifier<TestResult> classifier = testResult -> groupByLabels(testResult, group.getFields());
        final TestResultTree tree = new TestResultTree(id, classifier);
        final List<TestResult> allTests = service.findAllTests(false);
        allTests.forEach(tree::add);
        tree.setTotal(allTests.size());
        return tree;
    }
}
