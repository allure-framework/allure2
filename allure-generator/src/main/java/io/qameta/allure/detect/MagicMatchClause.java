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
package io.qameta.allure.detect;

/**
 * @author charlie (Dmitry Baev).
 */
final class MagicMatchClause implements Clause {
    private final byte[] pattern;
    private final byte[] mask;
    private final int start;
    private final int end;

    MagicMatchClause(final byte[] pattern, final byte[] mask, final int start, final int end) {
        this.pattern = pattern;
        this.mask = mask;
        this.start = start;
        this.end = end;
    }

    @Override
    public boolean eval(final byte[] data) {
        if (data.length < pattern.length + start) {
            return false;
        }

        // avoid running past the end of the array
        int effectiveEnd = end;
        final int maxStart = data.length - pattern.length;
        if (effectiveEnd > maxStart) {
            effectiveEnd = maxStart;
        }
        if (effectiveEnd < start) {
            return false;
        }

        for (int i = start; i <= effectiveEnd; i++) {
            boolean match = true;
            for (int j = 0; match && j < pattern.length; j++) {
                final int masked = (data[i + j] & 0xFF) & (mask[j] & 0xFF);
                match = masked == (pattern[j] & 0xFF);
            }
            if (match) {
                return true;
            }
        }
        return false;
    }
}
