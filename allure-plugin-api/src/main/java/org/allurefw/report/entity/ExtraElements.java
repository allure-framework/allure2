package org.allurefw.report.entity;

import javax.xml.bind.annotation.XmlElement;
import java.util.HashMap;
import java.util.Map;

/**
 * @author charlie (Dmitry Baev).
 */
public class ExtraElements {

    @XmlElement
    private Map<String, Object> extra = new HashMap<>();

    public void addExtraBlock(String blockName, Object block) {
        extra.put(blockName, block);
    }

    public <T> T getExtraBlock(String blockName, T defaultValue) {
        //noinspection unchecked
        return (T) extra.computeIfAbsent(blockName, name -> defaultValue);
    }

    public boolean hasExtraBlock(String blockName) {
        return extra.containsKey(blockName);
    }

    public <T> T getExtraBlock(String blockName) {
        //noinspection unchecked
        return (T) extra.get(blockName);
    }
}
