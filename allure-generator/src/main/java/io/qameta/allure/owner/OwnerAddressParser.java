/*
 *  Copyright 2016-2024 Qameta Software Inc
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
package io.qameta.allure.owner;

import java.net.MalformedURLException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class OwnerAddressParser {
    private static final Pattern RFC2822_ADDRESS = Pattern.compile("^(.*) <(.*)>$");
    private static final Pattern LOOKS_LIKE_EMAIL = Pattern.compile("^[^@]+@[^@]+$");

    private OwnerAddressParser() {
    }

    public static OwnerAddress parseAddress(final String maybeAddress) {
        if (maybeAddress == null || maybeAddress.isEmpty()) {
            return null;
        }

        final Matcher matcher = RFC2822_ADDRESS.matcher(maybeAddress);
        if (matcher.matches()) {
            final String displayName = matcher.group(1);
            final String url = toHref(matcher.group(2));
            return new OwnerAddress(displayName, url);
        }

        return new OwnerAddress(maybeAddress, toHref(maybeAddress));
    }

    private static String toHref(final String address) {
        if (isValidURL(address)) {
            return address;
        }

        if (LOOKS_LIKE_EMAIL.matcher(address).matches()) {
            return "mailto:" + address;
        }

        return null;
    }

    private static boolean isValidURL(final String maybeURL) {
        try {
            new java.net.URL(maybeURL);
            return true;
        } catch (MalformedURLException e) {
            return false;
        }
    }
}
