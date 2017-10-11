package io.qameta.allure.allure1;

import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.deser.BeanDeserializerModifier;
import com.fasterxml.jackson.databind.deser.std.CollectionDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.type.CollectionType;

/**
 * @author charlie (Dmitry Baev).
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
