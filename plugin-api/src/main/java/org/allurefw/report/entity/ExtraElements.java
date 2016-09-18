package org.allurefw.report.entity;

import java.util.Map;

/**
 * @author charlie (Dmitry Baev).
 */
public class ExtraElements {

    private Map<String, Object> extra;

    public void addExtraBlock(String blockName, Object block) {
        extra.put(blockName, block);
    }
}
