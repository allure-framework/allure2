package io.qameta.allure.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * @author charlie (Dmitry Baev).
 */
public final class Hashing {

    private static final String MD_5 = "md5";

    private Hashing() {
        throw new IllegalStateException("Do not instance");
    }

    public static MessageDigest md5() {
        try {
            return MessageDigest.getInstance(MD_5);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("Could not find md5 hashing algorithm", e);
        }
    }
}
