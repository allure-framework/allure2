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
public class PluginConfiguration implements Serializable {

    private static final long serialVersionUID = 1L;

    protected String id;
    protected String name;
    protected String description;
    protected List<String> extensions = new ArrayList<>();
    protected List<String> jsFiles = new ArrayList<>();
    protected List<String> cssFiles = new ArrayList<>();

}
