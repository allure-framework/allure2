/*
 *  Copyright 2016-2023 Qameta Software OÜ
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
package io.qameta.allure.tree;

import io.qameta.allure.entity.Status;
import io.qameta.allure.entity.TestResult;
import io.qameta.allure.entity.Time;

import java.util.List;
import java.util.Set;

/**
 * @author charlie (Dmitry Baev).
 */
public class TestResultTreeLeaf extends DefaultTreeLeaf {

    private final String uid;
    private final String parentUid;
    private final Status status;
    private final Time time;
    private final boolean flaky;
    private final boolean newFailed;
    private final boolean newPassed;
    private final boolean newBroken;
    private final int retriesCount;
    private final boolean retriesStatusChange;

    private final List<String> parameters;
    private final Set<String> tags;

    public TestResultTreeLeaf(final String parentUid, final TestResult testResult) {
        this(
                parentUid,
                testResult.getName(),
                testResult
        );
    }

    public TestResultTreeLeaf(final String parentUid, final String name, final TestResult testResult) {
        super(name);
        this.parentUid = parentUid;
        this.uid = testResult.getUid();
        this.status = testResult.getStatus();
        this.time = testResult.getTime();
        this.flaky = testResult.isFlaky();
        this.newFailed = testResult.isNewFailed();
        this.newPassed = testResult.isNewPassed();
        this.newBroken = testResult.isNewBroken();
        this.retriesStatusChange = testResult.isRetriesStatusChange();
        this.retriesCount = testResult.getRetriesCount();
        this.parameters = testResult.getParameterValues();
        this.tags = testResult.getExtraBlock("tags");
    }
    public String getParentUid() {
        return parentUid;
    }

    public String getUid() {
        return uid;
    }

    public Status getStatus() {
        return status;
    }

    public Time getTime() {
        return time;
    }

    public boolean isFlaky() {
        return flaky;
    }

    public boolean isNewFailed() {
        return newFailed;
    }

    public boolean isNewPassed() {
        return newPassed;
    }

    public boolean isNewBroken() {
        return newBroken;
    }

    public int getRetriesCount() {
        return retriesCount;
    }

    public boolean isRetriesStatusChange() {
        return retriesStatusChange;
    }

    public List<String> getParameters() {
        return parameters;
    }

    public Set<String> getTags() {
        return tags;
    }
}
