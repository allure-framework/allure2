package io.qameta.allure;

import io.qameta.allure.entity.Executor;
import io.qameta.allure.entity.Job;
import io.qameta.allure.entity.Project;

/**
 * @author charlie (Dmitry Baev).
 */
public class DefaultReportContext implements ReportContext {

    private final Project project;
    private final Job job;
    private final Executor executor;

    public DefaultReportContext(final Project project, final Executor executor, final Job job) {
        this.project = project;
        this.job = job;
        this.executor = executor;
    }

    @Override
    public Project getProject() {
        return project;
    }

    @Override
    public Job getJob() {
        return job;
    }

    @Override
    public Executor getExecutor() {
        return executor;
    }
}
