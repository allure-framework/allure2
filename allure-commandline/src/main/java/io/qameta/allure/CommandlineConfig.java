package io.qameta.allure;

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
public class CommandlineConfig implements Serializable {

    private static final long serialVersionUID = 1L;

    protected List<String> plugins = new ArrayList<>();
}
