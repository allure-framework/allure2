package io.qameta.allure.context;

import io.qameta.allure.Context;

import java.math.BigInteger;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Supplier;

/**
 * This context used to generate random uids for Allure results.
 *
 * @since 2.0
 */
public class RandomUidContext implements Context<Supplier<String>> {

    private static final int UID_RANDOM_BYTES_COUNT = 8;

    private static final int RADIX = 16;

    @Override
    public Supplier<String> getValue() {
        return () -> {
            final byte[] randomBytes = new byte[UID_RANDOM_BYTES_COUNT];
            ThreadLocalRandom.current().nextBytes(randomBytes);
            return new BigInteger(1, randomBytes).toString(RADIX);
        };
    }
}
