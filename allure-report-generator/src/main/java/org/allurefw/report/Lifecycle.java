package org.allurefw.report;

import com.google.inject.Inject;

import java.util.Set;

/**
 * @author Dmitry Baev charlie@yandex-team.ru
 *         Date: 30.01.16
 */
public class Lifecycle {

    @Inject
    protected Set<TestCaseProvider> providers;

    public void generate() {
        boolean findAnyResults = false;
        for (TestCaseProvider provider : providers) {
            System.out.println("Found provider " + provider.getClass());
            for (TestCase testCase : provider) {
                findAnyResults = true;
                System.out.println(testCase.getName());
            }
        }
        if (!findAnyResults) {
            System.out.println("Could not find any results");
        }
    }
}
