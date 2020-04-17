package io.qameta.allure;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.qameta.allure.hotspot.LocatorAction;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class HotspotPLugin {

    @Test
    public void testOutput() throws Exception {
        final ObjectMapper mapper = new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        final Path path = Paths.get("/Users/eroshenkoam/Downloads/realty/realty-web-tests/target/one/553d09ac-7872-4d5a-a10e-5ba1fad52ea4-attachment.locators2");
        final List<LocatorAction> actions = mapper
                .readValue(path.toFile(), new TypeReference<List<LocatorAction>>() {
                });

    }

}
