package io.qameta.allure.hotspot;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author eroshenkoam (Artem Eroshenko).
 */
@Data
@Accessors(chain = true)
public class Element {

    private String fullPath;
    private Set<String> urls = new HashSet<>();
    private List<Test> tests = new ArrayList<>();

    public Element addUrls(final Set<String> url) {
        urls.addAll(url);
        return this;
    }


    /**
     * @author eroshenkoam (Artem Eroshenko).
     */
    @Data
    @Accessors(chain = true)
    public static class Test {

        private String uid;
        private String name;
        private String url;
        private String status;
        private Long duration;

    }

}
