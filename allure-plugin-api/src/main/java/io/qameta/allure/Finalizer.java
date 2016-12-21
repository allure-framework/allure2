package io.qameta.allure;

/**
 * @author Dmitry Baev baev@qameta.io
 *         Date: 23.04.16
 */
@FunctionalInterface
public interface Finalizer<T> {

    Object convert(T identity);

    static <T> Finalizer<T> identity() {
        return identity -> identity;
    }
}
