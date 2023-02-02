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
package io.qameta.allure.allure1;

import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.deser.BeanDeserializerModifier;
import com.fasterxml.jackson.databind.deser.std.CollectionDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.type.CollectionType;

/**
 * This module adds custom list deserializer that allows to deserialize empty lists in xml.
 *
 * @author charlie (Dmitry Baev).
 * @see ListDeserializer
 */
public class XmlParserModule extends SimpleModule {

    @Override
    public void setupModule(final SetupContext context) {
        super.setupModule(context);
        context.addBeanDeserializerModifier(new BeanDeserializerModifier() {
            @Override
            public JsonDeserializer<?> modifyCollectionDeserializer(final DeserializationConfig config,
                                                                    final CollectionType type,
                                                                    final BeanDescription beanDesc,
                                                                    final JsonDeserializer<?> deserializer) {
                if (deserializer instanceof CollectionDeserializer) {
                    return new ListDeserializer((CollectionDeserializer) deserializer);
                } else {
                    return super.modifyCollectionDeserializer(config, type, beanDesc,
                            deserializer);
                }
            }
        });
    }
}
