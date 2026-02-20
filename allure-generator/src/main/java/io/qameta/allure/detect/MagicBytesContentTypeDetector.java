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
package io.qameta.allure.detect;

import java.io.CharArrayWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Ported from
 * <a href="https://github.com/allure-framework/allure3/blob/main/packages/reader-api/src/detect.ts">Allure 3</a>.
 * <p>
 * Simplified version of the MagicDetector, provided by tika-core.
 *
 * @author charlie (Dmitry Baev).
 */
@SuppressWarnings("all")
public final class MagicBytesContentTypeDetector {

    private static final List<Magic> MAGICS;

    static {
        final List<Magic> list = new ArrayList<>();

        // priority 60
        list.add(new Magic(
                60, "video/mp4",
                createMagicMatch("ftypmp42", null, "4", "string"),
                ".mp4", ".mp4v", ".mpg4"
        ));
        list.add(new Magic(
                60, "video/mp4",
                createMagicMatch("ftypmp41", null, "4", "string"),
                ".mp4", ".mp4v", ".mpg4"
        ));

        // priority 50
        list.add(new Magic(
                50, "image/svg+xml",
                new AndClause(Arrays.asList(
                        createMagicMatch("<svg", null, "0", "string"),
                        createMagicMatch("http://www.w3.org/2000/svg", null, "5:256", "string")
                )),
                ".svg", ".svgz"
        ));

        list.add(new Magic(
                50, "image/png",
                createMagicMatch("\\x89PNG\\x0d\\x0a\\x1a\\x0a", null, "0", "string"),
                ".png"
        ));

        list.add(new Magic(
                50, "application/x-gtar",
                createMagicMatch("ustar  \\0", null, "257", "string"),
                ".gtar"
        ));

        list.add(new Magic(
                50, "application/pdf",
                createMagicMatch("\\xef\\xbb\\xbf%PDF-", null, "0", "string"),
                ".pdf"
        ));

        list.add(new Magic(
                50, "image/gif",
                createMagicMatch("GIF89a", null, "0", "string"),
                ".gif"
        ));

        list.add(new Magic(
                50, "image/gif",
                createMagicMatch("GIF87a", null, "0", "string"),
                ".gif"
        ));

        list.add(new Magic(
                50, "image/bmp",
                new AndClause(Arrays.asList(
                        createMagicMatch("BM", null, "0", "string"),
                        new AndClause(Arrays.asList(
                                createMagicMatch("0x0100", null, "26", "string"),
                                new OrClause(Arrays.asList(
                                        createMagicMatch("0x0000", null, "28", "string"),
                                        createMagicMatch("0x0100", null, "28", "string"),
                                        createMagicMatch("0x0400", null, "28", "string"),
                                        createMagicMatch("0x0800", null, "28", "string"),
                                        createMagicMatch("0x1000", null, "28", "string"),
                                        createMagicMatch("0x1800", null, "28", "string"),
                                        createMagicMatch("0x2000", null, "28", "string")
                                ))
                        ))
                )),
                ".bmp", ".dib"
        ));

        list.add(new Magic(
                50, "application/pdf",
                createMagicMatch("%PDF-", null, "0", "string"),
                ".pdf"
        ));

        list.add(new Magic(
                50, "image/tiff",
                createMagicMatch("MM\\x00\\x2b", null, "0", "string"),
                ".tiff", ".tif"
        ));

        list.add(new Magic(
                50, "image/tiff",
                createMagicMatch("MM\\x00\\x2a", null, "0", "string"),
                ".tiff", ".tif"
        ));

        list.add(new Magic(
                50, "image/tiff",
                createMagicMatch("II\\x2a\\x00", null, "0", "string"),
                ".tiff", ".tif"
        ));

        list.add(new Magic(
                50, "application/zip",
                createMagicMatch("PK\\x07\\x08", null, "0", "string"),
                ".zip"
        ));

        list.add(new Magic(
                50, "application/zip",
                createMagicMatch("PK\\005\\006", null, "0", "string"),
                ".zip"
        ));

        list.add(new Magic(
                50, "application/zip",
                createMagicMatch("PK\\003\\004", null, "0", "string"),
                ".zip"
        ));

        list.add(new Magic(
                50, "image/jpeg",
                createMagicMatch("0xffd8ff", null, "0", "string"),
                ".jpg", ".jpeg", ".jpe", ".jif", ".jfif", ".jfi"
        ));

        // priority 45
        list.add(new Magic(
                45, "application/gzip",
                createMagicMatch("\\x1f\\x8b", null, "0", "string"),
                ".gz", ".tgz", "-gz"
        ));

        list.add(new Magic(
                45, "application/gzip",
                createMagicMatch("\\037\\213", null, "0", "string"),
                ".gz", ".tgz", "-gz"
        ));

        // priority 40
        list.add(new Magic(
                40, "application/pdf",
                new AndClause(Arrays.asList(
                        createMagicMatch("%%", null, "0:128", "string"),
                        createMagicMatch("%PDF-2.", null, "1:512", "string")
                )),
                ".pdf"
        ));

        list.add(new Magic(
                40, "application/pdf",
                new AndClause(Arrays.asList(
                        createMagicMatch("%%", null, "0:128", "string"),
                        createMagicMatch("%PDF-1.", null, "1:512", "string")
                )),
                ".pdf"
        ));

        list.add(new Magic(
                40, "application/x-tar",
                createMagicMatch("ustar\\0", null, "257", "string"),
                ".tar"
        ));

        // priority 20
        list.add(new Magic(
                20, "application/pdf",
                createMagicMatch("%PDF-2.", null, "1:512", "string"),
                ".pdf"
        ));

        list.add(new Magic(
                20, "application/pdf",
                createMagicMatch("%PDF-1.", null, "1:512", "string"),
                ".pdf"
        ));

        MAGICS = list;
    }

