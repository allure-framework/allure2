package io.qameta.allure.category;

import io.qameta.allure.entity.Status;
import io.qameta.allure.entity.TestCaseResult;
import org.junit.Test;

import static io.qameta.allure.category.CategoryPlugin.matches;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

/**
 * @author charlie (Dmitry Baev).
 */
public class CategoryPluginTest {

    @Test
    public void shouldMatchByStatus() throws Exception {
        Category category = new Category().withMatchedStatuses(Status.FAILED, Status.BROKEN);
        TestCaseResult first = new TestCaseResult().withStatus(Status.FAILED);
        TestCaseResult second = new TestCaseResult().withStatus(Status.BROKEN);
        TestCaseResult third = new TestCaseResult().withStatus(Status.PASSED);
        TestCaseResult fourth = new TestCaseResult();
        assertThat(matches(first, category), is(true));
        assertThat(matches(second, category), is(true));
        assertThat(matches(third, category), is(false));
        assertThat(matches(fourth, category), is(false));
    }
}