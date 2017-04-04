package io.qameta.allure.testdata;

import org.apache.commons.lang3.RandomStringUtils;

import java.util.concurrent.ThreadLocalRandom;

/**
 * @author charlie (Dmitry Baev).
 */
public final class TestData {

    private TestData() {
        throw new IllegalStateException("Do not instance");
    }

    public static String randomString() {
        return RandomStringUtils.randomAlphabetic(10);
    }

    public static int randomPort() {
        return ThreadLocalRandom.current().nextInt(65535);
    }
}
