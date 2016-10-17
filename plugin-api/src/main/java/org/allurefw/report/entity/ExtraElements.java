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

    public Map<String, Object> getExtra() {
        return extra;
    }
}
