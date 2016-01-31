package org.allurefw.report.junit;

import org.allurefw.report.io.AbstractResultsIterator;
import ru.yandex.qatools.allure.BadXmlCharacterFilterReader;

import javax.xml.bind.JAXB;
import java.io.IOException;
import java.nio.file.Path;

import static ru.yandex.qatools.allure.AllureConstants.TEST_SUITE_XML_FILE_GLOB;

/**
 * @author Dmitry Baev charlie@yandex-team.ru
 *         Date: 08.10.15
 */
public class JunitResultIterator extends AbstractResultsIterator<Testsuite> {

    /**
     * Creates an instance of iterator.
     */
    public JunitResultIterator(Path... resultDirectories) {
        super(resultDirectories);
    }

    /**
     * Read XML test suite result.
     * <p/>
     * {@inheritDoc}
     */
    @Override
    protected Testsuite readResult(Path path) throws IOException {
        try (BadXmlCharacterFilterReader reader = new BadXmlCharacterFilterReader(path)) {
            return JAXB.unmarshal(reader, Testsuite.class);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getFilesGlob() {
        return "TEST-*.xml";
    }

}
