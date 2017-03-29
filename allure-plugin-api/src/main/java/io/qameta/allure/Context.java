package io.qameta.allure;

/**
 * @author charlie (Dmitry Baev).
 */
@FunctionalInterface
public interface Context<T> {

    T getValue();

}
