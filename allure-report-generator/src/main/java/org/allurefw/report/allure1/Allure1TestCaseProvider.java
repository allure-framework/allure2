package org.allurefw.report.allure1;

import org.allurefw.report.ResultDirectories;
import org.allurefw.report.TestCase;
import org.allurefw.report.TestCaseProvider;

import javax.inject.Inject;
import java.nio.file.Path;
import java.util.Iterator;

/**
 * @author Dmitry Baev charlie@yandex-team.ru
 *         Date: 30.01.16
 */
public class Allure1TestCaseProvider implements TestCaseProvider {

    private final Path[] resultDirectories;

    @Inject
    public Allure1TestCaseProvider(@ResultDirectories Path[] resultDirectories) {
        this.resultDirectories = resultDirectories;
    }

    @Override
    public Iterator<TestCase> iterator() {
        return new TestCaseResultIterator(
                new XmlTestSuiteResultIterator(resultDirectories)
        );
    }
}
