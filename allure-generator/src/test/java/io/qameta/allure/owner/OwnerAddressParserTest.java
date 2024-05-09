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

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class OwnerAddressParserTest {
    @Test
    void shouldReturnNullForNullInput() {
        assertNull(OwnerAddressParser.parseAddress(null));
    }

    @Test
    void shouldReturnNullForEmptyInput() {
        assertNull(OwnerAddressParser.parseAddress(""));
    }

    @Test
    void shouldParseRFC2822FormattedStringWithEmail() {
        String input = "John Doe < john.doe@example.com >";
        OwnerAddress expected = new OwnerAddress("John Doe", "mailto:john.doe@example.com");
        assertEquals(expected.getDisplayName(), OwnerAddressParser.parseAddress(input).getDisplayName());
        assertEquals(expected.getUrl(), OwnerAddressParser.parseAddress(input).getUrl());
    }

    @Test
    void shouldParseRFC2822FormattedStringWithURL() {
        String input = "John Doe <https://github.com/@john.doe>";
        OwnerAddress expected = new OwnerAddress("John Doe", "https://github.com/@john.doe");
        assertEquals(expected.getDisplayName(), OwnerAddressParser.parseAddress(input).getDisplayName());
        assertEquals(expected.getUrl(), OwnerAddressParser.parseAddress(input).getUrl());
    }

    @Test
    void shouldReturnOnlyDisplayNameForEmptyRFC822Address() {
        String emptyAddress = "John Doe <>";
        OwnerAddress actual = OwnerAddressParser.parseAddress(emptyAddress);
        assertEquals("John Doe", actual.getDisplayName());
        assertNull(actual.getUrl());
    }

    @Test
    void shouldReturnDisplayNameForPlainTextInput() {
        String displayName = "John Doe";
        OwnerAddress expected = new OwnerAddress(displayName, null);
        assertEquals(expected.getDisplayName(), OwnerAddressParser.parseAddress(displayName).getDisplayName());
        assertNull(OwnerAddressParser.parseAddress(displayName).getUrl());
    }

    @Test
    void shouldReturnDisplayNameAndUrlForEmailAddress() {
        String email = "john.doe@example.com";
        OwnerAddress expected = new OwnerAddress(email, "mailto:" + email);
        assertEquals(expected.getDisplayName(), OwnerAddressParser.parseAddress(email).getDisplayName());
        assertEquals(expected.getUrl(), OwnerAddressParser.parseAddress(email).getUrl());
    }

    @Test
    void shouldReturnDisplayNameAndUrlForValidURL() {
        String validUrl = "https://github.com/john.doe";
        OwnerAddress expected = new OwnerAddress(validUrl, validUrl);
        assertEquals(expected.getDisplayName(), OwnerAddressParser.parseAddress(validUrl).getDisplayName());
        assertEquals(expected.getUrl(), OwnerAddressParser.parseAddress(validUrl).getUrl());
    }

    @Test
    void shouldReturnOnlyDisplayNameForInvalidURL() {
        String invalidUrl = "htp:/www.example.com/page";
        OwnerAddress actual = OwnerAddressParser.parseAddress(invalidUrl);
        assertEquals(invalidUrl, actual.getDisplayName());
        assertNull(actual.getUrl());
    }

    @Test
    void shouldReturnOnlyDisplayNameForInvalidEmail() {
        String invalidEmail = "user@.example.com";
        OwnerAddress actual = OwnerAddressParser.parseAddress(invalidEmail);
        assertEquals(invalidEmail, actual.getDisplayName());
        assertNull(actual.getUrl());
    }

    @Test
    void shouldReturnInvalidRFC822AddressUnchanged() {
        String invalidAddress = "John Doe <john@@doe>";
        OwnerAddress actual = OwnerAddressParser.parseAddress(invalidAddress);
        assertEquals(invalidAddress, actual.getDisplayName());
        assertNull(actual.getUrl());
    }
}
