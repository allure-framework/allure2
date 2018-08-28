package io.qameta.allure.allure1;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import ru.yandex.qatools.allure.model.Status;

import java.io.IOException;
import java.util.stream.Stream;

/**
 * @author charlie (Dmitry Baev).
 */
public class StatusDeserializer extends StdDeserializer<Status> {

    protected StatusDeserializer() {
        super(io.qameta.allure.model.Status.class);
    }

    @Override
    public Status deserialize(final JsonParser p, final DeserializationContext ctxt) throws IOException {
        final String value = p.readValueAs(String.class);
        return Stream.of(Status.values())
                .filter(status -> status.value().equalsIgnoreCase(value))
                .findAny()
                .orElse(null);
    }
}
