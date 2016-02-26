package org.allurefw.report.entity;

import java.util.List;

/**
 * @author Dmitry Baev charlie@yandex-team.ru
 *         Date: 31.01.16
 */
public interface WithSteps {

    List<Step> getSteps();

    void setSteps(List<Step> steps);

}
