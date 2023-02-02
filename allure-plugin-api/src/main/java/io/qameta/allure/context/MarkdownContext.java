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
package io.qameta.allure.context;

import com.vladsch.flexmark.ext.tables.TablesExtension;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.data.MutableDataSet;
import io.qameta.allure.Context;

import java.util.Arrays;
import java.util.function.Function;

/**
 * Markdown context. Can be used to process markdown files to html.
 *
 * @since 2.0
 */
public class MarkdownContext implements Context<Function<String, String>> {

    private static final MutableDataSet OPTIONS = new MutableDataSet()
            .set(HtmlRenderer.SOFT_BREAK, "<br />\n")
            .set(HtmlRenderer.SUPPRESS_HTML, true)
            .set(Parser.EXTENSIONS, Arrays.asList(TablesExtension.create()));

    @Override
    public Function<String, String> getValue() {
        final Parser parser = Parser.builder(OPTIONS).build();
        final HtmlRenderer renderer = HtmlRenderer.builder(OPTIONS).build();
        return s -> renderer.render(parser.parse(s));
    }
}
