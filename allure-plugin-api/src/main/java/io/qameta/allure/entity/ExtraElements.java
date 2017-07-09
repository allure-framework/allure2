package io.qameta.allure.entity;

import javax.xml.bind.annotation.XmlElement;
import java.util.HashMap;
import java.util.Map;

/**
 * Base class for entities that adds an ability to store extra data for beans.
 *
 * @since 2.0
 */
@SuppressWarnings("unchecked")
public class ExtraElements {

    @XmlElement
    private final Map<String, Object> extra = new HashMap<>();

    public void addExtraBlock(final String blockName, final Object block) {
        extra.put(blockName, block);
    }

    public <T> T getExtraBlock(final String blockName, final T defaultValue) {
        return (T) extra.computeIfAbsent(blockName, name -> defaultValue);
    }

    public <T> T getExtraBlock(final String blockName) {
        return (T) extra.get(blockName);
    }

    public boolean hasExtraBlock(final String blockName) {
        return extra.containsKey(blockName);
    }
}
