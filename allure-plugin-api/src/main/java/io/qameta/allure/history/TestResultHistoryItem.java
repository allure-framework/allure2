package io.qameta.allure.history;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * @author charlie (Dmitry Baev).
 */
@Data
@Accessors(chain = true)
public class TestResultHistoryItem implements Serializable {

    private static final long serialVersionUID = 1L;


}
