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
package io.qameta.allure.entity;

/**
 * @author Dmitry Baev baev@qameta.io
 *         Date: 31.01.16
 */
public interface WithStatistic {

    Statistic getStatistic();

    void setStatistic(Statistic statistic);

    default void updateStatistic(Statistic other) {
        getStatistic().setFailed(other.getFailed() + getStatistic().getFailed());
        getStatistic().setBroken(other.getBroken() + getStatistic().getBroken());
        getStatistic().setPassed(other.getPassed() + getStatistic().getPassed());
        getStatistic().setSkipped(other.getSkipped() + getStatistic().getSkipped());
        getStatistic().setUnknown(other.getUnknown() + getStatistic().getUnknown());
    }

    default void updateStatistic(Statusable statusable) {
        if (statusable == null) {
            return;
        }
        if (getStatistic() == null) {
            setStatistic(new Statistic());
        }
        getStatistic().update(statusable.getStatus());
    }
}
