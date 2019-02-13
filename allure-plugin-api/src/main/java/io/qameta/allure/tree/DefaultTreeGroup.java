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
package io.qameta.allure.tree;

import java.util.ArrayList;
import java.util.List;

/**
 * @author charlie (Dmitry Baev).
 */
public class DefaultTreeGroup implements TreeGroup {

    private String name;

    private List<TreeNode> children = new ArrayList<>();

    public DefaultTreeGroup(final String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public List<TreeNode> getChildren() {
        return children;
    }

    @Override
    public void addChild(final TreeNode node) {
        children.add(node);
    }

    public void setName(final String name) {
        this.name = name;
    }

    public void setChildren(final List<TreeNode> children) {
        this.children = children;
    }
}
