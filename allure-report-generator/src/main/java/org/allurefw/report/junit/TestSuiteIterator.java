package org.allurefw.report.junit;

import org.allurefw.Status;
import org.allurefw.report.Failure;
import org.allurefw.report.TestCase;
import org.allurefw.report.Time;
import org.allurefw.report.io.AbstractTestCaseGroupIterator;

import java.math.BigDecimal;
import java.util.Iterator;

/**
 * @author Dmitry Baev charlie@yandex-team.ru
 *         Date: 08.10.15
 */
public class TestSuiteIterator
        extends AbstractTestCaseGroupIterator<Testsuite, Testsuite.Testcase> {

    /**
     * {@inheritDoc}
     */
    public TestSuiteIterator(Testsuite testSuite) {
        super(testSuite);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Iterator<Testsuite.Testcase> extract(Testsuite testSuite) {
        return testSuite.getTestcase().iterator();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected TestCase convert(Testsuite.Testcase source) {
        TestCase dest = new TestCase();
        dest.setName(source.getName());
        dest.setTime(new Time()
                .withDuration(source.getTime().multiply(new BigDecimal(1000)).longValue())
        );
        dest.setStatus(getStatus(source));
        dest.setFailure(getFailure(source));
        return dest;
    }

    protected Status getStatus(Testsuite.Testcase source) {
        if (source.getFailure() != null) {
            return Status.FAILED;
        }
        if (source.getError() != null) {
            return Status.BROKEN;
        }
        return Status.PASSED;
    }

    protected Failure getFailure(Testsuite.Testcase source) {
        if (source.getFailure() != null) {
            return new Failure().withMessage(source.getFailure().getMessage());
        }
        if (source.getError() != null) {
            return new Failure().withMessage(source.getError().getMessage());
        }
        return null;
    }
}
