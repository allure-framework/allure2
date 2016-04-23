package org.allurefw.report;

/**
 * @author Dmitry Baev charlie@yandex-team.ru
 *         Date: 23.04.16
 */
public interface Finalizer<T> {

    Object finalize(T identity);

    static <T> Finalizer<T> identity() {
        return identity -> identity;
    }
}
