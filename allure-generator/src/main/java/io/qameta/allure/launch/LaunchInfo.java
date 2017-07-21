package io.qameta.allure.launch;

import io.qameta.allure.entity.Statistic;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * @author charlie (Dmitry Baev).
 */
@Data
@Accessors(chain = true)
public class LaunchInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    protected String uid;
    protected String name;
    protected Statistic statistic = new Statistic();

}
