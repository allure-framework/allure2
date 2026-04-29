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
package io.qameta.allure.core;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents one entry in the Vite build manifest.
 */
public final class ViteManifestEntry {

    private List<String> assets = new ArrayList<>();
    private List<String> css = new ArrayList<>();
    private String file;
    private List<String> imports = new ArrayList<>();
    @JsonProperty("isEntry")
    private boolean isEntry;
    @JsonProperty("isDynamicEntry")
    private boolean isDynamicEntry;
    private String name;
    private String src;
    private List<String> dynamicImports = new ArrayList<>();

    public List<String> getAssets() {
        return assets;
    }

    public void setAssets(final List<String> assets) {
        this.assets = assets == null ? new ArrayList<>() : new ArrayList<>(assets);
    }

    public List<String> getCss() {
        return css;
    }

    public void setCss(final List<String> css) {
        this.css = css == null ? new ArrayList<>() : new ArrayList<>(css);
    }

    public List<String> getDynamicImports() {
        return dynamicImports;
    }

    public void setDynamicImports(final List<String> dynamicImports) {
        this.dynamicImports = dynamicImports == null
                ? new ArrayList<>()
                : new ArrayList<>(dynamicImports);
    }

    public String getFile() {
        return file;
    }

    public void setFile(final String file) {
        this.file = file;
    }

    public List<String> getImports() {
        return imports;
    }

    public void setImports(final List<String> imports) {
        this.imports = imports == null ? new ArrayList<>() : new ArrayList<>(imports);
    }

    public boolean isDynamicEntry() {
        return isDynamicEntry;
    }

    public void setIsDynamicEntry(final boolean dynamicEntry) {
        isDynamicEntry = dynamicEntry;
    }

    public boolean isEntry() {
        return isEntry;
    }

    public void setIsEntry(final boolean entry) {
        isEntry = entry;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getSrc() {
        return src;
    }

    public void setSrc(final String src) {
        this.src = src;
    }
}
