package io.qameta.allure.allure2;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import io.qameta.allure.model.Status;

import java.io.IOException;
import java.util.stream.Stream;

/**
 * @author charlie (Dmitry Baev).
 */
public class StatusDeserializer extends StdDeserializer<Status> {
    protected StatusDeserializer() {
        super(Status.class);
    }

    @Override
    public Status deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        String value = p.readValueAs(String.class);
        return Stream.of(Status.values())
                .filter(status -> status.value().equalsIgnoreCase(value))
                .findAny()
                .orElse(null);
    }
}
