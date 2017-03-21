package io.qameta.allure.categories;

import io.qameta.allure.Processor;
import io.qameta.allure.entity.Status;
import io.qameta.allure.entity.TestCase;
import io.qameta.allure.entity.TestCaseResult;
import io.qameta.allure.entity.TestRun;

import java.util.ArrayList;
import java.util.List;

import static io.qameta.allure.categories.CategoriesPlugin.CATEGORIES;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;


/**
 * @author charlie (Dmitry Baev).
 */
public class CategoriesProcessor implements Processor {

    public static final Category UNKNOWN_FAILURE = new Category().withName("Unknown failure");
    public static final Category UNKNOWN_ERROR = new Category().withName("Unknown error");

    @Override
    public void process(final TestRun testRun, final TestCase testCase, final TestCaseResult result) {
        final List<Category> testRunCategories = testRun.getExtraBlock(CATEGORIES, new ArrayList<>());
        final List<Category> resultCategories = result.getExtraBlock(CATEGORIES, new ArrayList<>());
        for (Category category : testRunCategories) {
            if (matches(result, category)) {
                resultCategories.add(category);
            }
        }
        if (resultCategories.isEmpty() && Status.FAILED.equals(result.getStatus())) {
            result.getExtraBlock(CATEGORIES, new ArrayList<Category>()).add(UNKNOWN_FAILURE);
        }
        if (resultCategories.isEmpty() && Status.BROKEN.equals(result.getStatus())) {
            result.getExtraBlock(CATEGORIES, new ArrayList<Category>()).add(UNKNOWN_ERROR);
        }
    }

    public static boolean matches(final TestCaseResult result, final Category category) {
        boolean matchesStatus = category.getMatchedStatuses().isEmpty()
                || nonNull(result.getStatus())
                && category.getMatchedStatuses().contains(result.getStatus());
        boolean matchesMessage = isNull(category.getMessageRegex())
                || nonNull(result.getStatusDetails())
                && nonNull(result.getStatusDetails().getMessage())
                && result.getStatusDetails().getMessage().matches(category.getMessageRegex());
        boolean matchesTrace = isNull(category.getTraceRegex())
                || nonNull(result.getStatusDetails())
                && nonNull(result.getStatusDetails().getTrace())
                && result.getStatusDetails().getTrace().matches(category.getTraceRegex());
        return matchesStatus && matchesMessage && matchesTrace;
    }
}
