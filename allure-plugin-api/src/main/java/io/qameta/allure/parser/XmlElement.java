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
package io.qameta.allure.parser;

import org.w3c.dom.CharacterData;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * @author charlie (Dmitry Baev).
 */
public class XmlElement implements Iterable<XmlElement> {

    private final Map<String, String> attributes = new HashMap<>();

    private final List<XmlElement> children = new ArrayList<>();

    private final String name;

    private final String value;

    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    public XmlElement(final Element element) {
        this.name = element.getNodeName();
        final NamedNodeMap attributes = element.getAttributes();
        for (int i = 0; i < attributes.getLength(); i++) {
            final Node item = attributes.item(i);
            this.attributes.put(item.getNodeName(), item.getNodeValue());
        }

        final StringBuilder textValue = new StringBuilder();
        final NodeList children = element.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            final Node node = children.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                this.children.add(new XmlElement((Element) node));
            }
            if (node.getNodeType() == Node.TEXT_NODE) {
                textValue.append(node.getNodeValue());
            }
            if (node.getNodeType() == Node.CDATA_SECTION_NODE) {
                textValue.append(((CharacterData) node).getData());
            }
        }
        this.value = textValue.toString();
    }

    public List<XmlElement> get(final String name) {
        return children.stream()
                .filter(elementWithName(name))
                .collect(Collectors.toList());

    }

    public Optional<XmlElement> getFirst(final String name) {
        return children.stream()
                .filter(elementWithName(name))
                .findFirst();
    }

    public boolean contains(final String name) {
        return children.stream()
                .anyMatch(elementWithName(name));
    }

    public String getAttribute(final String key) {
        return attributes.get(key);
    }

    public Double getDoubleAttribute(final String key) {
        return Double.parseDouble(attributes.get(key));
    }

    public boolean containsAttribute(final String key) {
        return attributes.containsKey(key);
    }

    public Map<String, String> getAttributes() {
        return attributes;
    }

    public List<XmlElement> getChildren() {
        return children;
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }

    @Override
    public Iterator<XmlElement> iterator() {
        return children.iterator();
    }

    private Predicate<XmlElement> elementWithName(final String name) {
        return xmlElement -> Objects.equals(xmlElement.getName(), name);
    }
}
