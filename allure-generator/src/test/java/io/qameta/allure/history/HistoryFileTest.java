package io.qameta.allure.history;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.qameta.allure.Main;
import io.qameta.allure.model.Allure2ModelJackson;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static io.qameta.allure.history.HistoryReader.HISTORY_TYPE;
import static io.qameta.allure.testdata.TestData.unpackDummyResources;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static ru.yandex.qatools.matchers.nio.PathMatchers.contains;

/**
 * @author Egor Borisov ehborisov@gmail.com
 */
public class HistoryFileTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    private final ObjectMapper mapper = Allure2ModelJackson.createMapper();

    @Test
    public void skipHistoryForTestCaseWithoutId() throws Exception {
        Main main = new Main();
        Path resultsDirectory = folder.newFolder().toPath();
        Path output = folder.newFolder().toPath();
        unpackDummyResources("allure2data/", resultsDirectory);
        main.generate(output, resultsDirectory);
        assertThat(output, contains("index.html"));
        assertThat(output, contains("data"));
        Path data = output.resolve("data");
        assertThat(data, contains("history.json"));

        try (InputStream is = Files.newInputStream(data.resolve("history.json"))) {
            Map<String, HistoryData> history = mapper.readValue(is, HISTORY_TYPE);
            List<String> names = history.values().stream()
                    .map(HistoryData::getName)
                    .collect(Collectors.toList());
            assertThat(names, hasSize(3));
            assertThat(names, hasItems("shouldMainTest", "shouldDelete", "shouldCreate"));
        } catch (IOException e) {
            e.printStackTrace(System.err);
            fail("Cannot read history.json file");
        }
    }
}
