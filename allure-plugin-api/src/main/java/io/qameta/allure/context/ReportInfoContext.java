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
package io.qameta.allure.context;

import io.qameta.allure.Context;
import io.qameta.allure.ReportInfo;
import io.qameta.allure.core.Configuration;

import java.util.UUID;

/**
 * @author charlie (Dmitry Baev).
 * @deprecated use {@link Configuration#getUuid()} and {@link Configuration#getVersion()} instead.
 */
@Deprecated
public class ReportInfoContext implements Context<ReportInfo> {

    private final ReportInfo reportInfo;

    public ReportInfoContext(final String allureVersion) {
        this(allureVersion, UUID.randomUUID().toString());
    }

    public ReportInfoContext(final String uuid, final String allureVersion) {
        this.reportInfo = new ReportInfo()
                .setAllureVersion(allureVersion)
                .setReportUuid(uuid);
    }

    @Override
    public ReportInfo getValue() {
        return reportInfo;
    }
}
