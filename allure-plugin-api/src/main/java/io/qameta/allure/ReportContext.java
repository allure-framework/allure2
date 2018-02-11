package io.qameta.allure;

import io.qameta.allure.entity.Executor;
import io.qameta.allure.entity.Job;
import io.qameta.allure.entity.Project;

/**
 * @author charlie (Dmitry Baev).
 */
public interface ReportContext {

    Project getProject();

    Job getJob();

    Executor getExecutor();

}
