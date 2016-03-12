package org.allurefw.allure1;

import org.allurefw.report.ReportApiUtils;
import org.allurefw.report.entity.GroupInfo;
import org.allurefw.report.entity.TestCase;
import org.apache.camel.Body;
import org.apache.camel.Exchange;
import org.apache.camel.Handler;
import org.apache.camel.Header;
import org.apache.camel.Produce;
import org.apache.camel.ProducerTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.yandex.qatools.allure.model.TestSuiteResult;

import java.util.Objects;
import java.util.function.Supplier;

/**
 * @author Dmitry Baev charlie@yandex-team.ru
 *         Date: 07.03.16
 */
public class Allure1Result {

    public static final Logger LOGGER = LoggerFactory.getLogger(Allure1Result.class);

    @Produce(uri = "direct:testCase")
    protected ProducerTemplate testCases;

    @Produce(uri = "direct:testSuite")
    protected ProducerTemplate testSuites;

    @SuppressWarnings("unused")
    @Handler
    public void process(@Body TestSuiteResult testSuite, @Header(Exchange.FILE_NAME) String fileName) {
        LOGGER.info("file: {}; suite: {}",
                fileName,
                Objects.requireNonNull(testSuite, couldNotUnmarshal(fileName)).getName()
        );

        testSuites.sendBody(new GroupInfo()
                .withUid(ReportApiUtils.generateUid())
                .withName(testSuite.getName()));

        testSuite.getTestCases().forEach(testCase ->
                testCases.sendBody(new TestCase()
                        .withUid(ReportApiUtils.generateUid())
                        .withName(testCase.getName()))
        );
    }

    protected Supplier<String> couldNotUnmarshal(String fileName) {
        return () -> String.format("Could not unmarshal %s file", fileName);
    }
}
