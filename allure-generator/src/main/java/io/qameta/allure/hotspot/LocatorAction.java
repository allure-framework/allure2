package io.qameta.allure.hotspot;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.HashSet;
import java.util.Set;

/**
 * @author eroshenkoam (Artem Eroshenko).
 */
@Data
@Accessors(chain = true)
public class LocatorAction {

    private String fullPath;
    private Set<String> urls = new HashSet<>();

}
