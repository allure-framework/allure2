/*
 *  Copyright 2016-2023 Qameta Software OÜ
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
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
