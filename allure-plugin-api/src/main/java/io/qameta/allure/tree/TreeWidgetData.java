package io.qameta.allure.tree;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author charlie (Dmitry Baev).
 */
@Data
@Accessors(chain = true)
public class TreeWidgetData implements Serializable {

    private static final long serialVersionUID = 1L;

    protected long total;
    protected List<TreeWidgetItem> items = new ArrayList<>();

}
