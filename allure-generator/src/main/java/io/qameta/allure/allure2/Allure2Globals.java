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
package io.qameta.allure.allure2;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.List;

/**
 * Allure 2 run-level data stored in {@code *-globals.json} files.
 */
@Data
@Accessors(chain = true)
class Allure2Globals {

    private List<Error> errors = new ArrayList<>();
    private List<Attachment> attachments = new ArrayList<>();

    @Data
    @Accessors(chain = true)
    static class Error {
        private Long timestamp;
        private String message;
        private String trace;
        private String actual;
        private String expected;
    }

    @Data
    @Accessors(chain = true)
    static class Attachment {
        private String name;
        private String type;
        private String source;
    }

}
