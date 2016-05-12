package org.allurefw.report;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * @author Dmitry Baev charlie@yandex-team.ru
 *         Date: 30.01.16
 */
public class ReportGeneratorTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Test
    public void shouldGenerateAllure1() throws Exception {
        Path input = Paths.get("/Users/charlie/projects/allure-report/allure-report-generator/src/test/java/org/allurefw/report/allure1data");
        Path output = folder.newFolder().toPath();

        Path report = Paths.get("/Users/charlie/projects/allure-report/allure-report-generator/target/allure1-report");
        Files.createDirectories(report);
        new ReportGenerator(input).generate(report);
    }

    @Test
    public void shouldGenerateJunit() throws Exception {
        Path input = Paths.get("/Users/charlie/projects/allure-report/allure-report-generator/src/test/java/org/allurefw/report/junitdata");
        Path output = folder.newFolder().toPath();

        Path report = Paths.get("/Users/charlie/projects/allure-report/allure-report-generator/target/junit-report");
        new ReportGenerator(input).generate(report);
    }
}