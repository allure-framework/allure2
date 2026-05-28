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
package io.qameta.allure.timeline;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.qameta.allure.CommonJsonAggregator2;
import io.qameta.allure.core.LaunchResults;
import io.qameta.allure.entity.LabelName;
import io.qameta.allure.entity.Status;
import io.qameta.allure.entity.TestResult;
import io.qameta.allure.entity.Time;
import io.qameta.allure.tree.AbstractTree;
import io.qameta.allure.tree.DefaultTreeLeaf;
import io.qameta.allure.tree.TestResultGroupFactory;
import io.qameta.allure.tree.TestResultTreeGroup;
import io.qameta.allure.tree.Tree;

import java.util.Collection;
import java.util.List;

import static io.qameta.allure.tree.TreeUtils.createGroupUid;
import static io.qameta.allure.tree.TreeUtils.groupByLabels;

/**
 * Plugin that generates data for Timeline tab.
 *
 * @since 2.0
 */
public class TimelinePlugin extends CommonJsonAggregator2 {

    private static final String TIMELINE = "timeline";

    public TimelinePlugin() {
        super(TIMELINE + ".json");
    }

    @Override
    protected Tree<TestResult> getData(final List<LaunchResults> launchResults) {

        final Tree<TestResult> timeline = new TimelineTree();

        launchResults.stream()
                .map(LaunchResults::getAllResults)
                .flatMap(Collection::stream)
                .forEach(timeline::add);
        return timeline;
    }

    private static final class TimelineTree extends AbstractTree<TestResult, TestResultTreeGroup, TimelineTreeLeaf> {

        TimelineTree() {
            super(
                    new TestResultTreeGroup(createGroupUid(null, TIMELINE), TIMELINE),
                    testResult -> groupByLabels(testResult, LabelName.HOST, LabelName.THREAD),
                    new TestResultGroupFactory(),
                    (parent, item) -> new TimelineTreeLeaf(item)
            );
        }

        @JsonProperty("uid")
        String getUid() {
            return root.getUid();
        }

        @Override
        protected Class<TestResultTreeGroup> getRootType() {
            return TestResultTreeGroup.class;
        }
    }

    private static final class TimelineTreeLeaf extends DefaultTreeLeaf {

        private final String uid;
        private final Status status;
        private final Time time;
        private final Boolean retry;

        TimelineTreeLeaf(final TestResult testResult) {
            super(testResult.getName());
            this.uid = testResult.getUid();
            this.status = testResult.getStatus();
            this.time = testResult.getTime();
            this.retry = testResult.isRetry() ? Boolean.TRUE : null;
        }

        @JsonProperty("uid")
        String getUid() {
            return uid;
        }

        @JsonProperty("status")
        Status getStatus() {
            return status;
        }

        @JsonProperty("time")
        Time getTime() {
            return time;
        }

        @JsonProperty("retry")
        Boolean getRetry() {
            return retry;
        }
    }
}
