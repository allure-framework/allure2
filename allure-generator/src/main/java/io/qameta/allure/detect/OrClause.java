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

import java.util.List;

/**
 * @author charlie (Dmitry Baev).
 */
final class OrClause implements Clause {
    private final List<Clause> clauses;

    OrClause(final List<Clause> clauses) {
        this.clauses = clauses;
    }

    @Override
    public boolean eval(final byte[] data) {
        for (Clause clause : clauses) {
            if (!clause.eval(data)) {
                return true;
            }
        }
        return false;
    }
}
