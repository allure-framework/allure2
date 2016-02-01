package org.allurefw.report.junit;

import org.allurefw.Status;
import org.allurefw.report.entity.Failure;
import org.allurefw.report.entity.TestCase;
import org.allurefw.report.entity.Time;
import org.allurefw.report.io.AbstractTestCaseIterator;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.util.Iterator;

import static org.allurefw.report.ReportApiUtils.generateUid;

/**
 * @author Dmitry Baev charlie@yandex-team.ru
 *         Date: 31.01.16
 */
public class TestCaseIterator extends AbstractTestCaseIterator<Testsuite, Testsuite.Testcase> {

    /**
     * {@inheritDoc}
     */
    public TestCaseIterator(Path[] resultDirectories) {
        super(resultDirectories);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Iterator<Testsuite> createReader(Path... resultDirectories) {
        return new JunitResultIterator(resultDirectories);
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
        dest.setUid(generateUid());
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
