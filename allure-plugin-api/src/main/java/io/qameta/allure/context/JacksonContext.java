package io.qameta.allure.context;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import io.qameta.allure.Context;

/**
 * Context that stores pre-configured jackson mapper.
 *
 * @since 2.0
 */
public class JacksonContext implements Context<ObjectMapper> {

    private final ObjectMapper mapper;

    public JacksonContext() {
        this.mapper = new ObjectMapper()
                .enable(SerializationFeature.INDENT_OUTPUT)
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                .setSerializationInclusion(JsonInclude.Include.NON_NULL);
    }

    @Override
    public ObjectMapper getValue() {
        return mapper;
    }
}