    private MagicBytesContentTypeDetector() {
        throw new IllegalStateException("Utility class");
    }

    // Copy of https://github.com/apache/tika/blob/main/tika-core/
    // src/main/java/org/apache/tika/detect/MagicDetector.java#L274
    private static byte[] decodeString(String value, String type) {
        if (value.startsWith("0x")) {
            byte[] vals = new byte[(value.length() - 2) / 2];
            for (int i = 0; i < vals.length; i++) {
                vals[i] = (byte) Integer.parseInt(value.substring(2 + i * 2, 4 + i * 2), 16);
            }
            return vals;
        }

        CharArrayWriter decoded = new CharArrayWriter();

        for (int i = 0; i < value.length(); i++) {
            if (value.charAt(i) == '\\') {
                if (value.charAt(i + 1) == '\\') {
                    decoded.write('\\');
                    i++;
                } else if (value.charAt(i + 1) == 'x') {
                    decoded.write(Integer.parseInt(value.substring(i + 2, i + 4), 16));
                    i += 3;
                } else if (value.charAt(i + 1) == 'r') {
                    decoded.write('\r');
                    i++;
                } else if (value.charAt(i + 1) == 'n') {
                    decoded.write('\n');
                    i++;
                } else {
                    int j = i + 1;
                    while ((j < i + 4) && (j < value.length()) &&
                           (Character.isDigit(value.charAt(j)))) {
                        j++;
                    }
                    decoded.write(Short.decode("0" + value.substring(i + 1, j)).byteValue());
                    i = j - 1;
                }
            } else {
                decoded.write(value.charAt(i));
            }
        }

        // Now turn the chars into bytes
        char[] chars = decoded.toCharArray();
        byte[] bytes;
        if ("unicodeLE".equals(type)) {
            bytes = new byte[chars.length * 2];
            for (int i = 0; i < chars.length; i++) {
                bytes[i * 2] = (byte) (chars[i] & 0xff);
                bytes[i * 2 + 1] = (byte) (chars[i] >> 8);
            }
        } else if ("unicodeBE".equals(type)) {
            bytes = new byte[chars.length * 2];
            for (int i = 0; i < chars.length; i++) {
                bytes[i * 2] = (byte) (chars[i] >> 8);
                bytes[i * 2 + 1] = (byte) (chars[i] & 0xff);
            }
        } else {
            // Copy with truncation
            bytes = new byte[chars.length];
            for (int i = 0; i < bytes.length; i++) {
                bytes[i] = (byte) chars[i];
            }
        }
        return bytes;
    }

    private static Clause createMagicMatch(final String value,
                                           final String mask,
                                           final String offset,
                                           final String type) {

        final byte[] decodedValue = decodeString(value, type);
        final byte[] decodedMask = (mask != null) ? decodeString(mask, type) : null;

        int patternLength = decodedValue.length;
        if (decodedMask != null && decodedMask.length > patternLength) {
            patternLength = decodedMask.length;
        }

        final byte[] resPattern = new byte[patternLength];
        final byte[] resMask = new byte[patternLength];

        for (int i = 0; i < patternLength; i++) {
            if (decodedMask != null && i < decodedMask.length) {
                resMask[i] = decodedMask[i];
            } else {
                resMask[i] = (byte) 0xFF; // -1 in byte
            }

            if (i < decodedValue.length) {
                final int v = decodedValue[i] & 0xFF;
                final int m = resMask[i] & 0xFF;
                resPattern[i] = (byte) (v & m);
            } else {
                resPattern[i] = 0;
            }
        }

        int start = 0;
        int end = 0;
        if (offset != null && !offset.isEmpty()) {
            final int colonIdx = offset.indexOf(':');
            if (colonIdx > 0) {
                final String startRaw = offset.substring(0, colonIdx);
                final String endRaw = offset.substring(colonIdx + 1);
                start = Integer.parseInt(startRaw);
                end = Integer.parseInt(endRaw);
            } else {
                start = Integer.parseInt(offset);
                end = start;
            }
        }

        return new MagicMatchClause(resPattern, resMask, start, end);
    }

    public static String detectContentType(final byte[] buffer) {
        if (buffer == null || buffer.length == 0) {
            return null;
        }

        final List<String> result = new ArrayList<>();
        int currentPriority = -1;

        for (Magic magic : MAGICS) {
            if (currentPriority > 0 && currentPriority > magic.getPriority()) {
                break;
            }
            if (magic.getClause().eval(buffer)) {
                if (currentPriority == magic.getPriority()) {
                    result.add(magic.getType());
                } else {
                    // clear lower priority matches
                    result.clear();
                    result.add(magic.getType());
                    currentPriority = magic.getPriority();
                }
            }
        }

        if (!result.isEmpty()) {
            return result.get(0);
        }
        return null;
    }
}
