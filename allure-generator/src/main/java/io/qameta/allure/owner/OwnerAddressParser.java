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

import org.apache.commons.validator.routines.EmailValidator;
import org.apache.commons.validator.routines.UrlValidator;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class OwnerAddressParser {
    private static final Pattern RFC2822_ADDRESS = Pattern.compile("^([^<>]+)\\s+<\\s*(\\S*)\\s*>$");

    private OwnerAddressParser() {
    }

    @SuppressWarnings("ReturnCount")
    public static OwnerAddress parseAddress(final String maybeAddress) {
        if (maybeAddress == null || maybeAddress.isEmpty()) {
            return null;
        }

        // Prevent performance degradation for plain text
        if (!isLikelyAddress(maybeAddress)) {
            return new OwnerAddress(maybeAddress, null);
        }

        String displayName = maybeAddress;
        String urlOrEmail = maybeAddress;

        final Matcher matcher = RFC2822_ADDRESS.matcher(maybeAddress);
        if (matcher.matches()) {
            displayName = matcher.group(1);
            urlOrEmail = matcher.group(2);
        }

        // e.g.: John Doe <>
        if (urlOrEmail.isEmpty()) {
            return new OwnerAddress(displayName, null);
        }

        // e.g.: John Doe <https://example.com>
        if (UrlValidator.getInstance().isValid(urlOrEmail)) {
            return new OwnerAddress(displayName, urlOrEmail);
        }

        // e.g.: John Doe <mail@example.com>
        if (EmailValidator.getInstance().isValid(urlOrEmail)) {
            return new OwnerAddress(displayName, "mailto:" + urlOrEmail);
        }

        // Non-compliant addresses are treated as plain text
        return new OwnerAddress(maybeAddress, null);
    }

    /**
     * Checks if the given string is likely to be a plain text (not an email or URL).
     * Regular expressions are slow, therefore we just check for common characters.
     */
    private static boolean isLikelyAddress(final String input) {
        return input.contains("@") || input.contains(":") || input.contains("<");
    }
}
