/*
 *  Copyright 2016-2026 Qameta Software Inc
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
package io.qameta.allure.util;

import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;

import java.util.Objects;

public final class HtmlSanitizerUtils {

    private static final String ANCHOR_TAG = "a";
    private static final String HREF_ATTRIBUTE = "href";
    private static final Safelist SAFELIST = Safelist.relaxed()
            .removeTags("img")
            .removeProtocols(ANCHOR_TAG, HREF_ATTRIBUTE, "ftp", "mailto")
            .addProtocols(ANCHOR_TAG, HREF_ATTRIBUTE, "http", "https");

    private HtmlSanitizerUtils() {
        throw new IllegalStateException("Utility class");
    }

    public static String sanitizeHtml(final String source) {
        if (Objects.isNull(source)) {
            return null;
        }
        return Jsoup.clean(source, SAFELIST);
    }
}
