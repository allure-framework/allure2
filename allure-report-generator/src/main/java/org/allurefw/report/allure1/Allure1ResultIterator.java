package org.allurefw.report.allure1;

import org.allurefw.report.io.AbstractResultsIterator;
import ru.yandex.qatools.allure.BadXmlCharacterFilterReader;
import ru.yandex.qatools.allure.model.TestSuiteResult;

import javax.xml.bind.JAXB;
import java.io.IOException;
import java.nio.file.Path;

import static ru.yandex.qatools.allure.AllureConstants.TEST_SUITE_XML_FILE_GLOB;

/**
 * @author Dmitry Baev charlie@yandex-team.ru
 *         Date: 08.10.15
 */
public class Allure1ResultIterator extends AbstractResultsIterator<TestSuiteResult> {

    /**
     * Creates an instance of iterator.
     */
    public Allure1ResultIterator(Path... resultDirectories) {
        super(resultDirectories);
    }

    /**
     * Read XML test suite result.
     * <p/>
     * {@inheritDoc}
     */
    @Override
    protected TestSuiteResult readResult(Path path) throws IOException {
        try (BadXmlCharacterFilterReader reader = new BadXmlCharacterFilterReader(path)) {
            return JAXB.unmarshal(reader, TestSuiteResult.class);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getFilesGlob() {
        return TEST_SUITE_XML_FILE_GLOB;
    }

}
