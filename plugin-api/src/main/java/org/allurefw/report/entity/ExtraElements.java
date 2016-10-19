package org.allurefw.report.entity;

import java.util.HashMap;
import java.util.Map;

/**
 * @author charlie (Dmitry Baev).
 */
public class ExtraElements {

    private Map<String, Object> extra = new HashMap<>();

    public void addExtraBlock(String blockName, Object block) {
        extra.put(blockName, block);
    }

    public <T> T getExtraBlock(String blockName, T defaultValue) {
        //noinspection unchecked
        return extra.containsKey(blockName)
                ? (T) extra.get(blockName)
                : defaultValue;
    }

    public boolean hasExtraBlock(String blockName) {
        return extra.containsKey(blockName);
    }

    public <T> T getExtraBlock(String blockName) {
        //noinspection unchecked
        return (T) extra.get(blockName);
    }
}
