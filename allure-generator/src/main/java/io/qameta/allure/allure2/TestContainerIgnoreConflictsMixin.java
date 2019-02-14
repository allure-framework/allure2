/*
 *  Copyright 2019 Qameta Software OÜ
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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.qameta.allure.model.FixtureResult;
import io.qameta.allure.model.Link;
import io.qameta.allure.model.TestResultContainer;

import java.util.Collection;
import java.util.List;

/**
 * @author charlie (Dmitry Baev).
 */
@SuppressWarnings("PMD.DefaultPackage")
public abstract class TestContainerIgnoreConflictsMixin {

    @JsonProperty
    abstract TestResultContainer setChildren(List<String> values);

    @JsonIgnore
    abstract TestResultContainer setChildren(String... values);

    @JsonIgnore
    abstract TestResultContainer setChildren(Collection<String> values);

    @JsonProperty
    abstract TestResultContainer setBefores(List<FixtureResult> values);

    @JsonIgnore
    abstract TestResultContainer setBefores(FixtureResult... values);

    @JsonIgnore
    abstract TestResultContainer setBefores(Collection<FixtureResult> values);

    @JsonProperty
    abstract TestResultContainer setAfters(List<FixtureResult> values);

    @JsonIgnore
    abstract TestResultContainer setAfters(FixtureResult... values);

    @JsonIgnore
    abstract TestResultContainer setAfters(Collection<FixtureResult> values);

    @JsonProperty
    abstract TestResultContainer setLinks(List<Link> values);

    @JsonIgnore
    abstract TestResultContainer setLinks(Link... values);

    @JsonIgnore
    abstract TestResultContainer setLinks(Collection<Link> values);

}
