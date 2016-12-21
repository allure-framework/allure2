package io.qameta.allure.entity;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author charlie (Dmitry Baev).
 */
public interface WithLinks {

    List<Link> getLinks();

    void setLinks(List<Link> links);

    default void updateLinks(List<Link> links) {
        List<Link> updated = Stream.concat(getLinks().stream(), links.stream())
                .distinct()
                .collect(Collectors.toList());
        setLinks(updated);
    }
}
