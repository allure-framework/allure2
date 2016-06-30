package org.allurefw.report;

/**
 * @author Dmitry Baev baev@qameta.io
 *         Date: 23.04.16
 */
public interface Finalizer<T> {

    Object finalize(T identity);

    static <T> Finalizer<T> identity() {
        return identity -> identity;
    }
}
