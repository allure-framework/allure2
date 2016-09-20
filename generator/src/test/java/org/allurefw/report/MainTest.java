package org.allurefw.report;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;

/**
 * @author charlie (Dmitry Baev).
 */
public class MainTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Test
    public void shouldGenerateTheReport() throws Exception {
        Path plugins = folder.newFolder().toPath();
        Path work = folder.newFolder().toPath();
        Main main = new Main(plugins, work, Collections.emptySet());

        Path results = Paths.get("/Users/charlie/projects/allure-report/generator/src/test/resources/allure1data");
        ReportInfo report = main.createReport(results);

        assertThat(report, notNullValue());
        assertThat(report.getResults(), hasSize(20));

        Path output = Paths.get("/Users/charlie/projects/allure-report/generator/target/new-report");
        main.generate(output, results);
    }
}