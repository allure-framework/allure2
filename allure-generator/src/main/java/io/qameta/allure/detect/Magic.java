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

/**
 * @author charlie (Dmitry Baev).
 */
final class Magic {
    private final int priority;
    private final Clause clause;
    private final String type;
    private final String[] extensions;

    Magic(final int priority, final String type, final Clause clause, final String... extensions) {
        this.priority = priority;
        this.type = type;
        this.clause = clause;
        this.extensions = extensions;
    }

    public int getPriority() {
        return priority;
    }

    public Clause getClause() {
        return clause;
    }

    public String getType() {
        return type;
    }

    @SuppressWarnings("all")
    public String[] getExtensions() {
        return extensions;
    }
}
