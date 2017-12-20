package io.qameta.allure.tree;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author charlie (Dmitry Baev).
 */
@Data
@Accessors(chain = true)
public class GroupNodeContext {

    private final String key;
    private final String value;

    public GroupNodeContext(final String key, final String value) {
        this.key = key;
        this.value = value;
    }

}
