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

import io.qameta.allure.entity.TestResult;

import static io.qameta.allure.tree.TreeUtils.createGroupUid;

/**
 * @author charlie (Dmitry Baev).
 */
public class TestResultGroupFactory implements TreeGroupFactory<TestResult, TestResultTreeGroup> {

    @Override
    public TestResultTreeGroup create(final TestResultTreeGroup parent, final String name, final TestResult item) {
        return new TestResultTreeGroup(createGroupUid(parent.getUid(), name), name);
    }
}
