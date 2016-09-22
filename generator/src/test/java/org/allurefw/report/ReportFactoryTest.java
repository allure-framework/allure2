package org.allurefw.report;

import com.google.inject.Guice;
import org.junit.Test;

import java.nio.file.Path;
import java.nio.file.Paths;

import static java.util.Collections.emptyList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsNull.notNullValue;

/**
 * @author charlie (Dmitry Baev).
 */
public class ReportFactoryTest {

    @Test
    public void shouldCreateReport() throws Exception {
        ReportFactory factory = Guice.createInjector(new ParentModule(emptyList(), emptyList()))
                .getInstance(ReportFactory.class);

        Path results = Paths.get("/Users/charlie/projects/allure-report/generator/src/test/resources/allure1data");
        ReportInfo report = factory.create(results);

        assertThat(report, notNullValue());
    }
}
